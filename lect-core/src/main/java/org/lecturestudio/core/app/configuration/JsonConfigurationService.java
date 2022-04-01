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

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.lecturestudio.core.app.configuration.bind.AudioFormatDeserializer;
import org.lecturestudio.core.app.configuration.bind.AudioFormatSerializer;
import org.lecturestudio.core.app.configuration.bind.CalendarDeserializer;
import org.lecturestudio.core.app.configuration.bind.ColorDeserializer;
import org.lecturestudio.core.app.configuration.bind.ColorSerializer;
import org.lecturestudio.core.app.configuration.bind.Rectangle2DMixin;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableHashSet;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.util.ObservableSet;

/**
 * ConfigurationService implementation for loading and saving configuration
 * files in the JSON format.
 *
 * @param <T> The type of the configuration.
 *
 * @author Alex Andres
 */
public class JsonConfigurationService<T> implements ConfigurationService<T> {

	/**
	 * The object mapper that configures the conversion to and from the JSON
	 * format.
	 */
	private final ObjectMapper mapper;


	/**
	 * Create a new JsonConfigurationService instance.
	 */
	public JsonConfigurationService() {
		SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(ObservableList.class, ObservableArrayList.class);
		module.addAbstractTypeMapping(ObservableSet.class, ObservableHashSet.class);
		module.addSerializer(Color.class, new ColorSerializer());
		module.addSerializer(AudioFormat.class, new AudioFormatSerializer());
		module.addDeserializer(Color.class, new ColorDeserializer());
		module.addDeserializer(AudioFormat.class, new AudioFormatDeserializer());
		module.addDeserializer(Calendar.class, new CalendarDeserializer());
		module.setMixInAnnotation(Rectangle2D.class, Rectangle2DMixin.class);

		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.registerModules(new Jdk8Module(), new JavaTimeModule());
		mapper.registerModule(module);

		initModules(mapper);
	}

	@Override
	public T load(File file, Class<T> cls) throws IOException {
		T config;
		InputStream input = null;

		try {
			if (file.exists()) {
				input = new FileInputStream(file);
			}
			else {
				input = getClass().getResourceAsStream(file.getPath().replace("\\", "/"));
			}

			if (input == null) {
				throw new IOException("Unable to load configuration file. File does not exist.");
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
		File parent = file.getParentFile();

		if (nonNull(parent) && !parent.exists()) {
			parent.mkdirs();
		}

		mapper.writeValue(file, config);
	}

	/**
	 * Validate the provided configuration. Can be used in order to check for
	 * mandatory properties and set them accordingly.
	 *
	 * @param config The config to validate.
	 */
	public void validate(T config) {

	}

	/**
	 * Meant to be overridden by sub-classes to add custom modules to the object
	 * mapper.
	 *
	 * @param mapper The JSON object mapper.
	 */
	protected void initModules(ObjectMapper mapper) {

	}
}
