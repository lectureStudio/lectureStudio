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

package org.lecturestudio.core.util;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioFormat.Encoding;

public class AudioUtils {

	public static AudioFormat getNearestFormat(AudioFormat[] formats, int sampleRate) {
		if (formats == null || formats.length < 1)
			return null;
		
		AudioFormat nearestFormat = formats[0];
		
		for (AudioFormat format : formats) {
			if (sampleRate >= Math.round(format.getSampleRate()))
				nearestFormat = format;
		}
		
		return nearestFormat;
	}

	public static AudioFormat createAudioFormat(javax.sound.sampled.AudioFormat format) {
		int sampleRate = (int) format.getSampleRate();
		int channels = format.getChannels();
		int bitsPerSample = format.getSampleSizeInBits();
		boolean bigEndian = format.isBigEndian();
		boolean isFloat = format.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT;

		AudioFormat.Encoding encoding;

		if (bitsPerSample == 16) {
			encoding = bigEndian ? AudioFormat.Encoding.S16BE : AudioFormat.Encoding.S16LE;
		}
		else if (bitsPerSample == 24) {
			encoding = bigEndian ? AudioFormat.Encoding.S24BE : AudioFormat.Encoding.S24LE;
		}
		else if (bitsPerSample == 32) {
			if (isFloat) {
				encoding = bigEndian ? AudioFormat.Encoding.FLOAT32BE : AudioFormat.Encoding.FLOAT32LE;
			}
			else {
				encoding = bigEndian ? AudioFormat.Encoding.S32BE : AudioFormat.Encoding.S32LE;
			}
		}
		else {
			// Fallback to default.
			encoding = bigEndian ? AudioFormat.Encoding.S16BE : AudioFormat.Encoding.S16LE;
		}

		return new AudioFormat(encoding, sampleRate, channels);
	}

	public static javax.sound.sampled.AudioFormat createAudioFormat(AudioFormat format) {
		javax.sound.sampled.AudioFormat.Encoding encoding = javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

		if (format.getEncoding() == Encoding.FLOAT32BE || format.getEncoding() == Encoding.FLOAT32LE) {
			encoding = javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT;
		}

		int sampleRate = format.getSampleRate();
		int channels = format.getChannels();
		int sampleSizeInBits = format.getBitsPerSample();
		int frameSize = format.getBytesPerSample() * format.getChannels();

		return new javax.sound.sampled.AudioFormat(encoding, sampleRate,
				sampleSizeInBits, channels, frameSize, sampleRate, false);
	}

	public static long getAudioBytePosition(AudioFormat audioFormat, long millis) {
		int bytesPerSecond = getBytesPerSecond(audioFormat);
		int sampleSize = audioFormat.getBytesPerSample();
		long position = ((bytesPerSecond * millis) / 1000) & Long.MAX_VALUE;

		// Check for integral sample count.
		if ((position % sampleSize) != 0) {
			position--;
		}

		return position;
	}

	public static int getBytesPerSecond(AudioFormat audioFormat) {
		return Math.round(audioFormat.getSampleRate() * audioFormat.getBytesPerSample() * audioFormat.getChannels());
	}

	public static float getSampleValue(byte[] data, int index) {
		short value = (short) ((data[index + 1] << 8) | data[index]);

		return (float) value / Short.MAX_VALUE;
	}
}
