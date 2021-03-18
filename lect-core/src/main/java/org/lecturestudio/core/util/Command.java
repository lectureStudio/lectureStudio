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

package org.lecturestudio.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Command {

	private final static Logger LOGGER = LogManager.getLogger(Command.class);


	public static void execute(String command) throws Exception {
		LOGGER.info("Executing command {}", command);
		String[] shellCommand = null;
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Windows")) {
			LOGGER.debug("MS Windows platform detected.");
			String comSpec = System.getenv().get("COMSPEC");
			
			if (comSpec != null) {
				LOGGER.debug("Using ComSpec MS Windows environment variable:");
				LOGGER.debug("{} /C {}", comSpec, command);
				shellCommand = new String[] { comSpec, "/C", command };
			}
			else {
				LOGGER.warn("MS Windows ComSpec environment variable is not defined.");
			}
		}
		else {
			LOGGER.debug("Unix platform detected.");
			String shell = System.getenv().get("SHELL");
			if (shell != null) {
				LOGGER.debug("Using Unix SHELL environment variable:");
				LOGGER.debug(shell + " -c " + command);
				shellCommand = new String[] { shell, "-c", command };
			}
			else {
				LOGGER.debug("Unix SHELL environment variable is not defined.");
				LOGGER.debug("Using default Unix shell:");
				LOGGER.debug("/bin/sh -c " + command);
				shellCommand = new String[] { "/bin/sh", "-c", command };
			}
		}
		try {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(shellCommand);
		}
		catch (Exception exception) {
			LOGGER.error("Command {} execution failed", command, exception);
			throw new Exception("Command " + command + " execution failed", exception);
		}
	}

	public static void execute(String[] commandArray) throws Exception {
		String command = Arrays.toString(commandArray);
		LOGGER.info("Executing command {}", command);

		try {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(commandArray);
		}
		catch (Exception exception) {
			LOGGER.error("Command {} execution failed", command, exception);
			throw new Exception("Command " + command + " execution failed", exception);
		}
	}

}
