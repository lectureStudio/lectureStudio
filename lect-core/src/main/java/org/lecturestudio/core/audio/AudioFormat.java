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

import java.io.Serializable;
import java.util.Objects;

/**
 * The {@link AudioFormat} specifies a particular arrangement of audio data in a audio
 * source or in a audio sink. Having an audio format gives the ability to
 * interpret the bits in the binary audio data stream.
 *
 * @author Alex Andres
 */
public class AudioFormat implements Serializable {

	/**
	 * The encoding specifies the convention used to represent the audio data.
	 */
	public enum Encoding {
		/** Signed 16 Bit PCM, little endian. */
		S16LE,
		/** Signed 16 Bit PCM, big endian. */
		S16BE,
		/** Signed 24 Bit PCM, little endian. */
		S24LE,
		/** Signed 24 Bit PCM, big endian. */
		S24BE,
		/** Signed 32 Bit PCM, little endian. */
		S32LE,
		/** Signed 32 Bit PCM, big endian. */
		S32BE,
		/** 32 Bit IEEE floating point, little endian, range -1.0 to 1.0 */
		FLOAT32LE,
		/** 32 Bit IEEE floating point, big endian, range -1.0 to 1.0 */
		FLOAT32BE
	}


	/** The encoding of this audio format. */
	private Encoding encoding;

	/** The sample rate of this audio format. */
	private int sampleRate;

	/** The number of channels of this audio format. */
	private int channels;


	/**
	 * Creates an uninitialized {@link AudioFormat}.
	 */
	public AudioFormat() {

	}

	/**
	 * Creates an {@link AudioFormat} with the given parameters.
	 *
	 * @param encoding   The audio encoding.
	 * @param sampleRate The number of samples per second.
	 * @param channels   The number of channels (1 for mono, 2 for stereo, and
	 *                   so on).
	 */
	public AudioFormat(Encoding encoding, int sampleRate, int channels) {
		this.encoding = encoding;
		this.sampleRate = sampleRate;
		this.channels = channels;
	}

	/**
	 * Returns the type of encoding for audio in this format.
	 * 
	 * @return the audio encoding.
	 */
	public Encoding getEncoding() {
		return encoding;
	}

	/**
	 * Returns the number of samples per second.
	 * 
	 * @return the sample rate.
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * Returns the number of channels (1 for mono, 2 for stereo, etc.).
	 * 
	 * @return the number of channels.
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * Returns the number of bytes in each sample.
	 * 
	 * @return the size of a sample in bytes.
	 */
	public int getBytesPerSample() {
		return getBitsPerSample() / 8;
	}

	/**
	 * Returns the number of bits of each sample.
	 * 
	 * @return the size of a sample in bits.
	 */
	public int getBitsPerSample() {
		switch (encoding) {
			case S16LE:
			case S16BE:
				return 16;
			case S24LE:
			case S24BE:
				return 24;
			case S32LE:
			case S32BE:
			case FLOAT32LE:
			case FLOAT32BE:
				return 32;

			default:
				return 0;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(sampleRate, sampleRate, encoding);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		AudioFormat other = (AudioFormat) obj;

		if (channels != other.channels) {
			return false;
		}
		if (encoding != other.encoding) {
			return false;
		}

		return sampleRate == other.sampleRate;
	}

	@Override
	public String toString() {
		return encoding.toString() + ", " + sampleRate + ", " + channels;
	}

}
