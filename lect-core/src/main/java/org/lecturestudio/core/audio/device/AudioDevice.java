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

package org.lecturestudio.core.audio.device;

/**
 * Common class to provide a consistent interface to audio devices.
 *
 * @author Alex Andres
 */
public class AudioDevice {

	/** The name of this audio device. */
	private final String name;


	/**
	 * Creates a new {@code AudioDevice} with the specified name.
	 *
	 * @param name The name of the device.
	 */
	public AudioDevice(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the audio device assigned by the operating system.
	 *
	 * @return the name of the audio device.
	 */
	public String getName() {
		return name;
	}

}
