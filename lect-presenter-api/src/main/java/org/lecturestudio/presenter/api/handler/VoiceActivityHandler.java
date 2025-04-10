/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;

import dev.onvoid.webrtc.media.audio.VoiceActivityDetector;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.listener.DocumentListChangeListener;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.media.audio.DefaultAudioSink;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.RemindRecordingPresenter;
import org.lecturestudio.presenter.api.presenter.command.CloseablePresenterCommand;

/**
 * Handles voice activity detection for the presenter application.
 * <p>
 * This class manages the recording and processing of audio input to detect
 * voice activity. It uses the WebRTC VoiceActivityDetector to analyze
 * audio signals and determine when voice is present.
 * <p>
 * The handler automatically starts and stops based on the application's
 * notification settings in the presenter configuration.
 *
 * @author Alex Andres
 */
public class VoiceActivityHandler extends PresenterHandler implements DocumentListChangeListener {

	/** Provider for audio system services used to create audio recorders. */
	private final AudioSystemProvider audioSystemProvider;

	/** Provides access to notification, stream, and audio settings. */
	private final PresenterConfiguration config;

	/** Service that manages documents and provides access to the document collection. */
	private final DocumentService documentService;

	/** Audio recorder instance used to capture audio input for voice activity detection. */
	private AudioRecorder recorder;

	/** Detector that analyzes audio data to determine when voice is present. */
	private VoiceActivityDetector activityDetector;

	/** Flag indicating whether the user has explicitly declined the recording reminder notification. */
	private boolean userDeclinedRecording = false;


	/**
	 * Creates a new voice activity handler with the specified context and audio system provider.
	 *
	 * @param context        The presenter context containing application configuration.
	 * @param systemProvider The provider for audio system services.
	 * @param docService     Service that manages documents and provides access to the document collection.
	 */
	public VoiceActivityHandler(PresenterContext context, AudioSystemProvider systemProvider,
								DocumentService docService) {
		super(context);

		config = context.getConfiguration();
		audioSystemProvider = systemProvider;
		documentService = docService;
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = context.getConfiguration();

		AudioProcessingSettings settings = new AudioProcessingSettings();
		settings.setEchoCancellerEnabled(true);
		settings.setHighpassFilterEnabled(true);
		settings.setNoiseSuppressionEnabled(true);
		settings.setNoiseSuppressionLevel(AudioProcessingSettings.NoiseSuppressionLevel.LOW);

		activityDetector = new VoiceActivityDetector();

		recorder = createAudioRecorder();
		recorder.setAudioProcessingSettings(settings);
		recorder.setAudioSink(new DefaultAudioSink() {

			/**
			 * The VoiceActivityDetector expects audio input with a sample rate of 16 kHz.
			 */
			final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 16000, 1);

			/**
			 * Threshold in milliseconds that defines how long voice activity must be sustained
			 * before triggering a voice detection event. The default value of 100 ms helps
			 * filter out short noise bursts.
			 */
			final int voiceDurationThresholdMs = 100;

			/**
			 * Counter that tracks the duration of continuous voice activity in milliseconds.
			 * Increments when voice is detected and resets when voice stops or threshold is reached.
			 */
			int voiceDurationMs = 0;


			@Override
			public int write(byte[] data, int offset, int length) {
				// The data is in 16-bit signed PCM format.
				activityDetector.process(data, data.length / 2, audioFormat.getSampleRate());

				// Get the probability (0.0 to 1.0) that the audio signal contains human voice.
				float probability = activityDetector.getLastVoiceProbability();

				// If voice probability exceeds 90%, consider it as active voice detection.
				// This threshold provides high confidence that human speech is present.
				if (probability > 0.9) {
					// The data packets are processed in 10 ms intervals.
					voiceDurationMs += 10;

					if (voiceDurationMs >= voiceDurationThresholdMs) {
						// Reset the voice duration counter and notify the context about voice activity.
						voiceDurationMs = 0;

						if (isNotificationEnabled()) {
							showRecordNotification();
						}
					}
				}
				else {
					// Reset voice duration counter when voice probability falls below a threshold.
					// This ensures that only sustained voice activity triggers notifications.
					voiceDurationMs = 0;
				}

				return 0;
			}

			@Override
			public AudioFormat getAudioFormat() {
				return audioFormat;
			}
		});

