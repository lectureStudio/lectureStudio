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

package org.lecturestudio.media.track;

import java.io.IOException;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.media.audio.WaveformBuilder;
import org.lecturestudio.media.audio.WaveformData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioTrack extends MediaTrackBase<RandomAccessAudioStream> {

	private static final Logger LOG = LogManager.getLogger(AudioTrack.class);

	private final WaveformBuilder waveformBuilder;

	private WaveformData waveformData;


	public AudioTrack() {
		waveformBuilder = new WaveformBuilder();
	}

	@Override
	public void setData(RandomAccessAudioStream stream) {
		try {
			waveformData = waveformBuilder.build(stream.getAudioFormat(), stream.clone(), 30000);
		}
		catch (IOException e) {
			LOG.error("Create waveform data failed", e);
		}

		super.setData(stream);
	}

	@Override
	public void dispose() {
		try {
			getData().close();
		}
		catch (IOException e) {
			LOG.error("Close audio stream failed", e);
		}
	}

	@Override
	public void recordingChanged(RecordingChangeEvent event) {
		switch (event.getContentType()) {
			case ALL:
			case AUDIO:
				dispose();
				setData(event.getRecording().getRecordedAudio().getAudioStream().clone());
				break;
		}
	}

	public WaveformData getWaveformData() {
		return waveformData;
	}
}
