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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ConfigurationService implementation for loading and saving configuration
 * files in the YAML format.
 *
 * @param <T> The type of the configuration.
 *
 * @author Alex Andres
 */
public class YamlConfigurationService<T> implements ConfigurationService<T> {

	/**
	 * The object mapper that configures the conversion to and from the YAML
	 * format.
	 */
	private final ObjectMapper mapper;


	/**
	 * Create a new {@link YamlConfigurationService} instance.
	 */
	public YamlConfigurationService() {
		mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public T load(File file, Class<T> cls) throws IOException {
		T config = null;
		InputStream input = null;

		try {
			if (file.exists()) {
				input = new FileInputStream(file);
			}
			else {
				input = getClass().getResourceAsStream(file.getPath().replace("\\", "/"));
			}

			if (input == null) {
				throw new IOException("Unable to load configuration file. File '" + file.getAbsolutePath() + "' does not exist.");
			}

			config = mapper.readValue(input, cls);
		}
		finally {
			if (input != null) {
				input.close();
			}
		}

		return config;
	}

	@Override
	public void save(File file, T config) throws IOException {
		mapper.writeValue(file, config);
	}
}
