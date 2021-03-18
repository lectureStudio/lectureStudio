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
 * Common class to provide a consistent mechanism for audio capture devices.
 *
 * @author Alex Andres
 */
public abstract class AudioInputDevice extends AudioDevice {

	/**
	 * Device specific method to be implemented to read captured audio data from
	 * the device into the specified audio buffer.
	 *
	 * @param buffer The audio buffer into which to write the captured audio.
	 * @param offset The offset at which to start to write the audio buffer.
	 * @param length The length of the audio buffer.
	 *
	 * @return the number of bytes written to the audio buffer.
	 *
	 * @throws Exception If captured audio could not be written to the buffer.
	 */
	abstract protected int readInput(byte[] buffer, int offset, int length) throws Exception;

	/**
	 * Read captured audio data from the device into the specified audio
	 * buffer.
	 *
	 * @param buffer The audio buffer into which to write the captured audio.
	 * @param offset The offset at which to start to write the audio buffer.
	 * @param length The length of the audio buffer.
	 *
	 * @return the number of bytes written to the audio buffer.
	 *
	 * @throws Exception If captured audio could not be written to the buffer.
	 */
	public synchronized int read(byte[] buffer, int offset, int length) throws Exception {
		int read = readInput(buffer, offset, length);

		if (!isMuted()) {
			applyGain(buffer, offset, length);
		}

		return read;
	}

}
