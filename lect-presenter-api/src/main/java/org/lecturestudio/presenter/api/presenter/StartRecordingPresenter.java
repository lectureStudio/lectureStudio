/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioPlayer;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.ByteArrayAudioSink;
import org.lecturestudio.core.audio.source.ByteArrayAudioSource;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.camera.*;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.media.camera.CameraService;
import org.lecturestudio.presenter.api.config.CameraRecordingConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.StartRecordingView;

public class StartRecordingPresenter extends Presenter<StartRecordingView> {

	private final AudioSystemProvider audioSystemProvider;

	private final CameraRecordingConfiguration cameraRecordingConfig;

	private final CameraService camService;

	private ChangeListener<String> camListener;

	private boolean capture;

	private final AudioConfiguration audioConfig;

	/** The action that is executed when the saving process has been aborted. */
	private Action startAction;

	private AudioRecorder testRecorder;

	private AudioPlayer audioPlayer;

	private AudioSink testAudioSink;

	private BooleanProperty testCapture;

	private BooleanProperty testPlayback;

	private BooleanProperty captureEnabled;

	private BooleanProperty playbackEnabled;


	@Inject
	StartRecordingPresenter(PresenterContext context, StartRecordingView view,
			AudioSystemProvider audioSystemProvider,
			CameraService camService) {
		super(context, view);

		this.camService = camService;
		this.cameraRecordingConfig = context.getConfiguration().getCameraRecordingConfig();
		this.audioSystemProvider = audioSystemProvider;
		this.audioConfig = context.getConfiguration().getAudioConfig();
	}

	@Override
	public void initialize() {
		testCapture = new BooleanProperty();
		testPlayback = new BooleanProperty();
		captureEnabled = new BooleanProperty(true);
		playbackEnabled = new BooleanProperty();

		testCapture.addListener((observable, oldValue, newValue) -> {
			recordCaptureTest(newValue);
		});
		testPlayback.addListener((observable, oldValue, newValue) -> {
			try {
				playCaptureTest(newValue);
			}
			catch (Exception e) {
				handleException(e, "Test playback failed",
						"microphone.settings.test.playback.error",
						"microphone.settings.test.playback.error.message");
			}
		});

		validateMicrophone();
		validateSpeaker();

		camListener = (observable, oldCamera, newCamera) -> {
			if (nonNull(newCamera)) {
				setViewCamera(newCamera);
			}
		};
		cameraRecordingConfig.cameraNameProperty().addListener(camListener);

		view.setAudioCaptureDevices(audioSystemProvider.getRecordingDevices());
		view.setAudioCaptureDevice(audioConfig.captureDeviceNameProperty());
		view.setAudioPlaybackDevices(audioSystemProvider.getPlaybackDevices());
		view.setAudioPlaybackDevice(audioConfig.playbackDeviceNameProperty());
		view.setEnableCamera(cameraRecordingConfig.enableCameraProperty());
		view.setOnViewVisible(this::captureCamera);
		view.setCameraNames(camService.getCameraNames());
		view.setCameraName(cameraRecordingConfig.cameraNameProperty());
		setViewCamera(cameraRecordingConfig.getCameraName());
		view.setAudioTestCaptureEnabled(captureEnabled);
		view.setAudioTestPlaybackEnabled(playbackEnabled);
		view.setOnAudioTestCapture(testCapture);
		view.setOnAudioTestCapturePlayback(testPlayback);
		view.setOnStart(this::onStart);
		view.setOnClose(this::close);
	}

