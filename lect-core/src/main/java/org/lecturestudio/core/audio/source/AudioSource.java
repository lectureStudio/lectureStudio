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

package org.lecturestudio.core.audio.source;

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Common interface to provide a consistent mechanism for reading audio samples.
 * Audio sources are used, for example, by audio players to play the audio from
 * a file.
 *
 * @author Alex Andres
 */
public interface AudioSource {

	/**
	 * Read audio samples from the source into the provided audio buffer.
	 *
	 * @param data   The audio buffer that receives the samples from the
	 *               source.
	 * @param offset The location to which to start to write the samples in the
	 *               buffer.
	 * @param length The size in bytes of the audio buffer.
	 *
	 * @return The number of bytes read from the audio source.
	 *
	 * @throws IOException If the audio source failed to read audio samples.
	 */
	int read(byte[] data, int offset, int length) throws IOException;

	/**
	 * Close the audio source and release all previously assigned resources.
	 * Once this method returns, the source will not be able to read any audio
	 * samples.
	 *
	 * @throws IOException If the audio source could not be closed.
	 */
	void close() throws IOException;

	/**
	 * Reset the audio source and drop all buffered audio samples. The source
	 * will reset the reading position to the initial start position.
	 *
	 * @throws IOException If the audio source failed to reset.
	 */
	void reset() throws IOException;

	/**
	 * Skip the specified number of bytes in the source.
	 *
	 * @param n The number of bytes to skip.
	 */
	long skip(long n) throws IOException;

	/**
	 * Get the number of bytes the audio source has available to read.
	 *
	 * @return the available number of bytes of the source.
	 *
	 * @throws IOException If the source size could not be established.
	 */
	long getInputSize() throws IOException;

	/**
	 * Get the audio format of audio samples in the source.
	 *
	 * @return The audio format of samples to read.
	 */
	AudioFormat getAudioFormat();

}
