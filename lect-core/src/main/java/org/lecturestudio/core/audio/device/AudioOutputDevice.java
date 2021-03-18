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
 * Common class to provide a consistent mechanism for audio playback devices.
 *
 * @author Alex Andres
 */
public abstract class AudioOutputDevice extends AudioDevice {

	/**
	 * Device specific method to be implemented to write audio data for playback
	 * to the device from the specified audio buffer.
	 *
	 * @param buffer The audio buffer containing the samples for playback.
	 * @param offset The offset from which to start to read the audio buffer.
	 * @param length The length of the audio buffer.
	 *
	 * @return the number of bytes written to the playback buffer of the device.
	 *
	 * @throws Exception If the playback device did not accept the audio buffer.
	 */
	abstract public int writeOutput(byte[] buffer, int offset, int length) throws Exception;

	/**
	 * Write audio samples for playback.
	 *
	 * @param buffer The audio buffer containing the samples for playback.
	 * @param offset The offset from which to start to read the audio buffer.
	 * @param length The length of the audio buffer.
	 *
	 * @return the number of bytes written to the playback buffer of the device.
	 *
	 * @throws Exception If the playback device did not accept the audio buffer.
	 */
	public int write(byte[] buffer, int offset, int length) throws Exception {
		int written = length;

		if (!isMuted()) {
			applyGain(buffer, offset, length);
			written = writeOutput(buffer, offset, length);
		}

		return written;
	}

}
