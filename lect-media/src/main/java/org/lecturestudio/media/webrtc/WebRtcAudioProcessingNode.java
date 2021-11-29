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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import dev.onvoid.webrtc.media.audio.AudioProcessing;
import dev.onvoid.webrtc.media.audio.AudioProcessingConfig;
import dev.onvoid.webrtc.media.audio.AudioProcessingStreamConfig;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioProcessingStats;

public class WebRtcAudioProcessingNode implements WebRtcAudioSinkNode, WebRtcAudioSourceNode {

	private AudioProcessing audioProcessing;

	private AudioProcessingConfig config;

	private AudioProcessingStreamConfig streamConfigIn;

	private AudioProcessingStreamConfig streamConfigOut;

	private AudioProcessingStats stats;

	private AudioFormat sinkFormat;

	private WebRtcAudioSinkNode sinkNode;

	private byte[] buffer;


	@Override
	public void initialize() {
		requireNonNull(sinkFormat, "AudioFormat must be set");
		requireNonNull(config, "AudioProcessingConfig must be set");

		int nBytesPerSampleOut = 2 * sinkFormat.getChannels();
		int nSamplesOut = sinkFormat.getSampleRate() / 100; // 10 ms frame
		int nBytesOut = nSamplesOut * nBytesPerSampleOut;

		buffer = new byte[nBytesOut];

		streamConfigOut = new AudioProcessingStreamConfig(
				sinkFormat.getSampleRate(), sinkFormat.getChannels());

		audioProcessing = new AudioProcessing();
		audioProcessing.applyConfig(config);

		stats = new AudioProcessingStats();

		sinkNode.initialize();
	}

	@Override
	public void destroy() {
		if (nonNull(audioProcessing)) {
			audioProcessing.dispose();
		}

		sinkNode.destroy();
	}

	@Override
	public void setAudioSinkFormat(AudioFormat format) {
		this.sinkFormat = format;
	}

	@Override
	public void setAudioSinkNode(WebRtcAudioSinkNode node) {
		this.sinkNode = node;
	}

	@Override
	public void onData(byte[] audioSamples, int nSamples, int nBytesPerSample,
			int nChannels, int samplesPerSec, int totalDelayMS,
			int clockDrift) {
		if (isNull(streamConfigIn)) {
			streamConfigIn = new AudioProcessingStreamConfig(samplesPerSec,
					nChannels);
		}

		audioProcessing.processStream(audioSamples, streamConfigIn,
				streamConfigOut, buffer);

		sinkNode.onData(buffer, nSamples, sinkFormat.getChannels() * 2,
				sinkFormat.getChannels(), sinkFormat.getSampleRate(),
				totalDelayMS, clockDrift);
	}

	public AudioProcessingStats getAudioProcessingStats() {
		if (nonNull(audioProcessing)) {
			return AudioProcessingStatsConverter.INSTANCE.to(
					audioProcessing.getStatistics());
		}

		return stats;
	}

	public void setAudioProcessingConfig(AudioProcessingConfig config) {
		this.config = config;
	}
}