		// Add a listener to the notifyToRecord property to automatically start or stop
		// voice activity detection when the property value changes.
		config.notifyToRecordProperty().addListener((o, oldValue, newValue) -> {
			if (isDetectionRequired()) {
				start();
			}
			else {
				stop();
			}
		});

		// Respond to document events.
		documentService.getDocuments().addListener(this);

		// Start voice activity detection automatically if notifications are enabled and a document is selected.
		if (isDetectionRequired()) {
			start();
		}
	}

	@Override
	public void documentInserted(Document doc) {
		if (isDetectionRequired()) {
			start();
		}
	}

	@Override
	public void documentRemoved(Document doc) {
		// Check if any document remains selected after removal.
		boolean hasDocument = nonNull(documentService.getDocuments().getSelectedDocument());

		// If no document is selected anymore, reset tracking state and stop detection.
		if (!hasDocument) {
			userDeclinedRecording = false;
			stop();
		}
	}

	@Override
	public void documentSelected(Document prevDoc, Document newDoc) {
		// Ignore document selection events.
	}

	@Override
	public void documentReplaced(Document prevDoc, Document newDoc) {
		// Ignore document replacement events.
	}

	/**
	 * Determines whether notification for recording should be shown to the user.
	 * <p>
	 * Notifications are enabled when both conditions are true:
	 * <ul>
	 *   <li>The user has not explicitly declined the recording notification</li>
	 *   <li>The microphone is currently disabled in the stream configuration</li>
	 * </ul>
	 *
	 * @return {@code true} if notifications are to be displayed, {@code false} otherwise.
	 */
	private boolean isNotificationEnabled() {
		return !userDeclinedRecording && !config.getStreamConfig().getMicrophoneEnabled();
	}

	/**
	 * Determines whether voice activity detection is required based on application state.
	 * <p>
	 * Voice activity detection is required when all the following conditions are met:
	 * <ul>
	 *   <li>The "notify to record" setting is enabled in the application configuration</li>
	 *   <li>A document is currently selected</li>
	 *   <li>The audio recorder is not yet running</li>
	 * </ul>
	 *
	 * @return {@code true} if voice activity detection should be active, {@code false} otherwise.
	 */
	private boolean isDetectionRequired() {
		boolean notifySettingEnabled = requireNonNullElse(config.getNotifyToRecord(), false);
		boolean hasDocument = nonNull(documentService.getDocuments().getSelectedDocument());
		boolean notRecording = !recorder.started();

		return notifySettingEnabled && hasDocument && notRecording;
	}

	private void showRecordNotification() {
		context.getEventBus().post(new CloseablePresenterCommand<>(
				RemindRecordingPresenter.class, () -> {
			// User declined, so do not ask again.
			userDeclinedRecording = true;
		}));
	}

	private void start() {
		if (!recorder.started()) {
			try {
				recorder.start();
			}
			catch (ExecutableException e) {
				handleException(e, "Start voice activity detection failed", "generic.error");
			}
		}
	}

	private void stop() {
		if (recorder.started() || recorder.suspended()) {
			try {
				recorder.suspend();
			}
			catch (ExecutableException e) {
				handleException(e, "Stop voice activity detection failed", "generic.error");
			}
		}
	}

	private void destroy() {
		try {
			recorder.destroy();

			activityDetector.dispose();
		}
		catch (ExecutableException e) {
			handleException(e, "Destroy voice activity detection failed", "generic.error");
		}
	}

	private AudioRecorder createAudioRecorder() {
		AudioConfiguration audioConfig = config.getAudioConfig();
		String inputDeviceName = audioConfig.getCaptureDeviceName();
		double volume = audioConfig.getMasterRecordingVolume();
		Double devVolume = audioConfig.getRecordingVolume(inputDeviceName);

		if (nonNull(devVolume)) {
			volume = devVolume;
		}

		AudioRecorder recorder = audioSystemProvider.createAudioRecorder();
		recorder.setAudioDeviceName(inputDeviceName);
		recorder.setAudioVolume(volume);

		return recorder;
	}
}
