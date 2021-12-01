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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.inject.Inject;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioProcessingSettings;
import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.bus.event.AudioSignalEvent;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.ByteArrayAudioSink;
import org.lecturestudio.core.audio.source.ByteArrayAudioSource;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.MapChangeListener;
import org.lecturestudio.core.util.ObservableMap;
import org.lecturestudio.core.audio.AudioPlayer;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.presenter.command.AdjustAudioCaptureLevelCommand;
import org.lecturestudio.presenter.api.view.SoundSettingsView;

public class SoundSettingsPresenter extends Presenter<SoundSettingsView> {

	private final AudioConfiguration audioConfig;

	private final AudioSystemProvider audioSystemProvider;

	private AudioPlayer audioPlayer;

	private AudioRecorder levelRecorder;

	private AudioRecorder testRecorder;

	private AudioSink testAudioSink;

	private BooleanProperty testCapture;

	private BooleanProperty testPlayback;

	private BooleanProperty captureEnabled;

	private BooleanProperty playbackEnabled;

	private boolean captureAudio;


	@Inject
	SoundSettingsPresenter(ApplicationContext context, SoundSettingsView view,
			AudioSystemProvider audioSystemProvider) {
		super(context, view);

		this.audioConfig = context.getConfiguration().getAudioConfig();
		this.audioSystemProvider = audioSystemProvider;
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

		if (isNull(audioConfig.getCaptureDeviceName())) {
			setDefaultRecordingDevice();
		}
		if (isNull(audioConfig.getPlaybackDeviceName())) {
			setDefaultPlaybackDevice();
		}
		if (isNull(audioConfig.getRecordingProcessingSettings())) {
			AudioProcessingSettings processingSettings = new AudioProcessingSettings();
			processingSettings.setHighpassFilterEnabled(true);
			processingSettings.setNoiseSuppressionEnabled(true);
			processingSettings.setNoiseSuppressionLevel(NoiseSuppressionLevel.MODERATE);

			audioConfig.setRecordingProcessingSettings(processingSettings);
		}

		view.setAudioCaptureDevices(audioSystemProvider.getRecordingDevices());
		view.setAudioPlaybackDevices(audioSystemProvider.getPlaybackDevices());
		view.setAudioCaptureDevice(audioConfig.captureDeviceNameProperty());
		view.setAudioPlaybackDevice(audioConfig.playbackDeviceNameProperty());
		view.bindAudioCaptureLevel(audioConfig.recordingMasterVolumeProperty());
		view.setAudioCaptureNoiseSuppressionLevel(
				audioConfig.getRecordingProcessingSettings()
						.noiseSuppressionLevelProperty());
		view.setOnViewVisible(this::onViewVisible);
		view.setOnAdjustAudioCaptureLevel(this::adjustAudioCaptureLevel);
		view.bindTestCaptureEnabled(captureEnabled);
		view.bindTestPlaybackEnabled(playbackEnabled);
		view.setOnTestCapture(testCapture);
		view.setOnTestCapturePlayback(testPlayback);
		view.setOnReset(this::reset);
		view.setOnClose(this::close);

		if (audioSystemProvider.getRecordingDevices().length < 1) {
			view.setViewEnabled(false);
		}

		audioConfig.captureDeviceNameProperty().addListener((observable, oldDevice, newDevice) -> {
			if (isNull(newDevice)) {
				setDefaultRecordingDevice();
			}
			else if (newDevice.equals(oldDevice)) {
				return;
			}

			recordingDeviceChanged(newDevice);
		});
		audioConfig.playbackDeviceNameProperty().addListener((observable, oldDevice, newDevice) -> {
			if (isNull(newDevice)) {
				setDefaultPlaybackDevice();
			}
			else if (newDevice.equals(oldDevice)) {
				return;
			}

			playbackDeviceChanged(newDevice);
		});

		audioConfig.recordingMasterVolumeProperty().addListener((observable, oldValue, newValue) -> {
			String deviceName = audioConfig.getCaptureDeviceName();

			if (nonNull(deviceName)) {
				audioConfig.setRecordingVolume(deviceName, newValue);
			}
			if (nonNull(levelRecorder)) {
				levelRecorder.setAudioVolume(newValue.doubleValue());
			}
			if (nonNull(testRecorder)) {
				testRecorder.setAudioVolume(newValue.doubleValue());
			}
		});

		audioConfig.getRecordingVolumes().addListener(new MapChangeListener<>() {

			@Override
			public void mapChanged(ObservableMap<String, Double> map) {
				Double deviceVolume = nonNull(levelRecorder) ?
						map.get(audioConfig.getCaptureDeviceName()) :
						null;

				if (nonNull(deviceVolume)) {
					audioConfig.setMasterRecordingVolume(deviceVolume.floatValue());
				}
			}
		});
	}

