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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.util.AudioUtils;

public class WaveformBuilder {

	public WaveformData build(AudioFormat format, InputStream stream, int width) throws IOException {
		requireNonNull(format);
		requireNonNull(stream);

		if (width < 0) {
			throw new IllegalArgumentException();
		}

		float[] posSamples = new float[width];
		float[] negSamples = new float[width];

		int streamLength = stream.available();
		int sampleSize = format.getBytesPerSample();
		int blockSize = streamLength / width - (streamLength / width) % sampleSize;
		int pad = 0;

		double error = streamLength / (double) width - blockSize;
		double errorSum = 0;

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (int i = 0; i < width; i++) {
			int index = i;
			byte[] buffer = new byte[blockSize + sampleSize];

			int read = stream.read(buffer, 0, blockSize + pad);

			// Add missing samples due to rounding error.
			if (errorSum >= sampleSize) {
				pad = sampleSize;
				errorSum -= sampleSize;
			}
			else {
				pad = 0;
			}

			errorSum += error;

			futures.add(CompletableFuture.runAsync(() -> {
				process(buffer, posSamples, negSamples, index, read,
						sampleSize);
			}));
		}

		futures.forEach(CompletableFuture::join);

		return new WaveformData(posSamples, negSamples);
	}

	private void process(byte[] buffer, float[] posSamples, float[] negSamples,
			int index, int read, int sampleSize) {
		float posMax = 0;
		float negMax = 0;

		// Read all samples in the chunk.
		for (int j = 0; j < read; j += sampleSize) {
			// Get one sample.
			float value = AudioUtils.getSampleValue(buffer, j);

			if (value > 0) {
				posMax = Math.max(posMax, Math.abs(value));
			}
			else {
				negMax = Math.max(negMax, Math.abs(value));
			}
		}

		if (read > 0) {
			posSamples[index] = posMax;
			negSamples[index] = negMax;
		}
	}
}
