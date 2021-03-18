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

import java.io.IOException;
import java.io.InputStream;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.analysis.HannWindowFunction;
import org.lecturestudio.core.audio.analysis.WindowFunction;
import org.lecturestudio.core.io.RandomAccessAudioStream;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class SpectrogramBuilder {

	public Spectrogram build(RandomAccessAudioStream stream, int width, int height)
			throws IOException {
		final Transformer reader = new Transformer(stream, width, height);
		return reader.create();
	}

	private static int nextPow2(int n) {
		return n <= 0 ? 0 : highestOneBit(n - 1);
	}

	private static int highestOneBit(int n) {
		int digit;
		for (digit = 32; digit > 0; --digit) {
			if ((n & Integer.MIN_VALUE) == Integer.MIN_VALUE) {
				return digit;
			}

			n <<= 1;
		}

		return digit;
	}



	private static class Transformer {

		private final RandomAccessAudioStream inputStream;
		private final AudioFormat audioFormat;

		private final FastFourierTransformer transformer;

		private final PcmCodec pcmCodec;

		private final byte[] inputBuffer;

		private final double[] window;
		private final double[] samples;
		private final double[] sampleBuffer;
		private final double[] frame;

		private final int width;

		private final int channels;
		private final int sampleSize;
		private final int sampleBytes;

		private final int fftSize;


		Transformer(RandomAccessAudioStream stream, int width, int height) {
			this.width = width;
			audioFormat = stream.getAudioFormat();
			inputStream = stream;
			pcmCodec = PcmCodecs.getCodec(audioFormat);
			transformer = new FastFourierTransformer(DftNormalization.STANDARD);

			channels = audioFormat.getChannels();
			sampleSize = audioFormat.getBytesPerSample();
			sampleBytes = sampleSize * channels;

			fftSize = 1 << nextPow2(height);

			WindowFunction windowFunction = new HannWindowFunction(height);
			windowFunction.normalize();

			window = windowFunction.getValues(fftSize);

			inputBuffer = new byte[window.length * sampleBytes];
			samples = new double[window.length];
			sampleBuffer = new double[window.length];
			frame = new double[window.length];
		}

		Spectrogram create() throws IOException {
			final int frameBytes = audioFormat.getBytesPerSample() * audioFormat.getChannels();
			final long frameLength = inputStream.available() / frameBytes;
			final int shiftSize = (int) (frameLength / width);
			final int nFrames = (int) (frameLength / shiftSize);
			final int frameSize = (fftSize >> 1) + 1;

			final double[][] specLog = new double[nFrames][frameSize];

			readSamples(inputStream, inputBuffer.length);

			System.arraycopy(samples, 0, frame, 0, samples.length);
			System.arraycopy(frame, 0, sampleBuffer, 0, frame.length);

			specLog[0] = getFrequencies();

			int length = shiftSize * sampleBytes;

			for (int x = 1; x < nFrames; x++) {
				readSamples(inputStream, length);

				// Shifting buffer for overlapped samples.
				System.arraycopy(sampleBuffer, shiftSize, frame, 0, frame.length - shiftSize);
				System.arraycopy(samples, 0, frame, frame.length - shiftSize, shiftSize);
				System.arraycopy(frame, 0, sampleBuffer, 0, frame.length);

				specLog[x] = getFrequencies();
			}

			inputStream.close();

			return new Spectrogram(audioFormat, frameLength, width, frameSize, specLog);
		}

		void readSamples(InputStream stream, int numBytes) throws IOException {
			int read = stream.read(inputBuffer, 0, Math.min(numBytes, stream.available()));
			int sampleFrames = read / sampleBytes;

			for (int i = 0; i < sampleFrames; ++i) {
				double value = 0;

				for (int j = 0; j < channels; ++j) {
					value += pcmCodec.decode(inputBuffer, (i * channels + j) * sampleSize);
				}

				samples[i] = value / channels;
			}
		}

		double[] getFrequencies() {
			// Apply window function on the samples.
			for (int i = 0; i < frame.length; i++) {
				frame[i] *= window[i];
			}

			Complex[] complex = transformer.transform(frame, TransformType.FORWARD);
			int freqLength = (frame.length >> 1) + 1;
			double[] freq = new double[freqLength];

			for (int c = 0; c < freqLength; c++) {
				freq[freqLength - c - 1] = 20.0 * Math.log10(complex[c].abs());
			}

			return freq;
		}
	}
}
