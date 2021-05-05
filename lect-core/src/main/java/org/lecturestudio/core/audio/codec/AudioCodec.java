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

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.audio.AudioFormat;

/**
 * Base audio codec implementation to provide a consistent mechanism to encode
 * or decode audio data.
 *
 * @author Alex Andres
 */
public abstract class AudioCodec extends ExecutableBase {

	/** The audio format of the audio codec. */
	private AudioFormat format;


	/**
	 * Process the audio input data with the implemented audio codec.
	 *
	 * @param input     The audio input data to process.
	 * @param length    The length of the audio input data.
	 * @param timestamp The current timestamp the specified audio data.
	 *
	 * @throws Exception If an fatal error occurred preventing the processed
	 *                   audio data to be used.
	 */
	abstract public void process(byte[] input, int length, long timestamp) throws Exception;


	/**
	 * Set the audio format of the audio data this codec has to process.
	 *
	 * @param format The audio format.
	 */
	public void setFormat(AudioFormat format) {
		this.format = format;
	}

	/**
	 * Get the audio format of the audio codec.
	 *
	 * @return the audio format.
	 */
	public AudioFormat getFormat() {
		return format;
	}

}
