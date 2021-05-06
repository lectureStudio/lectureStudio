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

import org.lecturestudio.core.model.Interval;

/**
 * Noise reduction parameters implementation used to configure the {@link
 * NoiseReduction} audio effect.
 *
 * @author Alex Andres
 *
 * @see NoiseProfiler
 * @see NoiseReduction
 */
public class NoiseReductionParameters extends AudioEffectParameters {

	/** The user-defined frequency threshold. */
	private float threshold;

	/** The computed noise profile of the audio stream. */
	private float[] profile;

	/** The time interval in the audio stream in which to create the noise profile. */
	private Interval<Long> profileTimeInterval;


	/**
	 * Create a new uninitialized {@link NoiseReductionParameters} instance.
	 */
	public NoiseReductionParameters() {

	}

	/**
	 * Copy constructor of {@link NoiseReductionParameters}.
	 *
	 * @param params The {@link NoiseReductionParameters} from which to copy the
	 *               parameters.
	 */
	public NoiseReductionParameters(NoiseReductionParameters params) {
		super(params);

		setProfile(params.getProfile());
		setProfileTimeInterval(params.getProfileTimeInterval());
		setThreshold(params.getThreshold());
	}

	/**
	 * Get the user-defined frequency threshold.
	 *
	 * @return the user-defined frequency threshold.
	 */
	public float getThreshold() {
		return threshold;
	}

	/**
	 * Set the user-defined frequency threshold. The threshold value must be in
	 * the range of [0,1]. The higher the frequency threshold the more
	 * frequencies will be suppressed during the noise reduction.
	 *
	 * @param threshold The user-defined frequency threshold.
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	/**
	 * Get the computed noise profile of the audio stream.
	 *
	 * @return the computed noise profile.
	 */
	public float[] getProfile() {
		return profile;
	}

	/**
	 * Set the computed noise profile of the audio stream.
	 *
	 * @param profile The computed noise profile.
	 */
	public void setProfile(float[] profile) {
		this.profile = profile;
	}

	/**
	 * Get the time interval in the audio stream in which to create the noise
	 * profile.
	 *
	 * @return the time interval for noise profile processing.
	 */
	public Interval<Long> getProfileTimeInterval() {
		return profileTimeInterval;
	}

	/**
	 * Set the time interval in the audio stream in which to create the noise
	 * profile. The audio interval should contain mostly audio with noise,
	 * silence with noise, for the best noise reduction result.
	 *
	 * @param interval The time interval for noise profile processing.
	 */
	public void setProfileTimeInterval(Interval<Long> interval) {
		this.profileTimeInterval = interval;
	}

	@Override
	public NoiseReductionParameters clone() {
		return new NoiseReductionParameters(this);
	}

}
