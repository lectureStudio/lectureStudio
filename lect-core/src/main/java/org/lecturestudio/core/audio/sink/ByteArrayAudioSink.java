/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * AudioSink implementation which is backed by a {@code ByteArrayOutputStream}.
 *
 * @author Alex Andres
 */
public class ByteArrayAudioSink implements AudioSink {

	private ByteArrayOutputStream outputStream;

	private AudioFormat format;


	@Override
	public void open() throws IOException {
		outputStream = new ByteArrayOutputStream();
	}

	@Override
	public void reset() throws IOException {
		outputStream.reset();
	}

	@Override
	public void close() throws IOException {
		outputStream.close();
	}

	@Override
	public int write(byte[] data, int offset, int length) throws IOException {
		try {
			outputStream.write(data, 0, length);
		}
		catch (Exception e) {
			throw new IOException(e);
		}

		return length;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public void setAudioFormat(AudioFormat format) {
		this.format = format;
	}

	public byte[] toByteArray() {
		return outputStream.toByteArray();
	}
}
