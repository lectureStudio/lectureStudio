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

package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.RecordingEditManager;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.edit.AudioTrackOverlayAction;
import org.lecturestudio.media.recording.RecordingEvent;
import org.lecturestudio.editor.api.presenter.command.AdjustAudioCommand;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.view.MediaTracksView;
import org.lecturestudio.media.track.AudioTrack;
import org.lecturestudio.media.track.EventsTrack;
import org.lecturestudio.media.track.MediaTrack;
import org.lecturestudio.media.track.control.AdjustAudioVolumeControl;

public class MediaTracksPresenter extends Presenter<MediaTracksView> {

	private final RecordingFileService recordingService;

	private final RecordingPlaybackService playbackService;

	private final List<MediaTrack<?>> mediaTracks;


	@Inject
	MediaTracksPresenter(ApplicationContext context, MediaTracksView view,
						 RecordingFileService recordingService,
						 RecordingPlaybackService playbackService) {
		super(context, view);

		this.recordingService = recordingService;
		this.playbackService = playbackService;
		this.mediaTracks = new ArrayList<>();
	}

	@Override
	public void initialize() {
		EditorContext editorContext = (EditorContext) context;

		// Init with empty tracks to display them in advance.
		AudioTrack audioTrack = new AudioTrack();
		EventsTrack eventsTrack = new EventsTrack();

		mediaTracks.add(audioTrack);
		mediaTracks.add(eventsTrack);

		view.setMediaTracks(eventsTrack, audioTrack);
		view.bindPrimarySelection(editorContext.primarySelectionProperty());
		view.bindLeftSelection(editorContext.leftSelectionProperty());
		view.bindRightSelection(editorContext.rightSelectionProperty());
		view.bindZoomLevel(editorContext.trackZoomLevelProperty());
		view.setOnSeekPressed(this::seekPressed);

		context.getEventBus().register(this);
	}

	@Override
	public void destroy() {
		context.getEventBus().unregister(this);
	}

	@Subscribe
	public void onEvent(AdjustAudioCommand command) {
		AudioTrack audioTrack = (AudioTrack) mediaTracks.stream()
				.filter(AudioTrack.class::isInstance).findFirst().orElse(null);

		if (isNull(audioTrack)) {
			return;
		}

		Recording recording = recordingService.getSelectedRecording();
		RecordingEditManager editManager = recording.getEditManager();

		EditorContext editorContext = (EditorContext) context;

		AdjustAudioVolumeControl trackControl = new AdjustAudioVolumeControl();
		trackControl.setStartTime(editorContext.getLeftSelection());
		trackControl.setEndTime(editorContext.getRightSelection());

		try {
			editManager.addEditAction(new AudioTrackOverlayAction(
					recording, audioTrack, trackControl, playbackService));

			editorContext.setCanRedo(editManager.hasRedoActions());
			editorContext.setCanUndo(editManager.hasUndoActions());
		}
		catch (RecordingEditException e) {
			handleException(e, "Add edit action failed", "generic.error");
		}
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.selected()) {
			final Recording recording = event.getRecording();
			final Recording prevRecording = event.getOldRecording();
			final RecordedAudio recAudio = recording.getRecordedAudio();
			final Time duration = new Time(recAudio.getAudioStream().getLengthInMillis());

			if (nonNull(prevRecording)) {
				mediaTracks.forEach(prevRecording::removeRecordingChangeListener);
			}

			// Dispose previous tracks.
			mediaTracks.forEach(MediaTrack::dispose);
			mediaTracks.clear();

			view.setDuration(duration);

			AudioTrack audioTrack = new AudioTrack();
			audioTrack.setData(recording.getRecordedAudio().getAudioStream());

			EventsTrack eventsTrack = new EventsTrack();
			eventsTrack.setData(recording.getRecordedEvents().getRecordedPages());

			mediaTracks.add(audioTrack);
			mediaTracks.add(eventsTrack);
			mediaTracks.forEach(recording::addRecordingChangeListener);

			view.setMediaTracks(eventsTrack, audioTrack);
		}
		else if (event.closed()) {
			Recording recording = event.getRecording();

			view.setMediaTracks();

			mediaTracks.forEach(recording::removeRecordingChangeListener);
			mediaTracks.clear();
		}
	}

	@Subscribe
	public void onEvent(RecordingChangeEvent event) {
		switch (event.getContentType()) {
			case ALL:
			case HEADER:
				view.setDuration(new Time(event.getRecording().getRecordedAudio().getAudioStream().getLengthInMillis()));
				view.stickSliders();
				break;
			case AUDIO, DOCUMENT, EVENTS:
				break;
		}
	}

	private void seekPressed() {
		try {
			if (playbackService.started()) {
				playbackService.suspend();
			}
		}
		catch (ExecutableException e) {
			logException(e, "Suspend playback failed");
		}
	}
}
