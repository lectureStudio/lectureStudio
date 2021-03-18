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

package org.lecturestudio.core.audio.bus.event;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.bus.event.BusEvent;

/**
 * This event is published when audio data has been recorded by an audio capture
 * device.
 *
 * @author Alex Andres
 */
public class AudioRecordedEvent extends BusEvent {

	/** The audio format of the recorded audio data. */
	private AudioFormat audioFormat;

	/** The recorded audio data. */
	private byte[] data;


	/**
	 * Create a new AudioRecordedEvent instance with the specified audio data
	 * and audio format.
	 *
	 * @param data        The recorded audio data.
	 * @param audioFormat The audio format of the recorded audio data.
	 */
	public AudioRecordedEvent(byte[] data, AudioFormat audioFormat) {
		this.data = data;
		this.audioFormat = audioFormat;
	}

	/**
	 * Get the recorded audio data.
	 *
	 * @return the recorded audio data.
	 */
	public byte[] getAudioData() {
		return data;
	}

	/**
	 * Get the audio format.
	 *
	 * @return the audio format.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}
	
}
