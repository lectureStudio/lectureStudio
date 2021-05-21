package org.lecturestudio.broadcast.server;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.quarkus.bootstrap.runner.QuarkusEntryPoint;
import io.quarkus.bootstrap.runner.SerializedApplication;
import io.quarkus.bootstrap.runner.Timing;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.jul.LogManager;
import org.lecturestudio.broadcast.config.Configuration;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.AppDataLocator;

public class QuarkusServer extends ExecutableBase {

	private final AppDataLocator dataLocator;

	private final Configuration config;


	public QuarkusServer(Configuration config) {
		dataLocator = new AppDataLocator("lectureBroadcaster");
		this.config = config;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(config)) {
			throw new ExecutableException("No configuration provided");
		}

		Path privateKeyPath = Paths.get(dataLocator.toAppDataPath("jwt/privateKey.pem"));
		Path publicKeyPath = Paths.get(dataLocator.toAppDataPath("jwt/publicKey.pem"));

		if (Files.notExists(privateKeyPath)) {
			try {
				generateJwtKeys(privateKeyPath, publicKeyPath);
			}
			catch (Exception e) {
				throw new ExecutableException(e);
			}
		}
		else {
			// Generate new key pair every day.
			Instant retentionPeriod = ZonedDateTime.now().minusDays(1)
					.toInstant();

			try {
				if (Files.getLastModifiedTime(privateKeyPath).toInstant()
						.isBefore(retentionPeriod)) {
					generateJwtKeys(privateKeyPath, publicKeyPath);
				}
			}
			catch (Exception e) {
				throw new ExecutableException(e);
			}
		}

		System.setProperty("smallrye.jwt.sign.key.location", privateKeyPath.toString());
		System.setProperty("mp.jwt.verify.publickey.location", publicKeyPath.toString());
	}

	@Override
	protected void startInternal() {
		if (nonNull(config.port)) {
			System.setProperty("quarkus.http.port", config.port.toString());
		}
		if (nonNull(config.tlsPort)) {
			System.setProperty("quarkus.http.ssl-port", config.tlsPort.toString());
		}

		try {
			Class.forName("io.quarkus.runner.ApplicationImpl");

			Quarkus.run();
		}
		catch (Throwable e) {
			CompletableFuture.runAsync(() -> {
				try {
					runFast(new String[] { });
				}
				catch (Throwable error) {
					logException(error, "Start quarkus server failed");
				}
			});
		}
	}

	@Override
	protected void stopInternal() {
		Quarkus.asyncExit();
	}

	@Override
	protected void destroyInternal() {

	}

	private static void runFast(Object args) throws Exception {
		System.setProperty("java.util.logging.manager", LogManager.class.getName());

		Timing.staticInitStarted();

		String path = QuarkusServer.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
		Path appRoot = new File(decodedPath).toPath().getParent()
				.resolve("broadcast");

		SerializedApplication app;

		// The magic number here is close to the smallest possible dat file.
		try (InputStream in = new BufferedInputStream(Files.newInputStream(
				appRoot.resolve(QuarkusEntryPoint.QUARKUS_APPLICATION_DAT)),
				24_576)) {
			app = SerializedApplication.read(in, appRoot);
		}

		try {
			Thread.currentThread().setContextClassLoader(app.getRunnerClassLoader());
			Class<?> mainClass = app.getRunnerClassLoader().loadClass(app.getMainClass());
			mainClass.getMethod("main", String[].class).invoke(null, args);
		}
		finally {
			app.getRunnerClassLoader().close();
		}
	}

	private static void generateJwtKeys(Path privateKeyPath, Path publicKeyPath) throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();

		if (Files.notExists(privateKeyPath.getParent())) {
			Files.createDirectories(privateKeyPath.getParent());
		}

		try (BufferedWriter writer = Files.newBufferedWriter(privateKeyPath,
				StandardCharsets.UTF_8)) {
			writer.write("-----BEGIN PRIVATE KEY-----");
			writer.write(System.lineSeparator());
			writer.write(Base64.getMimeEncoder().encodeToString(kp.getPrivate().getEncoded()));
			writer.write(System.lineSeparator());
			writer.write("-----END PRIVATE KEY-----");
		}

		try (BufferedWriter writer = Files.newBufferedWriter(publicKeyPath,
				StandardCharsets.UTF_8)) {
			writer.write("-----BEGIN PUBLIC KEY-----");
			writer.write(System.lineSeparator());
			writer.write(Base64.getMimeEncoder().encodeToString(kp.getPublic().getEncoded()));
			writer.write(System.lineSeparator());
			writer.write("-----END PUBLIC KEY-----");
		}
	}



	public static class App implements QuarkusApplication {

		@Override
		public int run(String... args) {
			Quarkus.waitForExit();
			return 0;
		}

	}
}
