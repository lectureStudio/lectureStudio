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

import org.lecturestudio.core.audio.analysis.DSP;
import org.lecturestudio.core.audio.analysis.HannWindowFunction;
import org.lecturestudio.core.audio.analysis.WindowFunction;

/**
 * Noise reduction audio effect implementation that performs noise reduction on
 * an audio stream. The noise reduction of audio is an two-step process. First
 * the frequency profile of the audio stream needs to be created, then the noise
 * reduction algorithm is applied on the audio stream using the collected
 * frequencies to filter out the frequencies that cause the noise.
 * <p>
 * The first step would be running the {@link NoiseProfiler} and then running
 * this NoiseReduction.
 * <p>
 * The implementation is based on the SoX library (http://sox.sourceforge.net).
 *
 * @author Alex Andres
 */
public class NoiseReduction implements AudioEffect {

	private static final int WINDOW_SIZE = 2048;

	private static final int HALF_WINDOW = (WINDOW_SIZE / 2);

	private static final int FREQUENCY_COUNT = (HALF_WINDOW + 1);

	/** An array of profiles for each individual audio channel. */
	private ChannelData[] data;

	/** The number of buffered audio samples in bytes. */
	private int bufdata;

	/** The frequency filter threshold for noise reduction. */
	private float threshold;

	/** The remaining audio samples that may need to be flushed when finished. */
	private Samples lastInSamples;

	/** The window function to apply while filtering audio. */
	private WindowFunction windowFunction;


	@Override
	public void initialize(AudioEffectParameters parameters) {
		windowFunction = new HannWindowFunction(WINDOW_SIZE);

		NoiseReductionParameters params = (NoiseReductionParameters) parameters;

		threshold = params.getThreshold();
		float[] profile = params.getProfile();

		int channels = parameters.getFormat().getChannels();

		data = new ChannelData[channels];

		for (int i = 0; i < channels; i++) {
			data[i] = new ChannelData();
		}

		bufdata = 0;

		for (int i = 0; i < channels; i++) {
			ChannelData channelData = data[i];

			System.arraycopy(profile, 0, channelData.noisegate, 0, FREQUENCY_COUNT);
		}
	}

	@Override
	public void execute(Samples inputSamples, Samples outputSamples) {
		int samples = Math.min(inputSamples.getSampleCount(), outputSamples.getSampleCount());
		int channels = inputSamples.getFormat().getChannels();
		int channelSamples = samples / channels;
		int ncopy = Math.min(channelSamples, WINDOW_SIZE - bufdata);
		int oldbuf = bufdata;
		boolean wholeWindow = (ncopy + bufdata == WINDOW_SIZE);

		if (wholeWindow) {
			bufdata = HALF_WINDOW;
		}
		else {
			bufdata += ncopy;
		}

		float[] inSamples = inputSamples.getSamples();
		float[] outSamples = outputSamples.getSamples();

		lastInSamples = inputSamples;

		// Reduce noise on every channel.
		for (int i = 0; i < channels; i++) {
			ChannelData channelData = data[i];

			if (channelData.window == null) {
				channelData.window = new float[WINDOW_SIZE];
			}

			for (int j = 0; j < ncopy; j++) {
				channelData.window[oldbuf + j] = inSamples[i + channels * j];
			}

			if (!wholeWindow) {
				continue;
			}
			else {
				processWindow(i, channels, outSamples, (oldbuf + ncopy));
			}
		}

		if (wholeWindow) {
			outputSamples.setSampleCount(channels * HALF_WINDOW);
		}
		else {
			outputSamples.setSampleCount(0);
		}
	}

	@Override
	public void flush(Samples outputSamples) {
		int channels = data.length;
		int samples = 0;
		int flushed = 0;

		float[] outputBuffer = outputSamples.getSamples();

		// Process the last window, since we have one window buffer delay.
		float[] flushBuffer = new float[lastInSamples.getSampleCount() * 2];
		Samples flushSamples = new Samples(lastInSamples.getFormat(), flushBuffer, flushBuffer.length);

		while (flushSamples.getSampleCount() != 0 && samples < flushBuffer.length) {
			execute(lastInSamples, flushSamples);

			flushed = flushSamples.getSampleCount();

			if (flushed > 0) {
				System.arraycopy(flushBuffer, 0, outputBuffer, samples, flushed);
				samples += flushed;
			}
		}

		// Flush the remaining window data.
		for (int i = 0; i < channels; i++) {
			flushed = processWindow(i, channels, flushBuffer, bufdata);

			if (flushed > 0) {
				System.arraycopy(flushBuffer, 0, outputBuffer, samples, flushed);
				samples += flushed;
			}
		}

		outputSamples.setSampleCount(samples);
	}

