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

package org.lecturestudio.core.audio.effect;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Audio samples container used for audio processing.
 *
 * @author Alex Andres
 *
 * @see AudioEffect
 */
public class Samples {

	/** The audio format of the samples. */
	private AudioFormat format;

	/** The audio samples. */
	private float[] samples;

	/** The number of audio samples. */
	private int sampleCount;


	/**
	 * Create a new Samples instance with the specified audio format and audio
	 * samples.
	 *
	 * @param format      The audio format of the samples.
	 * @param samples     The audio samples.
	 * @param sampleCount The number of audio samples.
	 */
	public Samples(AudioFormat format, float[] samples, int sampleCount) {
		setFormat(format);
		setSamples(samples);
		setSampleCount(sampleCount);
	}

	/**
	 * Get audio format of the samples.
	 *
	 * @return the audio format of the samples.
	 */
	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * Set audio format of the samples.
	 *
	 * @param format The audio format of the samples.
	 */
	public void setFormat(AudioFormat format) {
		this.format = format;
	}

	/**
	 * Get the audio samples.
	 *
	 * @return the audio samples.
	 */
	public float[] getSamples() {
		return samples;
	}

	/**
	 * Set the audio samples.
	 *
	 * @param samples The audio samples.
	 */
	public void setSamples(float[] samples) {
		this.samples = samples;
	}

	/**
	 * Get the number of audio samples.
	 *
	 * @return the number of audio samples.
	 */
	public int getSampleCount() {
		return sampleCount;
	}

	/**
	 * Set the number of audio samples.
	 *
	 * @param sampleCount The number of audio samples.
	 */
	public void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}

}
