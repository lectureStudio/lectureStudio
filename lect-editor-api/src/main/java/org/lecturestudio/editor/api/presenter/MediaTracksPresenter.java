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
import org.lecturestudio.media.audio.WaveformData;
import org.lecturestudio.media.track.AudioTrack;
import org.lecturestudio.media.track.EventsTrack;
import org.lecturestudio.media.track.MediaTrack;

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
		Recording recording = recordingService.getSelectedRecording();
		RecordedAudio audio = recording.getRecordedAudio();
		RandomAccessAudioStream stream = audio.getAudioStream().clone();
		AudioFormat audioFormat = stream.getAudioFormat();

		double timeSelect1 = editorContext.getLeftSelection();
		double timeSelect2 = editorContext.getRightSelection();

		double start = Math.min(timeSelect1, timeSelect2);
		double end = Math.max(timeSelect1, timeSelect2);

		int startTime = (int) (start * stream.getLengthInMillis());
		int endTime = (int) (end * stream.getLengthInMillis());

		long startBytePosition = AudioUtils.getAudioBytePosition(audioFormat, startTime);
		long endBytePosition = AudioUtils.getAudioBytePosition(audioFormat, endTime);

		int sampleSize = audioFormat.getBytesPerSample();
		long readCount = endBytePosition - startBytePosition;

		double delta = command.getDelta();
		float scalar = (float) (delta < 0 ? 1 - -delta : 1 + delta);

		scalar = (float) (1 - delta);

		System.out.println(delta + " " + scalar);
//		System.out.println(startTime + " / " + endTime);
//		System.out.println(startBytePosition + " / " + endBytePosition);
//		System.out.println(readCount);
//		System.out.println();

		byte[] buffer = new byte[8192];
		boolean done = false;
		int readTotal = 0;
		int read;

		try {
			WaveformData data = audioTrack.getWaveformData();

			for (int i = 0; i < 10000; i++) {
				data.posSamples[i] *= scalar;
				data.negSamples[i] *= scalar;
			}

			audioTrack.notifyWaveformDataChange();



//			stream.skip(startBytePosition);
//			newStream.skip(startBytePosition);
//
//			while (!done && (read = stream.read(buffer)) > 0) {
//				readTotal += read;
//
//				if (readTotal >= readCount) {
//					read -= (int) (readTotal - readCount);
//					done = true;
//				}
//
//				// Scale all samples in the chunk.
//				AudioUtils.scaleSampleValues(buffer, read, sampleSize, scalar);
//
//				// TODO: write
//			}
		}
		catch (Exception e) {
			handleException(e, "Adjust audio failed", "Adjust audio failed");
		}
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
}