	@Override
	public void close() {
		dispose();
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnStart(Action action) {
		startAction = action;
	}

	private void onStart() {
		dispose();

		if (nonNull(startAction)) {
			startAction.execute();
		}
	}

	private void dispose() {
		cameraRecordingConfig.cameraNameProperty().removeListener(camListener);
		captureCamera(false);
		stopAudioCapture();

		super.close();
	}

	private void validateMicrophone() {
		var devices = audioSystemProvider.getRecordingDevices();

		// Check if the recently used microphone is connected.
		if (missingDevice(devices, audioConfig.getCaptureDeviceName())) {
			var device = audioSystemProvider.getDefaultRecordingDevice();

			if (nonNull(device)) {
				// Select the system's default microphone.
				audioConfig.setCaptureDeviceName(device.getName());
			}
			else if (devices.length > 0) {
				// Select the first available microphone.
				audioConfig.setCaptureDeviceName(devices[0].getName());
			}
		}
	}

	private void validateSpeaker() {
		var devices = audioSystemProvider.getPlaybackDevices();

		// Check if the recently used speaker is connected.
		if (missingDevice(devices, audioConfig.getPlaybackDeviceName())) {
			var device = audioSystemProvider.getDefaultPlaybackDevice();

			if (nonNull(device)) {
				// Select the system's default speaker.
				audioConfig.setPlaybackDeviceName(device.getName());
			}
			else if (devices.length > 0) {
				// Select the first available speaker.
				audioConfig.setPlaybackDeviceName(devices[0].getName());
			}
		}
	}

	private boolean missingDevice(AudioDevice[] devices, String deviceName) {
		if (isNull(deviceName)) {
			return true;
		}

		return Arrays.stream(devices)
				.noneMatch(device -> device.getName().equals(deviceName));
	}

	private void recordCaptureTest(boolean capture) {
		playbackEnabled.set(!capture);

		if (capture) {
			testAudioSink = new ByteArrayAudioSink();
			testAudioSink.setAudioFormat(audioConfig.getRecordingFormat());

			testRecorder = createAudioRecorder();
			testRecorder.setAudioSink(testAudioSink);
			testRecorder.setAudioProcessingSettings(
					audioConfig.getRecordingProcessingSettings());

			startAudioExecutable(testRecorder);
		}
		else {
			stopAudioExecutable(testRecorder);
		}
	}

	private void playCaptureTest(boolean play) {
		captureEnabled.set(!play);

		if (play) {
			if (isNull(audioPlayer)) {
				audioPlayer = createAudioPlayer();
			}

			startAudioExecutable(audioPlayer);
		}
		else {
			stopAudioExecutable(audioPlayer);

			audioPlayer = null;
		}
	}

	private void stopAudioCapture() {
		if (nonNull(testRecorder) && testRecorder.started()) {
			// This will update the view and the model.
			testCapture.set(false);
		}
	}

	private void startAudioExecutable(Executable executable) {
		try {
			executable.start();
		}
		catch (Exception e) {
			logException(e, "Start audio executable failed");
		}
	}

	private void stopAudioExecutable(Executable executable) {
		if (executable.started() || executable.suspended()) {
			try {
				executable.stop();
				executable.destroy();
			}
			catch (Exception e) {
				logException(e, "Stop audio executable failed");
			}
		}
	}

	private AudioRecorder createAudioRecorder() {
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

	private AudioPlayer createAudioPlayer() {
		ByteArrayAudioSink sink = (ByteArrayAudioSink) testAudioSink;
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				sink.toByteArray());

		ByteArrayAudioSource source = new ByteArrayAudioSource(inputStream,
				audioConfig.getRecordingFormat());

		AudioPlayer player = audioSystemProvider.createAudioPlayer();
		player.setAudioDeviceName(audioConfig.getPlaybackDeviceName());
		player.setAudioVolume(1.0);
		player.setAudioSource(source);
		player.addStateListener((oldState, newState) -> {
			if (newState == ExecutableState.Stopped) {
				testPlayback.set(false);
			}
		});

		return player;
	}

	private void captureCamera(boolean capture) {
		CompletableFuture.runAsync(() -> {
					if (this.capture == capture) {
						return;
					}

					this.capture = capture;

					if (capture) {
						startCameraPreview();
					} else {
						stopCameraPreview();
					}
				})
				.exceptionally(e -> {
					logException(e, "Start/stop camera failed");
					return null;
				});
	}

	private void startCameraPreview() {
		try {
			view.setCameraStatus(context.getDictionary()
					.get("start.stream.camera.starting"));
			view.startCameraPreview();
			view.setCameraStatus(null);
		} catch (Throwable e) {
			view.setCameraStatus(context.getDictionary()
					.get("start.stream.camera.unavailable"));
		}
	}

	private void stopCameraPreview() {
		view.stopCameraPreview();
	}

	private void setViewCamera(String cameraName) {
		CompletableFuture.runAsync(() -> {
					if (capture) {
						stopCameraPreview();
					}

					Camera camera = camService.getCamera(cameraName);

					if (nonNull(camera)) {
						view.setCamera(camera);

						if (camera.isOpened()) {
							return;
						}

						VideoCodecConfiguration cameraConfig = cameraRecordingConfig.getCameraCodecConfig();

						AspectRatio ratio = AspectRatio.forRect(cameraConfig.getViewRect());
						CameraProfile[] profiles = CameraProfiles.forRatio(ratio);
						CameraProfile profile = getCameraProfile(profiles);

						if (isNull(profile)) {
							profile = profiles[profiles.length - 1];
						}

						view.setCameraFormat(profile.getFormat());

						if (capture) {
							startCameraPreview();
						}
					}
				})
				.exceptionally(e -> {
					logException(e, "Start camera failed");
					return null;
				});
	}

	private CameraProfile getCameraProfile(CameraProfile[] profiles) {
		Rectangle2D rect = cameraRecordingConfig.getCameraCodecConfig().getViewRect();

		if (isNull(rect)) {
			return null;
		}

		return Arrays.stream(profiles).filter(p -> {
			return p.getFormat().getWidth() == rect.getWidth()
					&& p.getFormat().getHeight() == rect.getHeight();
		}).findFirst().orElse(null);
	}
}
