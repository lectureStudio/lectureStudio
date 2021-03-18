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

package org.lecturestudio.core.app.configuration;

import java.io.File;
import java.io.IOException;

/**
 * Common interface to provide a consistent mechanism for loading and saving
 * application configurations that are stored in the operating systems file
 * system.
 *
 * @param <T> The type of the configuration.
 *
 * @author Alex Andres
 */
public interface ConfigurationService<T> {

	/**
	 * Loads a configuration of the specified class type from the specified
	 * file.
	 *
	 * @param file The file containing the configuration values.
	 * @param cls  The class of the configuration to create.
	 *
	 * @return an instance of the specified configuration type.
	 *
	 * @throws IOException If an fatal error occurred while loading the
	 *                     configuration file.
	 */
	T load(File file, Class<T> cls) throws IOException;

	/**
	 * Save a configuration object of the specified type to the specified file.
	 *
	 * @param file   The destination file of the configuration.
	 * @param config The configuration object.
	 *
	 * @throws IOException If an fatal error occurred while saving the
	 *                     configuration file.
	 */
	void save(File file, T config) throws IOException;

}
