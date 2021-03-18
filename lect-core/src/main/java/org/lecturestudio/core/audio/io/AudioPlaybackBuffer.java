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

package org.lecturestudio.core.audio.io;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.PlaybackBuffer;
import org.lecturestudio.core.util.AudioUtils;

/**
 * Audio PlaybackBuffer implementation used by streaming audio players.
 *
 * @author Alex Andres
 */
public class AudioPlaybackBuffer extends PlaybackBuffer<byte[]> {

	/** The audio format of the enqueued audio samples. */
	private AudioFormat audioFormat;


	/**
	 * Get the audio format of the enqueued audio samples.
	 *
	 * @return the audio format.
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	/**
	 * Set the audio format of the enqueued audio samples.
	 *
	 * @param format The audio format of the enqueued audio samples.
	 */
	public void setAudioFormat(AudioFormat format) {
		this.audioFormat = format;
	}

	/**
	 * Skip audio of the specified time in milliseconds.
	 *
	 * @param time The time in milliseconds to skip.
	 */
	public void skip(int time) {
		float bytesPerSecond = AudioUtils.getBytesPerSecond(audioFormat);
		int skipBytes = Math.round(bytesPerSecond * time / 1000F);

		// TODO: skip buffer data
	}

}