	@Override
	public void terminate() {

	}

	/**
	 * Do window management once we have a complete window, including mangling
	 * the current window.
	 */
	private int processWindow(int channel, int channels, float[] outSamples, int length) {
		int use = Math.min(length, WINDOW_SIZE) - Math.min(length, HALF_WINDOW);
		ChannelData channelData = data[channel];
		boolean first = (channelData.lastWindow == null);

		float[] nextWindow = new float[WINDOW_SIZE];

		System.arraycopy(channelData.window, HALF_WINDOW, nextWindow, 0, HALF_WINDOW);

		reduceNoise(channelData);

		if (!first) {
			for (int j = 0; j < use; j++) {
				float s = channelData.window[j] + channelData.lastWindow[HALF_WINDOW + j];
				outSamples[channel + channels * j] = s;
			}
			channelData.lastWindow = null;
		}
		else {
			for (int j = 0; j < use; j++) {
				assert (channelData.window[j] >= -1 && channelData.window[j] <= 1);
				outSamples[channel + channels * j] = channelData.window[j];
			}
		}

		channelData.lastWindow = channelData.window;
		channelData.window = nextWindow;

		return use;
	}

	/**
	 * Mangle a single window. Each output sample (except the first and last
	 * half-window) is the result of two distinct calls to this function, due to
	 * overlapping windows.
	 */
	private void reduceNoise(ChannelData channelData) {
		float[] smoothing = channelData.smoothing;
		float[] window = channelData.window;

		float[] realIn = new float[WINDOW_SIZE];
		float[] imagIn = new float[WINDOW_SIZE];
		float[] realOut = new float[WINDOW_SIZE];
		float[] imagOut = new float[WINDOW_SIZE];
		float[] power = new float[WINDOW_SIZE];

		for (int i = 0; i < FREQUENCY_COUNT; i++) {
			assert (smoothing[i] >= 0 && smoothing[i] <= 1);
		}

		System.arraycopy(window, 0, realIn, 0, WINDOW_SIZE);

		DSP.FFT(WINDOW_SIZE, realIn, null, realOut, imagOut);

		System.arraycopy(window, 0, realIn, 0, WINDOW_SIZE);

		windowFunction.apply(realIn);

		DSP.getPowerSpectrum(WINDOW_SIZE, realIn, power);

		for (int i = 0; i < FREQUENCY_COUNT; i++) {
			float smooth;
			float plog = (float) Math.log(power[i]);

			if (power[i] != 0 && plog < channelData.noisegate[i] + threshold * 8.0f) {
				smooth = 0.0f;
			}
			else {
				smooth = 1.0f;
			}

			smoothing[i] = smooth * 0.5f + smoothing[i] * 0.5f;
		}

		// Audacity says this code will eliminate tinkle bells. I have no idea what that means.
		for (int i = 2; i < FREQUENCY_COUNT - 2; i++) {
			if (smoothing[i] >= 0.5 && smoothing[i] <= 0.55
					&& smoothing[i - 1] < 0.1 && smoothing[i - 2] < 0.1
					&& smoothing[i + 1] < 0.1 && smoothing[i + 2] < 0.1) {
				smoothing[i] = 0.0f;
			}
		}

		realOut[0] *= smoothing[0];
		imagOut[0] *= smoothing[0];
		realOut[FREQUENCY_COUNT - 1] *= smoothing[FREQUENCY_COUNT - 1];
		imagOut[FREQUENCY_COUNT - 1] *= smoothing[FREQUENCY_COUNT - 1];

		for (int i = 1; i < FREQUENCY_COUNT - 1; i++) {
			int j = WINDOW_SIZE - i;
			float smooth = smoothing[i];

			realOut[i] *= smooth;
			imagOut[i] *= smooth;
			realOut[j] *= smooth;
			imagOut[j] *= smooth;
		}

		DSP.IFFT(WINDOW_SIZE, realOut, imagOut, realIn, imagIn);

		windowFunction.apply(realIn);

		System.arraycopy(realIn, 0, window, 0, WINDOW_SIZE);

		for (int i = 0; i < FREQUENCY_COUNT; i++) {
			assert (smoothing[i] >= 0 && smoothing[i] <= 1);
		}
	}



	private static class ChannelData {

		float[] window;

		float[] lastWindow;

		float[] noisegate;

		float[] smoothing;


		ChannelData() {
			noisegate = new float[FREQUENCY_COUNT];
			smoothing = new float[FREQUENCY_COUNT];
			lastWindow = null;
		}

	}
}
