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

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.media.track.MediaTrack;
import org.lecturestudio.media.track.control.AudioFilterControl;

/**
 * EditAction that applies an {@code AudioFilter} on an {@code AudioTrack}. The
 * parameters of the AudioFilter are managed by the {@code AudioFilterControl}.
 *
 * @author Alex Andres
 *
 * @see org.lecturestudio.core.audio.filter.AudioFilter
 * @see org.lecturestudio.media.track.AudioTrack
 * @see AudioFilterControl
 */
public class AudioTrackOverlayAction implements EditAction {

	private final Recording recording;

	private final MediaTrack<?> track;

	private final AudioFilterControl<?> control;

	private final RecordingPlaybackService playbackService;

	private final Runnable controlChangeListener;

	private final Runnable controlRemoveListener;


	/**
	 * Creates a new {@code AudioTrackOverlayAction} with the provided
	 * parameters.
	 *
	 * @param recording       The recording on which to operate.
	 * @param track           The audio track on which to operate.
	 * @param control         The audio control to observe.
	 * @param playbackService The playback service to notify when changes
	 *                        occur.
	 */
	public AudioTrackOverlayAction(Recording recording, MediaTrack<?> track,
			AudioFilterControl<?> control,
			RecordingPlaybackService playbackService) {
		this.recording = recording;
		this.track = track;
		this.control = control;
		this.playbackService = playbackService;
		this.controlChangeListener = () -> {
			setAudioVolumeFilter(control);
		};
		this.controlRemoveListener = () -> {
			try {
				recording.getEditManager().addEditAction(
						new AudioTrackOverlayAction(recording, track, control,
								playbackService) {

							@Override
							public void undo() throws RecordingEditException {
								AudioTrackOverlayAction.this.redo();
							}

							@Override
							public void redo() throws RecordingEditException {
								execute();
							}

							@Override
							public void execute()
									throws RecordingEditException {
								AudioTrackOverlayAction.this.undo();
							}
						});
			}
			catch (RecordingEditException e) {
				// Ignore
			}
		};
	}

	@Override
	public void undo() throws RecordingEditException {
		track.removeMediaTrackControl(control);

		control.removeChangeListener(controlChangeListener);

		removeAudioVolumeFilter(control);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		track.addMediaTrackControl(control);

		control.addChangeListener(controlChangeListener);
		control.addRemoveListener(controlRemoveListener);

		setAudioVolumeFilter(control);
	}

	private void setAudioVolumeFilter(AudioFilterControl<?> control) {
		RecordedAudio recordedAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recordedAudio.getAudioStream();

		// Convert [0, 1] normalized values to virtual milliseconds.
		long durationMs = stream.getLengthInMillis();
		long startMs = (long) (control.getInterval().getStart() * durationMs);
		long endMs = (long) (control.getInterval().getEnd() * durationMs);

		// Convert virtual milliseconds to physical byte positions.
		long physicalStart = stream.virtualMillisToPhysicalBytes(startMs);
		long physicalEnd = stream.virtualMillisToPhysicalBytes(endMs);

		Interval<Long> physicalInterval = new Interval<>(physicalStart, physicalEnd);

		stream.setAudioFilter(control.getAudioFilter(), physicalInterval);
		playbackService.setAudioFilter(control.getAudioFilter(), physicalInterval);
	}

	private void removeAudioVolumeFilter(AudioFilterControl<?> control) {
		RecordedAudio recordedAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recordedAudio.getAudioStream();

		stream.removeAudioFilter(control.getAudioFilter());

		playbackService.removeAudioFilter(control.getAudioFilter());
	}
}
