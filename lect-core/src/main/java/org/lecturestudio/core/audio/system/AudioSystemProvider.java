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

import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.spi.ServiceProvider;

/**
 * Audio system provider implementation. The AudioSystemProvider provides access
 * implementation specific audio devices.
 *
 * @author Alex Andres
 */
public interface AudioSystemProvider extends ServiceProvider {

	/**
	 * Get the systems default audio capture device.
	 *
	 * @return The default audio capture device.
	 */
	AudioInputDevice getDefaultInputDevice();

	/**
	 * Get the systems default audio playback device.
	 *
	 * @return The default audio playback device.
	 */
	AudioOutputDevice getDefaultOutputDevice();

	/**
	 * Get all available audio capture devices.
	 *
	 * @return an array of all audio capture devices.
	 */
	AudioInputDevice[] getInputDevices();

	/**
	 * Get all available audio playback devices.
	 *
	 * @return an array of all audio playback devices.
	 */
	AudioOutputDevice[] getOutputDevices();

	/**
	 * Get an audio capture device with the specified device name.
	 *
	 * @param deviceName The name of the device to retrieve.
	 *
	 * @return an audio capture device.
	 */
	AudioInputDevice getInputDevice(String deviceName);

	/**
	 * Get an audio playback device with the specified device name.
	 *
	 * @param deviceName The name of the device to retrieve.
	 *
	 * @return an audio playback device.
	 */
	AudioOutputDevice getOutputDevice(String deviceName);

}
