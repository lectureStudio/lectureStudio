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

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Objects;

import dev.onvoid.webrtc.media.audio.VoiceActivityDetector;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.*;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.listener.DocumentListChangeListener;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.media.audio.DefaultAudioSink;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ManualStateObserver;
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

	/** Observer that tracks manual state changes affected by user interactions. */
	private final ManualStateObserver manualStateObserver;

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
		manualStateObserver = context.getManualStateObserver();
		audioSystemProvider = systemProvider;
		documentService = docService;
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = context.getConfiguration();

		activityDetector = new VoiceActivityDetector();
		recorder = createAudioRecorder();

		initAudioSink();

		audioSystemProvider.addDeviceChangeListener(new AudioDeviceChangeListener() {

			@Override
			public void deviceConnected(AudioDevice device) {
				try {
					handleConnectedDevice(device);
				}
				catch (Exception e) {
					logException(e, "Failed to handle connected device");
				}
			}

			@Override
			public void deviceDisconnected(AudioDevice device) {
				try {
					handleDisconnectedDevice(device);
				}
				catch (Exception e) {
					logException(e, "Failed to handle disconnected device");
				}
			}
		});

		// Adds a listener to the capture device name property to handle device changes.
		// This listener responds when the audio capture device is changed.
		config.getAudioConfig().captureDeviceNameProperty().addListener((o, oldDevice, newDevice) -> {
			if (isNull(newDevice)) {
				stop();
			}
			else if (newDevice.equals(oldDevice)) {
				return;
			}

			recordingDeviceChanged();
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

		// Reset the userDeclinedRecording flag when the recording is manually suspended or stopped.
		// This ensures that notifications will be shown again.
		manualStateObserver.recordingStartedProperty().addListener((o, oldValue, newValue) -> {
			if (!newValue) {
				resetUserDeclination();
			}
		});
		// When the microphone is muted manually, reset the user's declination status to ensure
		// that the notifications will be shown again on new voice detection.
		manualStateObserver.microphoneActiveProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				resetUserDeclination();
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

		// If no document is selected anymore, reset the tracking state and stop detection.
		if (!hasDocument) {
			resetUserDeclination();
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
		boolean notifyToRecord = context.getNotifyToRecord();
		boolean isMicrophoneEnabled = config.getStreamConfig().getMicrophoneEnabled();

		return notifyToRecord && !userDeclinedRecording && !isMicrophoneEnabled;
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
		boolean notRecording = nonNull(recorder) && !recorder.started();

		return notifySettingEnabled && hasDocument && notRecording;
	}

	/**
	 * Displays a notification reminding the user to start recording when voice activity is detected.
	 */
	private void showRecordNotification() {
		context.getEventBus().post(new CloseablePresenterCommand<>(
				RemindRecordingPresenter.class, () -> {
			// User declined, so do not ask again.
			userDeclinedRecording = true;
		}));
	}

	/**
	 * Resets the user's recording notification declination status.
	 * <p>
	 * This method clears the flag that tracks whether the user has declined
	 * recording notifications, allowing notifications to be shown again
	 * when voice activity is detected.
	 */
	private void resetUserDeclination() {
		userDeclinedRecording = false;
	}

	/**
	 * Starts the voice activity detection process.
	 * <p>
	 * If the recorder fails to start, the exception is handled through the
	 * {@code handleException} method with an appropriate error message.
	 */
	private void start() {
		if (nonNull(recorder) && !recorder.started()) {
			try {
				recorder.start();
			}
			catch (ExecutableException e) {
				handleException(e, "Start voice activity detection failed", "generic.error");
			}
		}
	}

	/**
	 * Suspends the voice activity detection process.
	 * <p>
	 * If the recorder fails to suspend, the exception is handled through the
	 * {@code handleException} method with an appropriate error message.
	 */
	private void stop() {
		if (nonNull(recorder) && (recorder.started() || recorder.suspended())) {
			try {
				recorder.suspend();
			}
			catch (ExecutableException e) {
				handleException(e, "Stop voice activity detection failed", "generic.error");
			}
		}
	}

	/**
	 * Stops and destroys the audio recorder.
	 * <p>
	 * This method ensures that the recorder is properly stopped if it's running or suspended,
	 * and then destroys the recorder instance to free resources. Any exceptions that occur
	 * during this process are caught and ignored to prevent disruption to the application.
	 */
	private void destroy() {
		if (nonNull(recorder)) {
			try {
				if (recorder.started() || recorder.suspended()) {
					recorder.stop();
				}

				recorder.destroy();
			}
			catch (ExecutableException e) {
				// Ignore
			}
		}
	}

	/**
	 * This method is called when the audio recording device configuration changes.
	 */
	private void recordingDeviceChanged() {
		destroy();

		recorder = createAudioRecorder();

		initAudioSink();

		if (isDetectionRequired()) {
			start();
		}
	}

	/**
	 * Initializes the audio sink for voice activity detection.
	 * <p>
	 * This method configures a custom audio sink that processes audio data from
	 * the recorder for voice activity detection. The sink analyzes audio samples
	 * in 10ms intervals, calculating voice probability, and triggers notification
	 * when sustained voice activity is detected.
	 * <p>
	 * The method does nothing if the recorder has not been initialized.
	 */
	private void initAudioSink() {
		if (isNull(recorder)) {
			return;
		}

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
			 * Increments when voice is detected and resets when the voice stops or the threshold
			 * is reached.
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
	}

	/**
	 * Creates and configures an audio recorder using the application's audio configuration.

	 * @return A configured {@link AudioRecorder} instance ready for voice activity detection,
	 *         or {@code null} if no recording device is available
	 */
	private AudioRecorder createAudioRecorder() {
		AudioConfiguration audioConfig = config.getAudioConfig();
		String inputDeviceName = audioConfig.getCaptureDeviceName();

		if (isNull(inputDeviceName) || !checkDeviceConnected(inputDeviceName)) {
			inputDeviceName = getDefaultRecordingDeviceName();
		}
		if (isNull(inputDeviceName)) {
			return null;
		}

		double volume = audioConfig.getMasterRecordingVolume();
		Double devVolume = audioConfig.getRecordingVolume(inputDeviceName);

		if (nonNull(devVolume)) {
			volume = devVolume;
		}

		AudioProcessingSettings settings = new AudioProcessingSettings();
		settings.setEchoCancellerEnabled(true);
		settings.setHighpassFilterEnabled(true);
		settings.setNoiseSuppressionEnabled(true);
		settings.setNoiseSuppressionLevel(AudioProcessingSettings.NoiseSuppressionLevel.LOW);

		AudioRecorder recorder = audioSystemProvider.createAudioRecorder();
		recorder.setAudioDeviceName(inputDeviceName);
		recorder.setAudioVolume(volume);
		recorder.setAudioProcessingSettings(settings);

		return recorder;
	}

	/**
	 * Retrieves the name of the default audio recording device.
	 * <p>
	 * This method first checks if any recording devices are available. If devices exist,
	 * it attempts to get the system's default recording device and returns its name.
	 *
	 * @return The name of the default recording device, or {@code null} if no devices
	 *         are available or if there was an error accessing the device
	 */
	private String getDefaultRecordingDeviceName() {
		if (audioSystemProvider.getRecordingDevices().length < 1) {
			return null;
		}

		AudioDevice captureDevice = null;

		try {
			captureDevice = audioSystemProvider.getDefaultRecordingDevice();
		}
		catch (Throwable e) {
			// Audio device error, e.g., no device connected.
		}

		return nonNull(captureDevice) ? captureDevice.getName() : null;
	}

	/**
	 * Checks if an audio device with the specified name is currently connected to the system.
	 * <p>
	 * This method first verifies that there are any recording devices available.
	 * If devices are present, it checks if any of them match the provided device name.
	 *
	 * @param deviceName The name of the audio device to check for.
	 *
	 * @return {@code true} if a recording device with the given name exists,
	 *         {@code false} if no recording devices are available or none match the name.
	 */
	private boolean checkDeviceConnected(String deviceName) {
		if (audioSystemProvider.getRecordingDevices().length < 1) {
			return false;
		}

		return Arrays.stream(audioSystemProvider.getRecordingDevices())
				.anyMatch(device -> device.getName().equals(deviceName));
	}

	/**
	 * Handles the connection of a new audio device to the system.
	 * <p>
	 * This method checks if there's currently no audio recorder initialized.
	 * If no recorder exists, it triggers the recorder reinitialization process
	 * to potentially use the newly connected device.
	 *
	 * @param device The audio device that was connected to the system.
	 */
	private void handleConnectedDevice(AudioDevice device) {
		if (isNull(recorder)) {
			recordingDeviceChanged();
		}
	}

	/**
	 * Handles the disconnection of an audio device from the system.
	 * <p>
	 * This method checks if the disconnected device matches the currently
	 * configured capture device. If it's the same device, the audio recorder
	 * is destroyed since it can no longer function with the disconnected device.
	 *
	 * @param device The audio device that was disconnected from the system.
	 */
	private void handleDisconnectedDevice(AudioDevice device) {
		AudioConfiguration audioConfig = config.getAudioConfig();
		String deviceConfigName = audioConfig.getCaptureDeviceName();

		if (Objects.equals(device.getName(), deviceConfigName)) {
			// The recording device has been disconnected.
			// Any operation on the audio recorder is not possible anymore.
			destroy();
		}
	}
}
