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

package org.lecturestudio.media.audio;

import org.lecturestudio.core.audio.AudioFormat;

public class Spectrogram {

	private final AudioFormat audioFormat;

	private final double[][] data;

	private final long frameLength;

	private final int width;

	private final int height;


	public Spectrogram(AudioFormat audioFormat, long frameLength, int width, int height, double[][] data) {
		this.audioFormat = audioFormat;
		this.frameLength = frameLength;
		this.width = width;
		this.height = height;
		this.data = data;
	}

	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public long getFrameLength() {
		return frameLength;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public double[][] getData() {
		return data;
	}
}
