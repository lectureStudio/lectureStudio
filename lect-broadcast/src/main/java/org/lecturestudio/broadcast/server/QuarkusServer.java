package org.lecturestudio.broadcast.server;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.quarkus.bootstrap.runner.QuarkusEntryPoint;
import io.quarkus.bootstrap.runner.SerializedApplication;
import io.quarkus.bootstrap.runner.Timing;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.jul.LogManager;
import org.lecturestudio.broadcast.config.Configuration;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

public class QuarkusServer extends ExecutableBase {

	private final Configuration config;


	public QuarkusServer(Configuration config) {
		this.config = config;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		if (isNull(config)) {
			throw new ExecutableException("No configuration provided");
		}
	}

	@Override
	protected void startInternal() {
		if (nonNull(config.port)) {
			System.setProperty("quarkus.http.port", config.port.toString());
		}
		if (nonNull(config.tlsPort)) {
			System.setProperty("quarkus.http.ssl-port", config.tlsPort.toString());
		}

		CompletableFuture.runAsync(() -> {
			try {
				runFast(new String[] { });
			}
			catch (Throwable e) {
				logException(e, "Start quarkus server failed");
			}
		});
	}

	@Override
	protected void stopInternal() {
//		Quarkus.asyncExit();
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



	public static class App implements QuarkusApplication {

		@Override
		public int run(String... args) {
			Quarkus.waitForExit();
			return 0;
		}

	}
}
