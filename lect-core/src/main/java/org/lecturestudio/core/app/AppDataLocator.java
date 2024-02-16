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

package org.lecturestudio.core.app;

import static java.util.Objects.nonNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lecturestudio.core.util.OsInfo;

/**
 * The application data locator translates the paths of application related
 * files to the corresponding application data folder.
 *
 * @author Alex Andres
 */
public class AppDataLocator {

	/** The application name. */
	private final String appName;


	/**
	 * Create a new AppDataLocator with the specified application name and main
	 * application class.
	 *
	 * @param appName The application name.
	 */
	public AppDataLocator(String appName) {
		this.appName = appName;
	}

	/**
	 * Obtain the application data folder path.
	 *
	 * @return the application data folder path.
	 */
	public String getAppDataPath() {
		String userHome = System.getProperty("user.home");
		String path = "";
		Path appPath = null;

		if (nonNull(appName)) {
			path = appName;
		}

		if (OsInfo.isLinux()) {
			appPath = Paths.get(userHome, ".config");
		}
		else if (OsInfo.isMacOs()) {
			appPath = Paths.get(userHome, "Library", "Application Support");
		}
		else if (OsInfo.isWindows()) {
			appPath = Paths.get(userHome, "AppData", "Local");
		}

		if (nonNull(appPath)) {
			path = appPath.resolve(path).toString();
		}

		return path;
	}

	/**
	 * Translate the specified sub-path to the complete application data folder
	 * path. Once the complete path has been resolved, the path will end with
	 * the specified sub-path.
	 *
	 * @param subPath The sub-path in the application data folder.
	 *
	 * @return the resolved application data folder path.
	 */
	public String toAppDataPath(String subPath) {
		return getAppDataPath() + File.separator + subPath;
	}

}
