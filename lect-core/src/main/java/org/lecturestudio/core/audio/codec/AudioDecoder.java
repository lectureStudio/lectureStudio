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

/**
 * Base audio decoder implementation to provide a consistent mechanism to decode
 * encoded audio. To get the decoded audio the receiver has to register an
 * {@link AudioDecoderListener}.
 *
 * @author Alex Andres
 */
public abstract class AudioDecoder extends AudioCodec {

	/** Registered receivers of decoded audio. */
	private List<AudioDecoderListener> listeners = new ArrayList<>();


	/**
	 * Register an listener to receive the decoded audio.
	 *
	 * @param listener The listener that receives decoded audio.
	 */
	public void addListener(AudioDecoderListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove an listener from the listener list.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(AudioDecoderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all registered listeners that an audio chunk has been decoded.
	 *
	 * @param data      The decoded audio data.
	 * @param length    The length of the decoded audio data.
	 * @param timestamp The timestamp of the decoded audio.
	 */
	protected void fireAudioDecoded(byte[] data, int length, long timestamp) {
		for (AudioDecoderListener listener : listeners) {
			listener.audioDecoded(data, length, timestamp);
		}
	}

}
