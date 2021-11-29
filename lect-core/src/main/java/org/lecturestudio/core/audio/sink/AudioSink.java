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

package org.lecturestudio.core.audio.sink;

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Common interface to provide a consistent mechanism for writing audio samples.
 * Audio sinks are used, for example, by audio recorders to save the recorded
 * audio to a file.
 *
 * @author Alex Andres
 */
public interface AudioSink {

	/**
	 * Open the audio sink and prepare the sink to receive audio samples.
	 *
	 * @throws IOException If the audio sink could not be opened.
	 */
	void open() throws IOException;

	/**
	 * Reset the audio sink and drop all received audio samples.
	 *
	 * @throws IOException If the audio sink failed to reset the resources.
	 */
	void reset() throws IOException;

	/**
	 * Close the audio sink and release all previously assigned resources. Once
	 * this method returns, the sink will not receive any audio samples.
	 *
	 * @throws IOException If the audio sink could not be closed.
	 */
	void close() throws IOException;

	/**
	 * Receive the provided audio samples and write them to the implemented
	 * destination.
	 *
	 * @param data   The audio sample buffer.
	 * @param offset The location from which to start to read the samples in the
	 *               audio buffer.
	 * @param length The size in bytes of the audio buffer.
	 *
	 * @return the number of bytes written by the audio sink.
	 *
	 * @throws IOException If the sink fails to write the received audio
	 *                     samples.
	 */
	int write(byte[] data, int offset, int length) throws IOException;

	/**
	 * Get the audio format of audio samples for this sink.
	 *
	 * @return The audio format of samples to write.
	 */
	AudioFormat getAudioFormat();

	/**
	 * Sets the {@code AudioFormat} of samples which will be provided to this
	 * {@code AudioSink}.
	 *
	 * @param format The audio format of audio samples.
	 */
	void setAudioFormat(AudioFormat format);

}
