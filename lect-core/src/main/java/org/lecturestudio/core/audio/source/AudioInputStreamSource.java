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
import java.io.InputStream;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Audio input stream implementation of AudioSource.
 *
 * @author Alex Andres
 */
public class AudioInputStreamSource implements AudioSource {

	/** The audio input stream. */
	private final InputStream audioStream;

	/** The audio format of the input stream. */
	private final AudioFormat audioFormat;


	/**
	 * Create a AudioInputStreamSource with the specified audio input stream and
	 * its audio format.
	 *
	 * @param audioStream The audio input stream.
	 * @param audioFormat The audio format of the input stream.
	 */
	public AudioInputStreamSource(InputStream audioStream, AudioFormat audioFormat) {
		this.audioStream = audioStream;
		this.audioFormat = audioFormat;
	}

	@Override
	public int read(byte[] data, int offset, int length) throws IOException {
		return audioStream.read(data, offset, length);
	}

	@Override
	public void close() throws IOException {
		audioStream.close();
	}

	@Override
	public void reset() throws IOException {
		if (audioStream.markSupported()) {
			audioStream.reset();

			// Skip audio header.
			audioStream.skip(128);
		}
	}

	@Override
	public long getInputSize() throws IOException {
		return audioStream.available();
	}

	@Override
	public long skip(long n) throws IOException {
		return audioStream.skip(n);
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

}
