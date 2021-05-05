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

package org.lecturestudio.core.audio.codec;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SPI audio codec loader. To provide audio codecs the codec provider must
 * implement the {@link org.lecturestudio.core.audio.codec.AudioCodecProvider} interface and
 * be registered via the SPI.
 *
 * @author Alex Andres
 *
 * @link https://docs.oracle.com/javase/tutorial/ext/basics/spi.html
 */
public class AudioCodecLoader {

	/** Logger for {@link AudioCodecLoader} */
	private static final Logger LOG = LogManager.getLogger(AudioCodecLoader.class);

	/** The {@link AudioCodecLoader} singleton instance. */
	private static AudioCodecLoader service;

	/** The audio codec service loader. */
	private ServiceLoader<AudioCodecProvider> loader;


	/**
	 * Retrieve the singleton instance of {@link AudioCodecLoader}.
	 */
	public static synchronized AudioCodecLoader getInstance() {
		if (service == null) {
			service = new AudioCodecLoader();
		}
		return service;
	}

	/**
	 * Get a {@link AudioCodecProvider} with the specified name.
	 *
	 * @param providerName The name of the {@link AudioCodecProvider} to retrieve.
	 *
	 * @return the {@link AudioCodecProvider} or null, if no such provider exists.
	 */
	public AudioCodecProvider getProvider(String providerName) {
		try {
			for (AudioCodecProvider audioCodecProvider : loader) {
				if (audioCodecProvider.getProviderName().equals(providerName)) {
					return audioCodecProvider;
				}
			}
		}
		catch (ServiceConfigurationError error) {
			LOG.error("Get audio codec provider failed", error);
		}

		return null;
	}

	/**
	 * Retrieve all registered audio codec providers.
	 *
	 * @return an array of all audio codec providers.
	 */
	public AudioCodecProvider[] getProviders() {
		List<AudioCodecProvider> list = new ArrayList<>();

		try {
			for (AudioCodecProvider audioCodecProvider : loader) {
				list.add(audioCodecProvider);
			}
		}
		catch (ServiceConfigurationError error) {
			LOG.error("Get audio codec providers failed", error);
		}

		return list.toArray(new AudioCodecProvider[0]);
	}

	/**
	 * Retrieve the names of all registered audio codec providers.
	 *
	 * @return an array of names of all registered audio codec providers.
	 */
	public String[] getProviderNames() {
		List<String> names = new ArrayList<>();

		try {
			for (AudioCodecProvider audioCodecProvider : loader) {
				names.add(audioCodecProvider.getProviderName());
			}
		}
		catch (ServiceConfigurationError error) {
			LOG.error("Get audio codec provider names failed", error);
		}

		return names.toArray(new String[0]);
	}

	private AudioCodecLoader() {
		File[] dirs = new File[] { new File("lib") };
		List<URL> urls = new ArrayList<>();

		for (File dir : dirs) {
			if (dir.exists() && dir.isDirectory()) {
				List<URL> acc = new ArrayList<>();
				getLibs(dir, acc);

				urls.addAll(acc);
			}
		}

		URL[] libs = urls.toArray(new URL[0]);

		loader = ServiceLoader.load(AudioCodecProvider.class, getClassLoader(libs));
	}

	private ClassLoader getClassLoader(URL[] libs) {
		ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
		URLClassLoader urlClassLoader = new URLClassLoader(libs, currentThreadClassLoader);
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		return Thread.currentThread().getContextClassLoader();
	}

	private void getLibs(File path, List<URL> libs) {
		File[] files = path.listFiles();

		if (files == null) {
			return;
		}

		File file;

		for (File value : files) {
			file = value;

			if (file.isFile() && file.canRead() && file.getName().endsWith(".jar")) {
				try {
					libs.add(file.toURI().toURL());
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

			if (value.isDirectory()) {
				getLibs(value, libs);
			}
		}
	}
}
