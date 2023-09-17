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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioPlayer;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.effect.DenoiseEffectRunner;
import org.lecturestudio.core.audio.effect.NoiseReductionParameters;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.WavFileSink;
import org.lecturestudio.core.audio.source.AudioInputStreamSource;
import org.lecturestudio.core.audio.source.RandomAccessAudioSource;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.bus.event.ProgressEvent;
import org.lecturestudio.core.io.ByteArrayChannel;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.presenter.command.NoiseReductionProgressCommand;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.NoiseReductionSettingsView;
import org.lecturestudio.media.audio.SpectrogramBuilder;
import org.lecturestudio.media.recording.RecordingEvent;

public class NoiseReductionSettingsPresenter extends Presenter<NoiseReductionSettingsView> {

	private static final int SNIPPET_LENGTH_MS = 10000;

	private static final int MIN_MS_PROFILE = 100;

	/** Used to start AudioPlayer. */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	private final AudioSystemProvider audioSystemProvider;

	private final RecordingFileService recordingService;

	private final NoiseReductionParameters effectParams;

	private DoubleProperty sensitivity;

	private BooleanProperty playAudioSnippet;

	private AudioPlayer audioPlayer;


	@Inject
	NoiseReductionSettingsPresenter(ApplicationContext context,
			NoiseReductionSettingsView view,
			RecordingFileService recordingService,
			AudioSystemProvider audioSystemProvider) {
		super(context, view);

		this.recordingService = recordingService;
		this.audioSystemProvider = audioSystemProvider;
		this.effectParams = new NoiseReductionParameters();
	}

	@Override
	public void initialize() {
		sensitivity = new DoubleProperty(0.1);
		sensitivity.addListener((observable, oldValue, newValue) -> {
			effectParams.setThreshold(newValue.floatValue());
		});

		playAudioSnippet = new BooleanProperty();
		playAudioSnippet.addListener((observable, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(newValue)) {
				startAudioSnippetPlayback();
			}
			else {
				stopAudioSnippetPlayback();
			}
		});

		view.bindSensitivity(sensitivity);
		view.bindPlayAudioSnippet(playAudioSnippet);
		view.setNoiseReductionEnabled(false);
		view.setOnUpdateAudioSnippet(this::updateAudioSnippet);
		view.setOnSaveProfileSelection(this::onSaveProfileSelection);
		view.setOnTrialDenoise(this::onTrialDenoise);
		view.setOnFinalDenoise(this::onFinalDenoise);

