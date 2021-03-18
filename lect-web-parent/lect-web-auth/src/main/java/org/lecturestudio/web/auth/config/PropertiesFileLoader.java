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

package org.lecturestudio.web.auth.config;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class PropertiesFileLoader {

	private final Properties prop;


	public PropertiesFileLoader() {
		this.prop = new Properties();
	}

	public Set<String> getKeys() {
		return prop.stringPropertyNames();
	}

	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	public void load(String file) throws IOException {
		InputStream input = null;

		try {
			input = this.getClass().getResourceAsStream(file);

			prop.load(input);
		}
		finally {
			if (nonNull(input)) {
				try {
					input.close();
				}
				catch (IOException e) {
					// Ignore
				}
			}
		}
	}

}
