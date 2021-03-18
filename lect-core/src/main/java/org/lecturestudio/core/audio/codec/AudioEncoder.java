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

package org.lecturestudio.core.audio.codec;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.audio.AudioFormat;

/**
 * Base audio encoder implementation to provide a consistent mechanism to encode
 * audio. To get the encoded audio the receiver has to register an {@link
 * AudioEncoderListener}.
 *
 * @author Alex Andres
 */
public abstract class AudioEncoder extends AudioCodec {

	/** Registered receivers of encoded audio. */
	private List<AudioEncoderListener> listeners = new ArrayList<>();

	/** The bitrate of the encoded audio. */
	private int bitrate;


	/**
	 * Get all supported audio formats of this encoder.
	 *
	 * @return an array of supported audio formats.
	 */
	abstract public AudioFormat[] getSupportedFormats();


	/**
	 * Set the bitrate of the encoded audio.
	 *
	 * @param bitrate the bitrate of the encoded audio.
	 */
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	/**
	 * Get the bitrate of the encoded audio.
	 *
	 * @return the bitrate of the encoded audio.
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * Register an listener to receive the encoded audio.
	 *
	 * @param listener The listener that receives encoded audio.
	 */
	public void addListener(AudioEncoderListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove an listener from the listener list.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(AudioEncoderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all registered listeners that an audio chunk has been encoded.
	 *
	 * @param data      The encoded audio data.
	 * @param length    The length of the encoded audio data.
	 * @param timestamp The timestamp of the encoded audio.
	 */
	protected void fireAudioEncoded(byte[] data, int length, long timestamp) {
		for (AudioEncoderListener listener : listeners) {
			listener.audioEncoded(data, length, timestamp);
		}
	}

}
