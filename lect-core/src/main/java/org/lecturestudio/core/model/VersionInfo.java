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

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Version information container.
 *
 * @author Alex Andres
 */
public class VersionInfo {

	/**
	 * The download {@link URL} for the package.
	 */
	public URL downloadUrl;

	/**
	 * The website {@link URL}.
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
			version = getManifestValue("Package-Version");

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
			String pkgBuildDate = getManifestValue("Build-Date");

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

	private static String getManifestValue(String key) throws IOException {
		String value = null;
		Enumeration<URL> resources = VersionInfo.class.getClassLoader()
				.getResources(JarFile.MANIFEST_NAME);

		while (resources.hasMoreElements()) {
			try {
				Manifest manifest = new Manifest(resources.nextElement()
						.openStream());

				Attributes attr = manifest.getMainAttributes();
				value = attr.getValue(key);

				if (nonNull(value)) {
					break;
				}
			}
			catch (Exception e) {
				// Ignore.
			}
		}

		return value;
	}
}
