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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Java-based audio playback device implementation.
 *
 * @author Alex Andres
 */
public class JavaSoundOutputDevice extends AudioOutputDevice {

	/** Minimal audio buffer size to use with Java. */
	private static final int BUFFER_SIZE = 4096;

	/** Internal Mixer.Info that represents information about an audio mixer. */
	private final Mixer.Info mixerInfo;

	/** Internal playback sink. */
	private SourceDataLine line;


	/**
	 * Create a new JavaSoundOutputDevice instance with the specified {@code
	 * Mixer.Info} that contains information about an audio mixer.
	 *
	 * @param mixerInfo The audio mixer info.
	 */
	public JavaSoundOutputDevice(Mixer.Info mixerInfo) {
		this.mixerInfo = mixerInfo;
	}

	@Override
	public String getName() {
		return mixerInfo.getName();
	}

	@Override
	public void open() throws Exception {
		if (isNull(mixerInfo)) {
			throw new Exception("Invalid audio mixer set.");
		}

		Mixer mixer = AudioSystem.getMixer(mixerInfo);
		if (mixer == null) {
			throw new Exception("Could not acquire specified mixer: " + getName());
		}

		AudioFormat audioFormat = getAudioFormat();
		javax.sound.sampled.AudioFormat format = createAudioFormat(
				audioFormat.getSampleRate(), audioFormat.getChannels());

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

		line = (SourceDataLine) mixer.getLine(info);
		line.open(format, BUFFER_SIZE);
	}

	@Override
	public void close() throws Exception {
		if (nonNull(line)) {
			line.stop();
			line.flush();
			line.close();
		}
	}

	@Override
	public void start() {
		if (nonNull(line)) {
			line.start();
		}
	}

	@Override
	public void stop() {
		if (nonNull(line)) {
			line.stop();
		}
	}

	@Override
	public int writeOutput(byte[] buffer, int offset, int length) {
		return line.write(buffer, offset, length);
	}

	@Override
	public List<AudioFormat> getSupportedFormats() {
		AudioFormat.Encoding encoding = AudioFormat.Encoding.S16LE;
		int[] supportedChannels = { 1, 2 };

		List<AudioFormat> formats = new ArrayList<>();

		Mixer mixer = AudioSystem.getMixer(mixerInfo);
		DataLine.Info info;

		for (int channels : supportedChannels) {
			for (int sampleRate : AudioDevice.SUPPORTED_SAMPLE_RATES) {
				AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, channels);
				javax.sound.sampled.AudioFormat format = createAudioFormat(sampleRate, channels);

				info = new DataLine.Info(SourceDataLine.class, format);

				if (mixer.isLineSupported(info)) {
					formats.add(audioFormat);
				}
			}
		}

		return formats;
	}

	@Override
	public int getBufferSize() {
		if (nonNull(line)) {
			return line.getBufferSize();
		}

		return -1;
	}

	@Override
	public boolean isOpen() {
		return nonNull(line) && line.isOpen();
	}

	private javax.sound.sampled.AudioFormat createAudioFormat(int sampleRate, int channels) {
		javax.sound.sampled.AudioFormat.Encoding encoding = javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
		int sampleSizeInBits = 16;
		int frameSize = (sampleSizeInBits / 8) * channels;

		return new javax.sound.sampled.AudioFormat(
				encoding, sampleRate, sampleSizeInBits, channels, frameSize,
				sampleRate, false);
	}

}
