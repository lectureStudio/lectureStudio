/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.audio;

/**
 * Represents an audio frame containing audio samples with properties describing
 * the audio format.
 *
 * @author Alex Andres
 */
public class AudioFrame {

	private final byte[] data;

	private final int bitsPerSample;

	private final int sampleRate;

	private final int channels;

	private final int frames;


	/**
	 * Create a new {@code AudioFrame} with the specified data and properties
	 * describing the audio format.
	 *
	 * @param data          The audio samples.
	 * @param bitsPerSample The number of bits per sample.
	 * @param sampleRate    The sample rate.
	 * @param channels      The number of channels.
	 * @param frames        The number of audio frames.
	 */
	public AudioFrame(byte[] data, int bitsPerSample, int sampleRate,
			int channels, int frames) {
		this.data = data;
		this.bitsPerSample = bitsPerSample;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.frames = frames;
	}

	/**
	 * @return The audio samples.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return The number of bits per sample.
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * @return The sample rate.
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @return The number of channels.
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @return The number of audio frames.
	 */
	public int getFrames() {
		return frames;
	}
}
