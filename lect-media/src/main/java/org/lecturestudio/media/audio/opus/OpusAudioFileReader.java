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

package org.lecturestudio.media.audio.opus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

/**
 * Opus audio file reading implementation. This reader can parse the format
 * information from the Opus audio stream, and can produce audio input streams
 * to be used for playback.
 *
 * @author Alex Andres
 */
public class OpusAudioFileReader extends AudioFileReader {

	@Override
	public AudioFileFormat getAudioFileFormat(InputStream stream)
			throws UnsupportedAudioFileException {
		return getStreamFormat(stream);
	}

	@Override
	public AudioFileFormat getAudioFileFormat(URL url)
			throws UnsupportedAudioFileException, IOException {
		try (InputStream inputStream = url.openStream()) {
			return getStreamFormat(inputStream);
		}
	}

	@Override
	public AudioFileFormat getAudioFileFormat(File file)
			throws UnsupportedAudioFileException, IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return getStreamFormat(inputStream);
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		return getInputStream(stream);
	}

	@Override
	public AudioInputStream getAudioInputStream(URL url)
			throws UnsupportedAudioFileException, IOException {
		InputStream stream = url.openStream();

		try {
			return getInputStream(stream);
		}
		catch (UnsupportedAudioFileException | IOException e) {
			stream.close();
			throw e;
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(File file)
			throws UnsupportedAudioFileException, IOException {
		InputStream stream = new FileInputStream(file);

		try {
			return getInputStream(stream);
		}
		catch (UnsupportedAudioFileException | IOException e) {
			stream.close();
			throw e;
		}
	}

	protected AudioFileFormat getStreamFormat(InputStream stream)
			throws UnsupportedAudioFileException {
		OpusStream opusStream;

		try {
			opusStream = new OpusStream(stream);
		}
		catch (IOException e) {
			throw new UnsupportedAudioFileException(e.getMessage());
		}

		return getAudioFileFormat(opusStream);
	}

	protected AudioInputStream getInputStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		OpusStream opusStream;

		try {
			opusStream = new OpusStream(stream);
		}
		catch (IOException e) {
			if (stream.markSupported()) {
				stream.reset();
			}

			throw new UnsupportedAudioFileException(e.getMessage());
		}

		AudioFileFormat format = getAudioFileFormat(opusStream);

		return new OpusInputStream(opusStream, format.getFormat(),
				format.getFrameLength());
	}

	private AudioFileFormat getAudioFileFormat(OpusStream opus) {
		AudioFormat format = new AudioFormat(OpusEncoding.OPUS,
				opus.getInfo().getSampleRate(), -1,
				opus.getInfo().getNumChannels(), -1, -1, true);

		return new AudioFileFormat(OpusFileFormatType.OPUS, format, -1);
	}
}
