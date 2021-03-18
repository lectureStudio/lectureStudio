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

/**
 * Common interface to provide a consistent mechanism for audio effect
 * processing.
 *
 * @author Alex Andres
 */
public interface AudioEffect {

	/**
	 * Prepare the audio effect for execution. This method should perform any
	 * initialization with the specified parameters required post object
	 * creation.
	 *
	 * @param parameters The effect parameters.
	 */
	void initialize(AudioEffectParameters parameters);

	/**
	 * Execute the audio effect and start processing the provided samples.
	 *
	 * @param inputSamples  The audio input samples.
	 * @param outputSamples The audio output samples.
	 */
	void execute(Samples inputSamples, Samples outputSamples);

	/**
	 * Flush the remaining audio samples the audio effect may have buffered.
	 *
	 * @param outputSamples The buffered audio samples of this audio effect.
	 */
	void flush(Samples outputSamples);

	/**
	 * Terminate the processing and release all assigned resources.
	 */
	void terminate();

}
