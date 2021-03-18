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
import org.lecturestudio.core.model.Interval;

/**
 * Audio effect parameters are used to configure {@link AudioEffect}s describing
 * the desired result after processing.
 *
 * @author Alex Andres
 */
public class AudioEffectParameters {

	/** The desired audio format. */
	private AudioFormat format;

	/** The desired time interval to process. */
	private Interval<Long> timeInterval;


	/**
	 * Create a new uninitialized AudioEffectParameters instance.
	 */
	public AudioEffectParameters() {

	}

	/**
	 * Copy constructor of AudioEffectParameters.
	 *
	 * @param params The AudioEffectParameters from which to copy the parameters.
	 */
	public AudioEffectParameters(AudioEffectParameters params) {
		setFormat(params.getFormat());
		setTimeInterval(params.getTimeInterval());
	}

	/**
	 * Get the audio format.
	 *
	 * @return the audio format.
	 */
	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * Set the desired audio format.
	 *
	 * @param format The desired audio format.
	 */
	public void setFormat(AudioFormat format) {
		this.format = format;
	}

	/**
	 * Get the desired time interval to process.
	 *
	 * @return the time interval to process.
	 */
	public Interval<Long> getTimeInterval() {
		return timeInterval;
	}

	/**
	 * Set the desired time interval to process.
	 *
	 * @param timeInterval the time interval to process.
	 */
	public void setTimeInterval(Interval<Long> timeInterval) {
		this.timeInterval = timeInterval;
	}

	/**
	 * Create a copy of this AudioEffectParameters.
	 *
	 * @return copy of AudioEffectParameters.
	 */
	public AudioEffectParameters clone() {
		return new AudioEffectParameters(this);
	}

}
