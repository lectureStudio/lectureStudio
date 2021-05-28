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

package org.lecturestudio.presenter.api.recording;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.avdev.AudioCaptureDevice;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.media.avdev.AVdevAudioInputDevice;

/**
 * Dummy audio capture device implementation.
 *
 * @author Alex Andres
 */
public class DummyAudioInputDevice extends AVdevAudioInputDevice {

	/**
	 * Create a new DummyAudioInputDevice instance with the specified AVdev
	 * capture device.
	 *
	 * @param device The AVdev capture device.
	 */
	public DummyAudioInputDevice(AudioCaptureDevice device) {
		super(device);
	}

	@Override
	public String getName() {
		return "DummyCapture";
	}

	@Override
	protected int readInput(byte[] buffer, int offset, int length) {
		return 0;
	}

	@Override
	public void open() {
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isOpen() {
		return true;
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
