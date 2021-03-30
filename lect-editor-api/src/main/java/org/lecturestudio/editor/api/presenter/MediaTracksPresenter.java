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
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordingChangeEvent;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.editor.api.context.EditorContext;
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

		EditorContext editorContext = (EditorContext) context;
		double selectPos = editorContext.getPrimarySelection();

		AdjustAudioVolumeControl trackControl = new AdjustAudioVolumeControl();
		trackControl.setStartTime(selectPos);
		trackControl.setEndTime(selectPos);
		trackControl.addChangeListener(() -> {
			setAudioVolumeFilter(trackControl);
		});

		setAudioVolumeFilter(trackControl);

		audioTrack.addMediaTrackControl(trackControl);
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.selected()) {
			final Recording recording = event.getRecording();
			final Recording prevRecording = event.getOldRecording();
			final Time duration = new Time(recording.getRecordedAudio().getAudioStream().getLengthInMillis());

			if (nonNull(prevRecording)) {
				mediaTracks.forEach(prevRecording::removeRecordingChangeListener);
			}

			// Dispose previous tracks.
			mediaTracks.forEach(MediaTrack::dispose);
			mediaTracks.clear();

			view.setDuration(duration);

			CompletableFuture.runAsync(() -> {
				AudioTrack audioTrack = new AudioTrack();
				audioTrack.setData(recording.getRecordedAudio().getAudioStream());

				EventsTrack eventsTrack = new EventsTrack();
				eventsTrack.setData(recording.getRecordedEvents().getRecordedPages());

				mediaTracks.add(audioTrack);
				mediaTracks.add(eventsTrack);
				mediaTracks.forEach(recording::addRecordingChangeListener);

				view.setMediaTracks(eventsTrack, audioTrack);
			})
			.exceptionally(throwable -> {
				handleException(throwable, "Create waveform failed", "open.recording.error");
				return null;
			});
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

	private void setAudioVolumeFilter(AdjustAudioVolumeControl trackControl) {
		Recording recording = recordingService.getSelectedRecording();
		RecordedAudio recordedAudio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = recordedAudio.getAudioStream();
		AudioFormat audioFormat = stream.getAudioFormat();

		// Convert [0, 1] values to audio stream byte offsets.
		long durationMs = stream.getLengthInMillis();
		long x1 = (long) (trackControl.getInterval().getStart() * durationMs);
		long x2 = (long) (trackControl.getInterval().getEnd() * durationMs);

		long startBytePos = AudioUtils.getAudioBytePosition(audioFormat, x1);
		long endBytePos = AudioUtils.getAudioBytePosition(audioFormat, x2);

		// Handle padding created by previous exclusions.
		long padding = stream.getPadding(startBytePos);

		stream.setAudioFilter(trackControl.getAudioFilter(),
				new Interval<>(padding + startBytePos, padding + endBytePos));

		playbackService.setAudioFilter(trackControl.getAudioFilter(),
				new Interval<>(padding + startBytePos, padding + endBytePos));
	}
}
