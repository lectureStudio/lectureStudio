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

package org.lecturestudio.broadcast.config;

import java.util.List;
import java.util.Map;

public class Configuration {

	/**
	 * The keystore configuration in order to use TLS enabled streams.
	 */
	public KeystoreConfiguration keystoreConfig;

	/**
	 * List of web-applications to start during the bootstrap process.
	 */
	public List<Map<String, String>> applications;

	/**
	 * The application server's port number.
	 */
	public int port;

	/**
	 * The application server's TLS port number.
	 */
	public int tlsPort;

	/**
	 * Indicator whether to use TLS.
	 */
	public boolean tlsEnabled;

	/**
	 * Indicator whether to redirect HTTP to HTTPS.
	 */
	public boolean redirectToHttps;

	/**
	 * The application server's web-root directory.
	 */
	public String baseDir;

}
