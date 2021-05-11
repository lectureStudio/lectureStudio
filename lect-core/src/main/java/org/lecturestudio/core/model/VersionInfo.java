/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.model;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.lecturestudio.core.io.ResourceLoader;

/**
 * Version information container.
 *
 * @author Alex Andres
 */
public class VersionInfo {

	/**
	 * The download URL for the package.
	 */
	public URL downloadUrl;

	/**
	 * The website URL.
	 */
	public URL htmlUrl;

	/**
	 * The version string.
	 */
	public String version;

	/**
	 * The publish date.
	 */
	public LocalDateTime published;


	/**
	 * Retrieves the version of the running application. The version of deployed
	 * applications will be retrieved from the {@code Package-Version} entry in
	 * the application's manifest. The default value is {@code
	 * 999.999.999-snapshot}.
	 *
	 * @return The application version.
	 */
	public static String getAppVersion() {
		String version;

		try {
			Manifest manifest = new Manifest(ResourceLoader.getResourceAsStream(JarFile.MANIFEST_NAME));
			Attributes attr = manifest.getMainAttributes();
			version = attr.getValue("Package-Version");

			Objects.requireNonNull(version);
		}
		catch (Exception e) {
			// Dev mode.
			version = "999.999.999-snapshot";
		}

		return version;
	}

	/**
	 * Retrieves the application publish date. The date of deployed applications
	 * will be retrieved from the {@code Build-Date} entry in the application's
	 * manifest. The default value is the current date-time from the system
	 * clock.
	 *
	 * @return The application publish date.
	 */
	public static LocalDateTime getAppPublishDate() {
		LocalDateTime date;

		try {
			Manifest manifest = new Manifest(ResourceLoader.getResourceAsStream(JarFile.MANIFEST_NAME));
			Attributes attr = manifest.getMainAttributes();
			String pkgBuildDate = attr.getValue("Build-Date");

			DateTimeFormatter pkgDateFormatter = DateTimeFormatter
					.ofPattern("yyyy-MM-dd HH:mm");

			date = LocalDateTime.parse(pkgBuildDate, pkgDateFormatter);
		}
		catch (Exception e) {
			// Dev mode.
			date = LocalDateTime.now();
		}

		return date;
	}
}
