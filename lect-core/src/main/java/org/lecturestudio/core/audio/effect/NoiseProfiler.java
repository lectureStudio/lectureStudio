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
import org.lecturestudio.core.audio.analysis.DSP;

/**
 * Noise reduction audio effect implementation that creates a noise profile for
 * an audio stream.
 * <p>
 * The implementation is based on the SoX library (http://sox.sourceforge.net).
 *
 * @author Alex Andres
 */
public class NoiseProfiler implements AudioEffect {

	private static final int WINDOW_SIZE = 2048;

	private static final int HALF_WINDOW = (WINDOW_SIZE / 2);

	private static final int FREQUENCY_COUNT = (HALF_WINDOW + 1);

	/** An array of profiles for each individual audio channel. */
	private ChannelProfile[] channelProfiles;

	/** The number of buffered audio samples in bytes. */
	private int bufdata;

	/** The computed noise profile of the audio stream. */
	private float[] noiseProfile;


	@Override
	public void initialize(AudioEffectParameters parameters) {
		int channels = parameters.getFormat().getChannels();

		channelProfiles = new ChannelProfile[channels];

		for (int i = 0; i < channels; i++) {
			channelProfiles[i] = new ChannelProfile();
		}

		bufdata = 0;
	}

	@Override
	public void execute(Samples inputSamples, Samples outputSamples) {
		AudioFormat format = inputSamples.getFormat();

		int samples = inputSamples.getSampleCount();
		int channels = format.getChannels();
		int windowSamples = Math.min(samples / channels, WINDOW_SIZE - bufdata);

		float[] inSamples = inputSamples.getSamples();

		// Collect data for every channel.
		for (int i = 0; i < channels; i++) {
			ChannelProfile profile = channelProfiles[i];

			for (int j = 0; j < windowSamples; j++) {
				profile.window[j + bufdata] = inSamples[i + j * channels];
			}

			if (windowSamples + bufdata == WINDOW_SIZE) {
				collectData(profile);
			}
		}

		bufdata += windowSamples;

		assert (bufdata <= WINDOW_SIZE);

		if (bufdata == WINDOW_SIZE) {
			bufdata = 0;
		}
	}

	@Override
	public void flush(Samples outputSamples) {
		if (bufdata == 0) {
			return;
		}

		for (ChannelProfile profile : channelProfiles) {
			for (int j = bufdata + 1; j < WINDOW_SIZE; j++) {
				profile.window[j] = 0;
			}

			collectData(profile);
		}
	}

	@Override
	public void terminate() {
		for (ChannelProfile profile : channelProfiles) {
			noiseProfile = new float[FREQUENCY_COUNT];

			for (int j = 0; j < FREQUENCY_COUNT; j++) {
				float value = (profile.profileCount[j] != 0) ? profile.sum[j] / profile.profileCount[j] : 0;

				noiseProfile[j] = value;
			}
		}
	}

	/**
	 * Get the computed noise profile of the processed audio stream.
	 *
	 * @return the computed noise profile.
	 */
	float[] getNoiseProfile() {
		return noiseProfile;
	}

	/**
	 * Collect statistics from the complete window on provided channel.
	 *
	 * @param profile The channel profile.
	 */
	private void collectData(ChannelProfile profile) {
		float[] out = new float[FREQUENCY_COUNT];

		DSP.getPowerSpectrum(WINDOW_SIZE, profile.window, out);

		for (int i = 0; i < FREQUENCY_COUNT; i++) {
			if (out[i] > 0) {
				float power = (float) Math.log(out[i]);
				profile.sum[i] += power;
				profile.profileCount[i]++;
			}
		}
	}



	private static class ChannelProfile {

		float[] sum;

		float[] window;

		int[] profileCount;


		ChannelProfile() {
			sum = new float[FREQUENCY_COUNT];
			window = new float[WINDOW_SIZE];
			profileCount = new int[FREQUENCY_COUNT];
		}

	}
}
