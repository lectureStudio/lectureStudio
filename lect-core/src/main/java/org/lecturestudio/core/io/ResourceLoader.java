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

package org.lecturestudio.core.io;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class ResourceLoader {

	/**
	 * Finds a resource of the specified name from the search path.
	 * 
	 * @param path The resource path.
	 * 
	 * @return A {@link URL} for reading the resource, or {@code null} if the resource could not be found.
	 */
	public static URL getResourceURL(String path) {
		return ClassLoader.getSystemResource(path);
	}

	/** @see java.lang.ClassLoader#getResourceAsStream(String)  */
	public static InputStream getResourceAsStream(String path) {
		return ResourceLoader.class.getClassLoader().getResourceAsStream(path);
	}

	/**
	 * Indicates whether the specified {@link URL} has the jar protocol.
	 *
	 * @param url The {@link URL}.
	 * @return {@code true} if the protocol of the specified {@link URL} is the jar protocol, otherwise {@code false}.
	 */
	public static boolean isJarResource(URL url) {
		return url.getProtocol().equals("jar");
	}
	
	public static String getJarPath(Class<?> cls) throws URISyntaxException {
		return cls.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
	}
	
}
