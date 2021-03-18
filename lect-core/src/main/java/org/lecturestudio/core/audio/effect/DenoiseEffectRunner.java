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

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.source.RandomAccessAudioSource;

/**
 * Noise reduction audio effect implementation.
 *
 * @author Alex Andres
 */
public class DenoiseEffectRunner extends AudioEffectRunner {

	/**
	 * Create an DenoiseEffectRunner with the with the desired output parameters
	 * and the audio source and sink.
	 *
	 * @param params The audio output parameters.
	 * @param source The audio source from which to read the audio samples.
	 * @param sink   The audio sink to which to write the processed audio
	 *               samples.
	 */
	public DenoiseEffectRunner(NoiseReductionParameters params, RandomAccessAudioSource source, AudioSink sink) {
		super(params, source, sink);
	}

	@Override
	protected void runEffect() throws IOException {
		NoiseReductionParameters noiseParams = (NoiseReductionParameters) params;
		AudioFormat audioFormat = source.getAudioFormat();

		// Start profiling phase.
		AudioEffectParameters profileParams = new AudioEffectParameters();
		profileParams.setFormat(audioFormat);
		profileParams.setTimeInterval(noiseParams.getProfileTimeInterval());

		RandomAccessAudioSource raSource = (RandomAccessAudioSource) source;

		// Profile only the given time span from the source.
		raSource.addExclusiveMillis(noiseParams.getProfileTimeInterval());

		NoiseProfiler profiler = new NoiseProfiler();

		runEffect(profiler, profileParams, null);

		if (!isRendering()) {
			// Quit, if aborted during the profiling phase.
			return;
		}

		// Run noise reduction phase on full input.
		raSource.removeExclusiveMillis(noiseParams.getProfileTimeInterval());

		// Start noise reduction phase.
		NoiseReductionParameters reductionParams = new NoiseReductionParameters();
		reductionParams.setFormat(audioFormat);
		reductionParams.setProfile(profiler.getNoiseProfile());
		reductionParams.setThreshold(noiseParams.getThreshold());
		reductionParams.setTimeInterval(noiseParams.getTimeInterval());

		NoiseReduction reduction = new NoiseReduction();

		runEffect(reduction, reductionParams, sink);
	}

}
