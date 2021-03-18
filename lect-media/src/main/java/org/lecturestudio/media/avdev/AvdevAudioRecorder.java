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

import org.lecturestudio.avdev.AudioSink;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.audio.AudioFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default AVdevAudioRecorder implementation. The AVdev implementation has an
 * advantage over the default Java implementation, multiple AVdev recorder
 * instances can be used at the same time recording with different audio
 * formats.
 *
 * @author Alex Andres
 */
public class AvdevAudioRecorder {

	private static final Logger LOG = LogManager.getLogger(AvdevAudioRecorder.class);

	/** The audio capture device. */
	private final AVdevAudioInputDevice device;

	/** The state of the recorder. */
	private ExecutableState state = ExecutableState.Stopped;


	/**
	 * Create an AvdevAudioRecorder with the specified audio capture device.
	 *
	 * @param device The audio capture device.
	 */
	public AvdevAudioRecorder(AVdevAudioInputDevice device) {
		this.device = device;
	}

	/**
	 * Set the audio sink that will receive the captured audio samples.
	 *
	 * @param sink The audio sink to set.
	 */
	public void setSink(AudioSink sink) {
		device.setSink(sink);
	}

	/**
	 * Set the audio format with which the capture device should capture audio.
	 *
	 * @param format The audio format for the capture device.
	 */
	public void setAudioFormat(AudioFormat format) {
		device.setAudioFormat(format);
	}

	/**
	 * Set the recording audio volume. The value must be in the range of [0,1].
	 *
	 * @param volume The recording audio volume.
	 */
	public void setAudioVolume(double volume) {
		device.setVolume(volume);
	}

	/**
	 * Start capturing audio. If the recorder is already running, nothing
	 * happens.
	 */
	public void start() {
		if (state == ExecutableState.Started) {
			return;
		}

		try {
			if (state == ExecutableState.Stopped) {
				device.open();
			}

			device.start();

			setState(ExecutableState.Started);
		}
		catch (Exception e) {
			LOG.error("Start audio recorder failed", e);
		}
	}

	/**
	 * Pause audio capturing. The audio sink will not receive any audio data
	 * until the {@link #start()} method is called again.
	 */
	public void pause() {
		if (state != ExecutableState.Started) {
			return;
		}

		try {
			device.stop();

			setState(ExecutableState.Suspended);
		}
		catch (Exception e) {
			LOG.error("Pause audio recorder failed", e);
		}
	}

	/**
	 * Stop audio capturing. Once the recorder has stopped it will release all
	 * assigned resources.
	 */
	public void stop() {
		if (state == ExecutableState.Stopped) {
			return;
		}

		try {
			if (state == ExecutableState.Started) {
				device.stop();
			}

			device.close();

			setState(ExecutableState.Stopped);
		}
		catch (Exception e) {
			LOG.error("Stop audio recorder failed", e);
		}
	}

	private void setState(ExecutableState state) {
		this.state = state;
	}

}