	private void adjustAudioCaptureLevel() {
		context.getEventBus().post(new AdjustAudioCaptureLevelCommand(() -> {
			// When determining a new microphone level, do it with the maximum volume.
			if (nonNull(levelRecorder)) {
				levelRecorder.setAudioVolume(1);
			}
		}, () -> {
			// Reset capture volume, when canceled.
			if (nonNull(levelRecorder)) {
				Double devVolume = audioConfig.getRecordingVolume(audioConfig
						.getCaptureDeviceName());

				if (isNull(devVolume)) {
					devVolume = (double) audioConfig.getMasterRecordingVolume();
				}

				levelRecorder.setAudioVolume(devVolume);
			}
		}));
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

	private void onViewVisible(boolean capture) {
		if (captureAudio == capture) {
			return;
		}

		captureAudio = capture;

		if (capture) {
			if (!hasDevice(audioSystemProvider.getRecordingDevices(),
					audioConfig.getCaptureDeviceName())) {
				setDefaultRecordingDevice();
			}
			if (!hasDevice(audioSystemProvider.getPlaybackDevices(),
					audioConfig.getPlaybackDeviceName())) {
				setDefaultPlaybackDevice();
			}

			startAudioLevelCapture();
		}
		else {
			stopAudioLevelCapture();
		}
	}

	private void setDefaultRecordingDevice() {
		AudioDevice captureDevice = audioSystemProvider.getDefaultRecordingDevice();

		// Select first available capture device.
		if (nonNull(captureDevice)) {
			audioConfig.setCaptureDeviceName(captureDevice.getName());
		}
		else {
			view.setViewEnabled(false);
		}
	}

	private void setDefaultPlaybackDevice() {
		AudioDevice playbackDevice = audioSystemProvider.getDefaultPlaybackDevice();

		// Select first available playback device.
		if (nonNull(playbackDevice)) {
			audioConfig.setPlaybackDeviceName(playbackDevice.getName());
		}
	}

	private void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();
		AudioConfiguration defaultAudioConfig = defaultConfig.getAudioConfig();
		AudioProcessingSettings defaultProcSettings = defaultAudioConfig.getRecordingProcessingSettings();

		audioConfig.getRecordingProcessingSettings().setNoiseSuppressionLevel(defaultProcSettings.getNoiseSuppressionLevel());
		audioConfig.setCaptureDeviceName(defaultAudioConfig.getCaptureDeviceName());
		audioConfig.setPlaybackDeviceName(defaultAudioConfig.getPlaybackDeviceName());
		audioConfig.setDefaultRecordingVolume(defaultAudioConfig.getDefaultRecordingVolume());
		audioConfig.setMasterRecordingVolume(defaultAudioConfig.getMasterRecordingVolume());
		audioConfig.getRecordingVolumes().clear();
	}

	private void recordingDeviceChanged(String name) {
		Double deviceVolume = audioConfig.getRecordingVolume(name);

		if (nonNull(deviceVolume)) {
			audioConfig.setMasterRecordingVolume(deviceVolume.floatValue());
		}

		stopAudioLevelCapture();
		startAudioLevelCapture();
	}

	private void playbackDeviceChanged(String name) {

	}

	private void startAudioLevelCapture() {
		levelRecorder = createAudioRecorder();
		levelRecorder.setAudioSink(new AudioSink() {

			@Override
			public void open() {}

			@Override
			public void reset() {}

			@Override
			public void close() {}

			@Override
			public int write(byte[] data, int offset, int length) {
				double level = getSignalPowerLevel(data);
				view.setAudioCaptureLevel(level);

				context.getAudioBus().post(new AudioSignalEvent(level));

				return 0;
			}

			@Override
			public AudioFormat getAudioFormat() {
				return audioConfig.getRecordingFormat();
			}

			@Override
			public void setAudioFormat(AudioFormat format) {}

			private double getSignalPowerLevel(byte[] buffer) {
				int max = 0;

				for (int i = 0; i < buffer.length; i += 2) {
					int value = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));

					max = Math.max(max, Math.abs(value));
				}

				return max / 32767.0;
			}
		});

		startAudioExecutable(levelRecorder);
	}

	private void stopAudioLevelCapture() {
		if (nonNull(levelRecorder) && levelRecorder.started()) {
			stopAudioExecutable(levelRecorder);
		}
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

	private boolean hasDevice(AudioDevice[] devices, String name) {
		return Arrays.stream(devices)
				.anyMatch(device -> device.getName().equals(name));
	}
}
