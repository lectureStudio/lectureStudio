/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.media.webrtc;

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.sink.AudioSink;

/**
 * An {@code AudioSinkNode} takes an {@code AudioSink} which is a proxy to the
 * {@code WebRtcAudioSinkNode}.
 *
 * @author Alex Andres
 */
public class AudioSinkNode implements WebRtcAudioSinkNode {

	private final AudioSink sink;


	public AudioSinkNode(AudioSink sink) {
		this.sink = sink;
	}

	@Override
	public void initialize() {
		try {
			sink.open();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void destroy() {
		try {
			sink.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setAudioSinkFormat(AudioFormat format) {
		// No-op
	}

	@Override
	public void onData(byte[] audioSamples, int nSamples, int nBytesPerSample,
			int nChannels, int samplesPerSec, int totalDelayMS,
			int clockDrift) {
		try {
			sink.write(audioSamples, 0, audioSamples.length);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
