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

package org.lecturestudio.core.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class YamlSource<T> implements DataSource<T> {

	/**
	 * The object mapper that configures the conversion to and from the YAML
	 * format.
	 */
	private final ObjectMapper mapper;

	private List<T> list;


	/**
	 * Create a new YamlConfigurationService instance.
	 */
	public YamlSource(String resourcePath) throws IOException {
		mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		load(resourcePath);
	}

	@Override
	public List<T> getAll() {
		return list;
	}

	private void load(String resourcePath) throws IOException {
		list = new ArrayList<>();

		try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
			Class<T> cls = (Class<T>) ((ParameterizedType) getClass()
					.getGenericSuperclass()).getActualTypeArguments()[0];

			JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, cls);
			list = mapper.readValue(input, type);
		}
	}
}
