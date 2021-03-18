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

package org.lecturestudio.media.avdev;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.avdev.AudioInputStream;
import org.lecturestudio.avdev.AudioPlaybackDevice;
import org.lecturestudio.avdev.AudioSource;
import org.lecturestudio.avdev.StreamListener;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;

/**
 * AVdev audio playback device implementation.
 *
 * @author Alex Andres
 */
public class AVdevAudioOutputDevice extends AudioOutputDevice {

	/** Internal playback device. */
	private final AudioPlaybackDevice device;

	/** Internal audio playback stream. */
	private AudioInputStream stream;

	/** Internal audio source. */
	private AudioSource source;

	/** Internal stream listener. */
	private StreamListener streamListener;


	/**
	 * Create a new AVdevAudioOutputDevice instance with the specified AVdev
	 * playback device.
	 *
	 * @param device The AVdev playback device.
	 */
	public AVdevAudioOutputDevice(AudioPlaybackDevice device) {
		this.device = device;
	}

	/**
	 * Set the audio source from which to read the audio samples to be played.
	 *
	 * @param source The audio source.
	 */
	public void setSource(AudioSource source) {
		this.source = source;
	}

	/**
	 * Set the stream listener to monitor the stream state of this device.
	 *
	 * @param listener The stream listener.
	 */
	public void setStreamListener(StreamListener listener) {
		this.streamListener = listener;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	@Override
	public int writeOutput(byte[] buffer, int offset, int length) {
		return 0;
	}

	@Override
	public void setVolume(double volume) {
		super.setVolume(volume);

		if (nonNull(stream)) {
			stream.setVolume((float) volume);
		}
	}

	@Override
	public void open() throws Exception {
		if (nonNull(stream)) {
			stream.dispose();
			stream = null;
		}

		if (isNull(source)) {
			throw new NullPointerException("No audio source provided.");
		}

		AudioFormat audioFormat = getAudioFormat();
		org.lecturestudio.avdev.AudioFormat format = new org.lecturestudio.avdev.AudioFormat(
				org.lecturestudio.avdev.AudioFormat.SampleFormat.S16LE,
				audioFormat.getSampleRate(), audioFormat.getChannels());

		stream = device.createInputStream(source);
		stream.setAudioFormat(format);
		stream.setVolume((float) getVolume());

		if (nonNull(streamListener)) {
			stream.attachStreamListener(streamListener);
		}

		stream.open();
	}

	@Override
	public void close() throws Exception {
		if (nonNull(stream)) {
			stream.close();
		}
	}

	@Override
	public void start() throws Exception {
		if (nonNull(stream)) {
			stream.start();
		}
	}

	@Override
	public void stop() throws Exception {
		if (nonNull(stream)) {
			stream.stop();
		}
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public List<AudioFormat> getSupportedFormats() {
		AudioFormat.Encoding encoding = AudioFormat.Encoding.S16LE;
		int channels = 1;

		List<AudioFormat> formats = new ArrayList<>();

		for (int sampleRate : AudioDevice.SUPPORTED_SAMPLE_RATES) {
			formats.add(new AudioFormat(encoding, sampleRate, channels));
		}

		return formats;
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

}
