package org.lecturestudio.broadcast.server;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

import java.io.IOException;

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

	}

	@Override
	protected void startInternal() throws ExecutableException {
		Quarkus.run(App.class);
		Quarkus.waitForExit();
	}

	@Override
	protected void stopInternal() throws ExecutableException {

	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}



	public static class App implements QuarkusApplication {

		@Override
		public int run(String... args) throws IOException {
			System.in.read();
			return 0;
		}

	}
}
