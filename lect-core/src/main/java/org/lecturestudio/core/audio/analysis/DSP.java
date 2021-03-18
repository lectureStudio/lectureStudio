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

package org.lecturestudio.core.audio.analysis;

import org.jtransforms.fft.FloatFFT_1D;

/**
 * Digital Signal Processing (DSP) helper class used to perform Fast Fourier
 * Transform (FFT) computations.
 *
 * @author Alex Andres
 */
public final class DSP {

	/**
	 * Compute the forward transform of the specified complex data set. The
	 * input is separated in two parts. The complex input number is stored as
	 * two float values: the real and imaginary part. Same is applied for the
	 * computed output values.
	 *
	 * @param samples The number of samples.
	 * @param realIn  The real part of data to transform.
	 * @param imagIn  The imaginary part of data to transform.
	 * @param realOut The computed real part of the transform.
	 * @param imagOut The computed imaginary part of the transform.
	 */
	public static void FFT(int samples, float[] realIn, float[] imagIn, float[] realOut, float[] imagOut) {
		float[] work = new float[2 * samples];

		for (int i = 0; i < 2 * samples; i += 2) {
			work[i] = realIn[i >> 1];
			work[i + 1] = (imagIn != null) ? imagIn[i >> 1] : 0;
		}

		FloatFFT_1D fft = new FloatFFT_1D(samples);
		fft.complexForward(work);

		for (int i = 0; i < 2 * samples; i += 2) {
			realOut[i >> 1] = work[i];
			imagOut[i >> 1] = work[i + 1];
		}
	}

	/**
	 * Compute the inverse transform of the specified complex data set. The
	 * input is separated in two parts. The complex input number is stored as
	 * two float values: the real and imaginary part. Same is applied for the
	 * computed output values.
	 *
	 * @param samples The number of samples.
	 * @param realIn  The real part of data to transform.
	 * @param imagIn  The imaginary part of data to transform.
	 * @param realOut The computed real part of the transform.
	 * @param imagOut The computed imaginary part of the transform.
	 */
	public static void IFFT(int samples, float[] realIn, float[] imagIn, float[] realOut, float[] imagOut) {
		float[] work = new float[2 * samples];

		for (int i = 0; i < 2 * samples; i += 2) {
			work[i] = realIn[i >> 1];
			work[i + 1] = (imagIn != null) ? imagIn[i >> 1] : 0;
		}

		FloatFFT_1D fft = new FloatFFT_1D(samples);
		fft.complexInverse(work, false);

		for (int i = 0; i < 2 * samples; i += 2) {
			realOut[i >> 1] = (float) (work[i] / samples);
			imagOut[i >> 1] = (float) (work[i + 1] / samples);
		}
	}

	/**
	 * Compute the power spectrum of the specified real sample input data.
	 *
	 * @param samples The number of samples.
	 * @param in      The sample input data.
	 * @param out     The computed power spectrum data.
	 */
	public static void getPowerSpectrum(int samples, float[] in, float[] out) {
		float[] work = new float[samples];
		int i;

		for (i = 0; i < samples; ++i) {
			work[i] = in[i];
		}

		FloatFFT_1D fft = new FloatFFT_1D(samples);
		fft.realForward(work);

		out[0] = (float) Math.pow(work[0], 2);

		for (i = 2; i < samples; i += 2) {
			out[i >> 1] = (float) (Math.pow(work[i], 2) + Math.pow(work[i + 1], 2));
		}

		out[i >> 1] = (float) Math.pow(work[1], 2);
	}

}
