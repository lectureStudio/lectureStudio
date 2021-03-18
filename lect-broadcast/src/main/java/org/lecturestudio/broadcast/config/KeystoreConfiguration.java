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

public class KeystoreConfiguration {

	/**
	 * The alias used to for the server certificate in the keystore.
	 * If not specified, the first alias in the keystore will be used.
	 */
	public String keyAlias;

	/**
	 * The pathname of the keystore file where the server certificate
	 * is stored. If undefined, the default path is '.keystore' in the
	 * users home directory who is running the server. If the specified
	 * keystore could not be loaded, the default packed keystore will
	 * be used.
	 */
	public String keystorePath;

	/**
	 * The type of the keystore file where the server certificate is
	 * stored. The default value is 'JKS', if not specified.
	 */
	public String keystoreType;

	/**
	 * The password used to access the server certificate from the
	 * specified keystore file.
	 */
	public String keystorePassword;

}
