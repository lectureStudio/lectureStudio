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

import com.github.javaffmpeg.Audio;
import com.github.javaffmpeg.AudioFrame;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * FFmpeg audio capture device implementation.
 *
 * @author Alex Andres
 */
public class FFmpegAudioInputDevice extends AudioInputDevice {

	/** Internal capture device. */
	private final com.github.javaffmpeg.FFmpegAudioInputDevice device;


	/**
	 * Create a new {@link FFmpegAudioInputDevice} instance with the specified FFmpeg
	 * capture device.
	 *
	 * @param device The FFmpeg capture device.
	 */
	public FFmpegAudioInputDevice(com.github.javaffmpeg.FFmpegAudioInputDevice device) {
		this.device = device;
	}

	@Override
	protected synchronized int readInput(byte[] buffer, int offset, int length) {
		if (isOpen()) {
			AudioFrame samples = device.getSamples();
			// TODO get by sample format
			Audio.getAudio16(samples, buffer);
			samples.clear();

			return length;
		}

		return 0;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	@Override
	public synchronized void open() throws Exception {
		if (isOpen()) {
			return;
		}

		AudioFormat audioFormat = getAudioFormat();

		com.github.javaffmpeg.AudioFormat format = new com.github.javaffmpeg.AudioFormat();
		format.setChannelLayout(Audio.getChannelLayout(audioFormat.getChannels()));
		format.setChannels(audioFormat.getChannels());
		format.setSampleFormat(Audio.getSampleFormat(audioFormat.getBytesPerSample(), false, false));
		format.setSampleRate(audioFormat.getSampleRate());

		device.setBufferMilliseconds(20);
		device.open(format);
	}

	@Override
	public synchronized void close() throws Exception {
		device.close();
	}

	@Override
	public synchronized void start() {
		// nothing to do
	}

	@Override
	public synchronized void stop() {
		// nothing to do
	}

	@Override
	public synchronized boolean isOpen() {
		return device.isOpen();
	}

	@Override
	public List<AudioFormat> getSupportedFormats() {
		AudioFormat.Encoding encoding = AudioFormat.Encoding.S16LE;
		int channels = 1;

		List<AudioFormat> formats = new ArrayList<AudioFormat>();

		for (int sampleRate : AudioDevice.SUPPORTED_SAMPLE_RATES) {
			AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, channels);

			formats.add(audioFormat);
		}

		return formats;
	}

	@Override
	public int getBufferSize() {
		return device.getBufferSize();
	}

}
