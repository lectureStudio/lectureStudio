package org.lecturestudio.broadcast.server;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

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

		Quarkus.run(App.class);
	}

	@Override
	protected void stopInternal() {
		Quarkus.asyncExit();
	}

	@Override
	protected void destroyInternal() {

	}



	public static class App implements QuarkusApplication {

		@Override
		public int run(String... args) {
			Quarkus.waitForExit();
			return 0;
		}

	}
}
