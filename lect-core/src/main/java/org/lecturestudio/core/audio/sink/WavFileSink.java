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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.WaveOutputStream;

/**
 * WAV file sink implementation.
 *
 * @author Alex Andres
 */
public class WavFileSink implements AudioSink {

	/** The file data channel to which to write the audio. */
	private final SeekableByteChannel channel;

	/** The WAV file stream that formats the audio file. */
	private WaveOutputStream stream;

	/** The audio format of samples to receive. */
	private AudioFormat format;


	/**
	 * Create a new WavFileSink with the specified file that should be created
	 * or overridden when the sink writes audio.
	 *
	 * @param file The file to which to write the audio.
	 *
	 * @throws IOException If the file could not be created.
	 */
	@SuppressWarnings("resource")
	public WavFileSink(File file) throws IOException {
		FileOutputStream fileStream = new FileOutputStream(file);
		this.channel = fileStream.getChannel();
	}

	/**
	 * Create a new WavFileSink with the specified data channel that receives
	 * the consumed audio samples.
	 *
	 * @param channel The data channel to write to.
	 */
	public WavFileSink(SeekableByteChannel channel) {
		this.channel = channel;
	}

	@Override
	public void open() throws IOException {
		stream = new WaveOutputStream(channel);
		stream.setAudioFormat(format);
	}

	@Override
	public void reset() throws IOException {
		stream.reset();
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public int write(byte[] data, int offset, int length) throws IOException {
		return stream.write(data, offset, length);
	}

	@Override
	public void setAudioFormat(AudioFormat format) {
		this.format = format;
	}

}
