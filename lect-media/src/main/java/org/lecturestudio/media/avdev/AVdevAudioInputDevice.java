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

import org.lecturestudio.avdev.AudioCaptureDevice;
import org.lecturestudio.avdev.AudioOutputStream;
import org.lecturestudio.avdev.AudioSessionListener;
import org.lecturestudio.avdev.AudioSink;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.device.AudioInputDevice;

/**
 * AVdev audio capture device implementation.
 *
 * @author Alex Andres
 */
public class AVdevAudioInputDevice extends AudioInputDevice {

	/** Internal capture device. */
	private final AudioCaptureDevice device;

	/** Internal audio capture stream. */
	private AudioOutputStream stream;

	/** Internal audio sink. */
	private AudioSink sink;

	/** Internal session listener. */
	private AudioSessionListener sessionListener;


	/**
	 * Create a new AVdevAudioInputDevice instance with the specified AVdev
	 * capture device.
	 *
	 * @param device The AVdev capture device.
	 */
	public AVdevAudioInputDevice(AudioCaptureDevice device) {
		this.device = device;
	}

	/**
	 * Set the audio sink that receives the captured audio.
	 *
	 * @param sink The audio sink.
	 */
	public void setSink(AudioSink sink) {
		this.sink = sink;
	}

	/**
	 * Set the session listener to monitor the audio session state of this
	 * device.
	 *
	 * @param listener The audio session listener.
	 */
	public void setSessionListener(AudioSessionListener listener) {
		this.sessionListener = listener;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	@Override
	protected int readInput(byte[] buffer, int offset, int length) {
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
	public double getVolume() {
		if (nonNull(stream)) {
			return stream.getVolume();
		}

		return super.getVolume();
	}

	@Override
	public void open() throws Exception {
		if (nonNull(stream)) {
			stream.dispose();
			stream = null;
		}

		if (isNull(sink)) {
			throw new NullPointerException("No audio sink provided.");
		}

		AudioFormat audioFormat = getAudioFormat();
		org.lecturestudio.avdev.AudioFormat format = new org.lecturestudio.avdev.AudioFormat(
				org.lecturestudio.avdev.AudioFormat.SampleFormat.S16LE,
				audioFormat.getSampleRate(), audioFormat.getChannels());

		double volume = getVolume();
		boolean mute = isMuted();

		stream = device.createOutputStream(sink);

		if (nonNull(sessionListener)) {
			stream.attachSessionListener(sessionListener);
		}

		stream.setAudioFormat(format);
		stream.setBufferLatency(50);
		stream.setVolume((float) volume);
		stream.setMute(mute);
		stream.open();
	}

	@Override
	public void close() throws Exception {
		if (nonNull(stream)) {
			if (nonNull(sessionListener)) {
				stream.detachSessionListener(sessionListener);
			}

			stream.close();
			stream = null;
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
		return nonNull(stream);
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
