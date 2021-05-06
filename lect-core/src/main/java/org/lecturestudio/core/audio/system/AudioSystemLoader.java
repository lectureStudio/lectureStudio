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

package org.lecturestudio.core.audio.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lecturestudio.core.spi.SpiLoader;

/**
 * SPI audio system loader. To provide different audio system implementations
 * the system provider must implement the {@link AudioSystemProvider} interface
 * and be registered via the SPI.
 *
 * @author Alex Andres
 * @see <a href= "https://docs.oracle.com/javase/tutorial/ext/basics/spi.html">
 *     https://docs.oracle.com/javase/tutorial/ext/basics/spi.html</a>
 */
public class AudioSystemLoader extends SpiLoader<AudioSystemProvider> {

	/** The audio system loader. */
	private static AudioSystemLoader serviceLoader;

	/** The Java-based sound system provided. */
	private final AudioSystemProvider javaSoundProvider = new JavaSoundProvider();


	/**
	 * Retrieve the singleton instance of {@link AudioSystemLoader}.
	 */
	public static synchronized AudioSystemLoader getInstance() {
		if (serviceLoader == null) {
			serviceLoader = new AudioSystemLoader();
		}
		return serviceLoader;
	}

	/**
	 * Get an {@link AudioSystemProvider} with the specified name.
	 *
	 * @param providerName The name of the {@link AudioSystemProvider} to retrieve.
	 *
	 * @return the {@link AudioSystemProvider} or null, if no such provider exists.
	 */
	public AudioSystemProvider getProvider(String providerName) {
		if (javaSoundProvider.getProviderName().equals(providerName)) {
			return javaSoundProvider;
		}

		return super.getProvider(providerName);
	}

	/**
	 * Retrieve the names of all registered audio system providers.
	 *
	 * @return an array of names of all registered audio system providers.
	 */
	public String[] getProviderNames() {
		List<String> names = new ArrayList<>();
		Collections.addAll(names, super.getProviderNames());
		names.add(javaSoundProvider.getProviderName());

		return names.toArray(new String[0]);
	}

	private AudioSystemLoader() {
		super(new File[] { new File("lib"), new File("../../lib") },
				"org.lecturestudio.core.audio.system", AudioSystemProvider.class);
	}
}