		context.getEventBus().register(this);
	}

	@Override
	public void destroy() {
		context.getEventBus().unregister(this);
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.selected()) {
			view.setNoiseReductionEnabled(false);

			updateAudioSnippet();
		}
	}

	private void onSaveProfileSelection() {
		EditorContext editorContext = (EditorContext) context;
		Recording recording = recordingService.getSelectedRecording();

		double timeSelect1 = editorContext.getLeftSelection();
		double timeSelect2 = editorContext.getRightSelection();

		long duration = recording.getRecordedAudio().getAudioStream().getLengthInMillis();
		long start = (long) (Math.min(timeSelect1, timeSelect2) * duration);
		long end = (long) (Math.max(timeSelect1, timeSelect2) * duration);
		long selectedLength = end - start;

		if (selectedLength < MIN_MS_PROFILE) {
			Dictionary dict = context.getDictionary();

			context.showNotification(NotificationType.WARNING,
					dict.get("noise.reduction.profile.error"),
					MessageFormat.format(dict.get("noise.reduction.profile.min.error"),
							MIN_MS_PROFILE));
		}
		else {
			effectParams.setProfileTimeInterval(new Interval<>(start, end));

			view.setNoiseReductionEnabled(true);
		}
	}

	private void onAudioStateChange(ExecutableState oldState, ExecutableState newState) {
		playAudioSnippet.set(newState == ExecutableState.Started);
	}

	private void startAudioSnippetPlayback() {
		// Run async to avoid running in the UI thread.
		CompletableFuture.runAsync(() -> {
			if (!audioPlayer.started()) {
				try {
					audioPlayer.start();
				}
				catch (ExecutableException e) {
					logException(e, "Start audio player failed");
				}
			}
		}, executorService);
	}

	private void stopAudioSnippetPlayback() {
		if (audioPlayer.started()) {
			try {
				audioPlayer.stop();
			}
			catch (ExecutableException e) {
				logException(e, "Stop audio player failed");
			}
		}
	}

	private void shutdownPlayback() {
		if (nonNull(audioPlayer)) {
			try {
				audioPlayer.destroy();
			}
			catch (ExecutableException e) {
				logException(e, "Destroy audio player failed");
			}
			audioPlayer = null;
		}
	}

	private void onTrialDenoise() {
		Recording recording = recordingService.getSelectedRecording();
		RandomAccessAudioStream stream = recording.getRecordedAudio().getAudioStream().clone();
		RandomAccessAudioSource source = new RandomAccessAudioSource(stream);

		// Create audio effect output buffer.
		ByteArrayChannel byteChannel = new ByteArrayChannel();

		// Set input stream parameters.
		if (nonNull(effectParams.getTimeInterval())) {
			stream.addExclusiveMillis(effectParams.getTimeInterval());
		}

		AudioSink sink = new WavFileSink(byteChannel);
		sink.setAudioFormat(stream.getAudioFormat());

		DenoiseEffectRunner effectRunner = new DenoiseEffectRunner(effectParams, source, sink);
		effectRunner.addProgressListener(event -> {
			onNoiseReductionPreviewProgress(event, byteChannel);
		});
		effectRunner.addStateListener(this::onNoiseReductionPreviewState);

		try {
			effectRunner.start();
		}
		catch (ExecutableException e) {
			handleException(e, "Render noise reduction preview failed",
					"noise.reduction.error");
		}
	}

	private void onFinalDenoise() {
		NoiseReductionParameters params = effectParams.clone();
		// Apply effect to the whole length.
		params.setTimeInterval(null);

		context.getEventBus().post(new NoiseReductionProgressCommand(params));
	}

	private void onNoiseReductionPreviewState(ExecutableState prevState, ExecutableState state) {
		view.setNoiseReductionRunning(state == ExecutableState.Started);
	}

	private void onNoiseReductionPreviewProgress(ProgressEvent event, ByteArrayChannel byteChannel) {
		if (event.running()) {
			view.setNoiseReductionProgress(event.getProgress());
		}
		else if (event.finished()) {
			view.setNoiseReductionRunning(false);

			try {
				DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(byteChannel.toByteArray()));
				RandomAccessAudioStream audioStream = new RandomAccessAudioStream(stream);

				initAudioPlayer(audioStream.clone());
				updateSpectrogram(audioStream);
			}
			catch (Exception e) {
				handleException(e, "Update noise reduction preview failed",
						"noise.reduction.error");
			}
		}
	}

	private void initAudioPlayer(RandomAccessAudioStream stream) {
		shutdownPlayback();

		AudioInputStreamSource audioSource = new AudioInputStreamSource(stream,
				stream.getAudioFormat());

		try {
			Configuration config = context.getConfiguration();
			String outputDeviceName = config.getAudioConfig().getPlaybackDeviceName();

			audioPlayer = audioSystemProvider.createAudioPlayer();
			audioPlayer.setAudioVolume(1.0);
			audioPlayer.setAudioDeviceName(outputDeviceName);
			audioPlayer.setAudioSource(audioSource);
			audioPlayer.addStateListener(this::onAudioStateChange);
		}
		catch (Exception e) {
			logException(e, "Initialize audio player failed");
		}
	}

	private void updateAudioSnippet() {
		EditorContext editorContext = (EditorContext) context;
		Recording recording = recordingService.getSelectedRecording();
		RandomAccessAudioStream stream = recording.getRecordedAudio().getAudioStream().clone();

		long durationMillis = stream.getLengthInMillis();
		long start = (long) (editorContext.getPrimarySelection() * durationMillis);
		long end = start + SNIPPET_LENGTH_MS;

		effectParams.setTimeInterval(new Interval<>(start, end));

		stream.addExclusiveMillis(new Interval<>(start, end));

		initAudioPlayer(stream.clone());
		updateSpectrogram(stream);
	}

	private void updateSpectrogram(RandomAccessAudioStream audioStream) {
		CompletableFuture.supplyAsync(() -> {
			final int width = 600;
			final int height = 1024;

			SpectrogramBuilder builder = new SpectrogramBuilder();

			try {
				return builder.build(audioStream, width, height);
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}
		})
		.thenAccept(view::setSpectrogram)
		.exceptionally(e -> {
			logException(e, "Create spectrogram failed");
			return null;
		});
	}
}
