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

package org.lecturestudio.core.audio;

import org.lecturestudio.core.audio.device.AudioDevice;

/**
 * Audio system provider implementation. This {@code AudioSystemProvider}
 * provides access to all connected audio devices, such as microphones and
 * speakers.
 *
 * @author Alex Andres
 */
public interface AudioSystemProvider {

	/**
	 * Adds a new device change listener that receives events each time an audio
	 * device is connected or disconnected to or from the system.
	 *
	 * @param listener The device change listener to register.
	 */
	void addDeviceChangeListener(AudioDeviceChangeListener listener);

	/**
	 * Removes a device change listener from event observation when an audio
	 * device is connected or disconnected to or from the system.
	 *
	 * @param listener The device change listener to unregister.
	 */
	void removeDeviceChangeListener(AudioDeviceChangeListener listener);

	/**
	 * Get the systems default audio recording device.
	 *
	 * @return The default audio recording device.
	 */
	AudioDevice getDefaultRecordingDevice();

	/**
	 * Get the systems default audio playback device.
	 *
	 * @return The default audio playback device.
	 */
	AudioDevice getDefaultPlaybackDevice();

	/**
	 * Get all available audio recording devices.
	 *
	 * @return An array of all audio recording devices.
	 */
	AudioDevice[] getRecordingDevices();

	/**
	 * Get all available audio playback devices.
	 *
	 * @return An array of all audio playback devices.
	 */
	AudioDevice[] getPlaybackDevices();

	/**
	 * Creates an audio player based on this provider internal implementation.
	 *
	 * @return A new audio player.
	 */
	AudioPlayer createAudioPlayer();

	/**
	 * Creates an audio recorder based on this provider internal
	 * implementation.
	 *
	 * @return A new audio recorder.
	 */
	AudioRecorder createAudioRecorder();

	/**
	 * Get the implementing service provider's name.
	 *
	 * @return the name of the service provider.
	 */
	String getProviderName();

}
