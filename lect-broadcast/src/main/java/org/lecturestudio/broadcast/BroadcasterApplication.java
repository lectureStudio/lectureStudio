/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.broadcast;

import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.broadcast.config.Configuration;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationBase;

@QuarkusMain
public class BroadcasterApplication extends ApplicationBase {

	private static final Logger LOG = LogManager.getLogger(BroadcasterApplication.class);

	private static final Options OPTIONS = new Options();

	private Broadcaster broadcaster;


	static {
		OPTIONS.addOption("help", false, "Print out a usage message");
		OPTIONS.addOption("p", true, "HTTP port");
		OPTIONS.addOption("tls", true, "HTTP TLS port");
	}


	/**
	 * The entry point of the application. This method calls the static {@link
	 * #launch(String[])} method to fire up the application.
	 *
	 * @param args the main method's arguments.
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;

		try {
			cmd = parser.parse(OPTIONS, args);
		}
		catch (ParseException e) {
			System.err.println("Invalid arguments: " + Arrays.toString(args));
			return;
		}

		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("lectureBroadcaster", OPTIONS);
			return;
		}

		BroadcasterApplication.launch(args);

		// Keep it running.
		try {
			System.in.read();
		}
		catch (IOException e) {
			// Ignore.
		}
	}

	@Override
	protected void initInternal(String[] args) throws ExecutableException {
		Configuration config = new Configuration();

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(OPTIONS, args);

			if (cmd.hasOption("p")) {
				config.port = Integer.parseInt(cmd.getOptionValue("p"));
			}
			if (cmd.hasOption("tls")) {
				config.tlsPort = Integer.parseInt(cmd.getOptionValue("tls"));
			}
		}
		catch (Exception e) {
			LOG.error("Parse input options failed", e);
		}

		broadcaster = new Broadcaster(config);
		broadcaster.init();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		broadcaster.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		broadcaster.stop();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		broadcaster.destroy();
	}
}
