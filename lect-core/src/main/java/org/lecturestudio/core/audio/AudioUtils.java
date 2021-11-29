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

package org.lecturestudio.core.audio;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.audio.AudioFormat.Encoding;

/**
 * Audio-related utility methods.
 *
 * @author Alex Andres
 */
public class AudioUtils {

	/** An array of all available sample rates to support. */
	public static final int[] SUPPORTED_SAMPLE_RATES = new int[] {
			8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 96000
	};


	/**
	 * Returns all supported {@code AudioFormat}s which can be used for audio
	 * playback or recording.
	 *
	 * @return A list of all supported {@code AudioFormat}s.
	 */
	public static List<AudioFormat> getAudioFormats() {
		List<AudioFormat> formats = new ArrayList<>();

		for (int sampleRate : SUPPORTED_SAMPLE_RATES) {
			formats.add(new AudioFormat(Encoding.S16LE, sampleRate, 1));
		}

		return formats;
	}

	/**
	 * Computes the number of bytes per second that the specified audio format
	 * will require.
	 *
	 * @param audioFormat The audio format.
	 *
	 * @return The required number of bytes per second.
	 */
	public static int getBytesPerSecond(AudioFormat audioFormat) {
		return Math.round(audioFormat.getSampleRate() * audioFormat
				.getBytesPerSample() * audioFormat.getChannels());
	}

	/**
	 * Pack two sequential bytes into a {@code short} value according to the
	 * specified endianness.
	 *
	 * @param bytes     The bytes to pack, must of size 2.
	 * @param bigEndian True to pack with big-endian order, false to pack with
	 *                  little-endian order.
	 *
	 * @return two packed bytes as an {@code short} value.
	 */
	public static int toShort(byte[] bytes, boolean bigEndian) {
		if (bytes.length == 1) {
			return bytes[0];
		}

		if (bigEndian) {
			return ((bytes[0] << 8) | (0xff & bytes[1]));
		}
		else {
			return ((bytes[1] << 8) | (0xff & bytes[0]));
		}
	}

	/**
	 * Convert the specified normalized float value to an short value.
	 *
	 * @param value The normalized float value to convert.
	 *
	 * @return an short value.
	 */
	public static int toShort(double value) {
		return (int) (value * Short.MAX_VALUE);
	}

	/**
	 * Convert the specified integer value to a normalized float value in the
	 * range of [0,1].
	 *
	 * @param value     The integer value to convert.
	 * @param frameSize The sample size in bytes.
	 * @param signed    True to respect the sign bit, false otherwise.
	 *
	 * @return a normalized float value.
	 */
	public static float getNormalizedSampleValue(int value, int frameSize,
			boolean signed) {
		float relValue;
		int maxValue;

		if (signed) {
			maxValue = (frameSize == 2) ? Short.MAX_VALUE : Byte.MAX_VALUE;
		}
		else {
			maxValue = (frameSize == 2) ? 0xffff : 0xff;
		}

		relValue = (float) value / maxValue;

		return relValue;
	}

}
