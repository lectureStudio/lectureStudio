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

package org.lecturestudio.editor.api.edit;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.media.track.MediaTrack;
import org.lecturestudio.media.track.control.AudioFilterControl;

public class AudioTrackOverlayAction implements EditAction {

	private final Recording recording;

	private final MediaTrack<?> track;

	private final AudioFilterControl control;

	private final RecordingPlaybackService playbackService;

	private final Runnable controlListener;


	public AudioTrackOverlayAction(Recording recording, MediaTrack<?> track,
			AudioFilterControl control,
			RecordingPlaybackService playbackService) {
		this.recording = recording;
		this.track = track;
		this.control = control;
		this.playbackService = playbackService;
		this.controlListener = () -> {
			setAudioVolumeFilter(control);
		};
	}

	@Override
	public void undo() throws RecordingEditException {
		track.removeMediaTrackControl(control);

		control.removeChangeListener(controlListener);

		removeAudioVolumeFilter(control);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		track.addMediaTrackControl(control);

		control.addChangeListener(controlListener);

		setAudioVolumeFilter(control);
	}

	private void setAudioVolumeFilter(AudioFilterControl control) {
		RecordedAudio recordedAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recordedAudio.getAudioStream();
		AudioFormat audioFormat = stream.getAudioFormat();

		// Convert [0, 1] values to audio stream byte offsets.
		long durationMs = stream.getLengthInMillis();
		long x1 = (long) (control.getInterval().getStart() * durationMs);
		long x2 = (long) (control.getInterval().getEnd() * durationMs);

		long startBytePos = AudioUtils.getAudioBytePosition(audioFormat, x1);
		long endBytePos = AudioUtils.getAudioBytePosition(audioFormat, x2);

		// Handle padding created by previous exclusions.
		long padding = stream.getPadding(startBytePos);

		stream.setAudioFilter(control.getAudioFilter(),
				new Interval<>(padding + startBytePos, padding + endBytePos));

		playbackService.setAudioFilter(control.getAudioFilter(),
				new Interval<>(padding + startBytePos, padding + endBytePos));
	}

	private void removeAudioVolumeFilter(AudioFilterControl control) {
		RecordedAudio recordedAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recordedAudio.getAudioStream();

		stream.removeAudioFilter(control.getAudioFilter());

		playbackService.removeAudioFilter(control.getAudioFilter());
	}
}
