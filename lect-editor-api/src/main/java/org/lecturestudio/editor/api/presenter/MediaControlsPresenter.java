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

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.media.recording.RecordingEvent;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.view.MediaControlsView;
import org.lecturestudio.media.event.MediaPlayerProgressEvent;

public class MediaControlsPresenter extends Presenter<MediaControlsView> {

	private final RecordingPlaybackService playbackService;

	private final RecordingFileService recordingService;

	private BooleanProperty playing;

	private BooleanProperty mute;

	private double playbackVolume;


	@Inject
	MediaControlsPresenter(ApplicationContext context, MediaControlsView view,
						   RecordingPlaybackService playbackService,
						   RecordingFileService recordingService) {
		super(context, view);

		this.playbackService = playbackService;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		EditorContext editorContext = (EditorContext) context;
		AudioConfiguration audioConfig = context.getConfiguration().getAudioConfig();
		Recording recording = recordingService.getSelectedRecording();

		playing = new BooleanProperty();
		playing.addListener((o, oldValue, newValue) -> {
			try {
				if (Boolean.TRUE.equals(newValue) && !playbackService.started()) {
					playbackService.start();
				}
				else if (Boolean.FALSE.equals(newValue) && playbackService.started()) {
					playbackService.suspend();
				}
			}
			catch (ExecutableException e) {
				handleException(e, "Change playback failed", "recording.playback.error");
			}
		});
		mute = new BooleanProperty();
		mute.addListener((o, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(newValue)) {
				playbackVolume = audioConfig.getPlaybackVolume();
				audioConfig.setPlaybackVolume(0);
			}
			else {
				audioConfig.setPlaybackVolume(playbackVolume);
			}
		});

		view.bindSeek(editorContext.primarySelectionProperty());
		view.bindPlaying(playing);
		view.bindMute(mute);
		view.bindVolume(audioConfig.playbackVolumeProperty());
		view.setOnSeekPressed(this::seekPressed);
		view.setOnPreviousPage(this::previousPage);
		view.setOnNextPage(this::nextPage);

		if (nonNull(recording)) {
			Document doc = recording.getRecordedDocument().getDocument();

			view.setDuration(new Time(recording.getRecordedAudio().getAudioStream().getLengthInMillis()));
			view.setCurrentPage(doc.getCurrentPageNumber() + 1, doc.getPageCount());
		}

		audioConfig.playbackVolumeProperty().addListener((o, oldValue, newValue) -> {
			playbackService.setVolume(newValue.floatValue());
		});

		playbackService.addStateListener((oldState, newState) -> {
			playing.set(newState == ExecutableState.Started);
		});

		context.getEventBus().register(this);
	}

	@Override
	public void destroy() {
		context.getEventBus().unregister(this);
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.selected()) {
			EditorContext editorContext = (EditorContext) context;
			Recording recording = event.getRecording();
			Document doc = recording.getRecordedDocument().getDocument();
			Time duration = new Time(recording.getRecordedAudio().getAudioStream().getLengthInMillis());

			view.setDuration(duration);
			view.setCurrentPage(doc.getCurrentPageNumber() + 1, doc.getPageCount());

			editorContext.setPrimarySelection(0.0);
		}
	}

	@Subscribe
	public void onEvent(RecordingChangeEvent event) {
		switch (event.getContentType()) {
			case ALL:
			case HEADER:
				final Recording recording = event.getRecording();
				final Document doc = recording.getRecordedDocument().getDocument();

				view.setDuration(new Time(recording.getRecordedAudio().getAudioStream().getLengthInMillis()));
				view.setCurrentPage(doc.getCurrentPageNumber() + 1, doc.getPageCount());
				break;
			case AUDIO, DOCUMENT, EVENTS:
				break;
		}
	}

	@Subscribe
	public void onEvent(final MediaPlayerProgressEvent event) {
		view.setCurrentPage(event.getPageNumber(), event.getPageCount());
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

	private void previousPage() {
		try {
			playbackService.selectPreviousPage();
		}
		catch (Exception e) {
			handleException(e, "Select previous page failed", "select.recording.page.error");
		}
	}

	private void nextPage() {
		try {
			playbackService.selectNextPage();
		}
		catch (Exception e) {
			handleException(e, "Select next page failed", "select.recording.page.error");
		}
	}
}
