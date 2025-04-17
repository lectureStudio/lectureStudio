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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.*;
import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.audio.bus.event.AudioSignalEvent;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.ByteArrayAudioSink;
import org.lecturestudio.core.audio.source.AudioInputStreamSource;
import org.lecturestudio.core.audio.source.ByteArrayAudioSource;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.io.DynamicInputStream;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.MapChangeListener;
import org.lecturestudio.core.util.ObservableMap;
import org.lecturestudio.media.audio.DefaultAudioSink;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.presenter.command.AdjustAudioCaptureLevelCommand;
import org.lecturestudio.presenter.api.view.SoundSettingsView;

public class SoundSettingsPresenter extends Presenter<SoundSettingsView> {

	private final AudioConfiguration audioConfig;

	private final AudioSystemProvider audioSystemProvider;

	private AudioDeviceChangeListener deviceChangeListener;

	private AudioPlayer micTestAudioPlayer;

	private AudioPlayer speakerTestAudioPlayer;

	private AudioRecorder levelRecorder;

	private AudioRecorder testRecorder;

	private AudioSink testAudioSink;

	private BooleanProperty testCapture;

	private BooleanProperty testPlayback;

	private BooleanProperty testSpeaker;

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
		testSpeaker = new BooleanProperty();
		captureEnabled = new BooleanProperty(true);
		playbackEnabled = new BooleanProperty();

		deviceChangeListener = new AudioDeviceChangeListener() {

			@Override
			public void deviceConnected(AudioDevice device) {
				loadDevices();
			}

			@Override
			public void deviceDisconnected(AudioDevice device) {
				loadDevices();
			}
		};

		audioSystemProvider.addDeviceChangeListener(deviceChangeListener);

		testCapture.addListener((o, oldValue, newValue) -> {
			recordCaptureTest(newValue);
		});
		testPlayback.addListener((o, oldValue, newValue) -> {
			try {
				playCaptureTest(newValue);
			}
			catch (Exception e) {
				handleException(e, "Test playback failed",
						"microphone.settings.test.playback.error",
						"microphone.settings.test.playback.error.message");
			}
		});
		testSpeaker.addListener((o, oldValue, newValue) -> {
			try {
				playSpeakerTest(newValue);
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

		AudioProcessingSettings processingSettings = audioConfig.getRecordingProcessingSettings();

		view.setAudioCaptureDevices(audioSystemProvider.getRecordingDevices());
		view.setAudioPlaybackDevices(audioSystemProvider.getPlaybackDevices());
		view.setAudioCaptureDevice(audioConfig.captureDeviceNameProperty());
		view.setAudioPlaybackDevice(audioConfig.playbackDeviceNameProperty());
		view.bindAudioPlaybackLevel(audioConfig.playbackVolumeProperty());
		view.bindAudioCaptureLevel(audioConfig.recordingMasterVolumeProperty());
		view.bindAudioCaptureNoiseSuppressionLevelEnabled(processingSettings
				.enableNoiseSuppressionProperty());
		view.setAudioCaptureNoiseSuppressionLevel(processingSettings
				.noiseSuppressionLevelProperty());
		view.setOnViewVisible(this::onViewVisible);
		view.setOnAdjustAudioCaptureLevel(this::adjustAudioCaptureLevel);
		view.bindTestCaptureEnabled(captureEnabled);
		view.bindTestPlaybackEnabled(playbackEnabled);
		view.setOnTestCapture(testCapture);
		view.setOnTestCapturePlayback(testPlayback);
		view.setOnTestSpeakerPlayback(testSpeaker);
		view.setOnReset(this::reset);
		view.setOnClose(this::close);

		if (audioSystemProvider.getRecordingDevices().length < 1) {
			view.setViewEnabled(false);
		}

		audioConfig.captureDeviceNameProperty().addListener((o, oldDevice, newDevice) -> {
			if (isNull(newDevice)) {
				setDefaultRecordingDevice();
			}
			else if (newDevice.equals(oldDevice)) {
				return;
			}

			recordingDeviceChanged(newDevice);
		});
		audioConfig.playbackDeviceNameProperty().addListener((o, oldDevice, newDevice) -> {
			if (isNull(newDevice)) {
				setDefaultPlaybackDevice();
			}
			else if (newDevice.equals(oldDevice)) {
				return;
			}

			playbackDeviceChanged(newDevice);
		});

		audioConfig.recordingMasterVolumeProperty().addListener((o, oldValue, newValue) -> {
			String deviceName = audioConfig.getCaptureDeviceName();

			if (nonNull(deviceName)) {
				audioConfig.setRecordingVolume(deviceName, newValue);
			}
			if (nonNull(levelRecorder)) {
				levelRecorder.setAudioVolume(newValue);
			}
			if (nonNull(testRecorder)) {
				testRecorder.setAudioVolume(newValue);
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

		audioConfig.playbackVolumeProperty().addListener((o, oldValue, newValue) -> {
			if (captureAudio && nonNull(micTestAudioPlayer)) {
				micTestAudioPlayer.setAudioVolume(newValue);
			}
			if (captureAudio && nonNull(speakerTestAudioPlayer)) {
				speakerTestAudioPlayer.setAudioVolume(newValue);
			}
		});
	}

	@Override
	public void close() {
		super.close();
	}

	private void loadDevices() {
		CompletableFuture.runAsync(() -> {
			AudioDevice[] recordingDevices = audioSystemProvider.getRecordingDevices();

			view.setAudioCaptureDevices(recordingDevices);
			view.setAudioPlaybackDevices(audioSystemProvider.getPlaybackDevices());
			view.setAudioCaptureDevice(audioConfig.captureDeviceNameProperty());
			view.setAudioPlaybackDevice(audioConfig.playbackDeviceNameProperty());
			view.setViewEnabled(nonNull(recordingDevices) && recordingDevices.length > 0);
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
			if (nonNull(speakerTestAudioPlayer)) {
				stopAudioExecutable(speakerTestAudioPlayer);
			}
			if (isNull(micTestAudioPlayer)) {
				micTestAudioPlayer = createAudioPlayer();
			}

			startAudioExecutable(micTestAudioPlayer);
		}
		else {
			stopAudioExecutable(micTestAudioPlayer);

			micTestAudioPlayer = null;
		}
	}

	private void playSpeakerTest(boolean play) {
		if (play) {
			if (nonNull(micTestAudioPlayer)) {
				stopAudioExecutable(micTestAudioPlayer);
			}
			if (isNull(speakerTestAudioPlayer)) {
				try {
					speakerTestAudioPlayer = createSpeakerAudioPlayer();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			startAudioExecutable(speakerTestAudioPlayer);
		}
		else {
			stopAudioExecutable(speakerTestAudioPlayer);

			speakerTestAudioPlayer = null;
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

			if (nonNull(micTestAudioPlayer)) {
				stopAudioExecutable(micTestAudioPlayer);
			}
			if (nonNull(speakerTestAudioPlayer)) {
				stopAudioExecutable(speakerTestAudioPlayer);
			}
		}
	}

	private void setDefaultRecordingDevice() {
		if (audioSystemProvider.getRecordingDevices().length < 1) {
			return;
		}

		AudioDevice captureDevice = null;

		try {
			captureDevice = audioSystemProvider.getDefaultRecordingDevice();
		}
		catch (Throwable e) {
			// Audio device error, e.g., no device connected, will be visible in the view.
		}

		// Select first available capture device.
		if (nonNull(captureDevice)) {
			audioConfig.setCaptureDeviceName(captureDevice.getName());
		}
		else {
			view.setViewEnabled(false);
		}
	}

	private void setDefaultPlaybackDevice() {
		AudioDevice playbackDevice = null;

		try {
			playbackDevice = audioSystemProvider.getDefaultPlaybackDevice();
		}
		catch (Throwable e) {
			// Audio device error, e.g. no device connected, will be visible in the view.
		}

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
		audioConfig.setPlaybackVolume(defaultAudioConfig.getPlaybackVolume());
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
		AudioProcessingSettings settings = new AudioProcessingSettings();
		settings.setHighpassFilterEnabled(true);
		settings.setNoiseSuppressionEnabled(true);
		settings.setNoiseSuppressionLevel(NoiseSuppressionLevel.LOW);
		settings.setEchoCancellerEnabled(true);

		levelRecorder = createAudioRecorder();
		levelRecorder.setAudioProcessingSettings(settings);
		levelRecorder.setAudioSink(new DefaultAudioSink() {

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
		player.setAudioVolume(audioConfig.getPlaybackVolume());
		player.setAudioSource(source);
		player.addStateListener((oldState, newState) -> {
			if (newState == ExecutableState.Stopped) {
				testPlayback.set(false);
			}
		});

		return player;
	}

	private AudioPlayer createSpeakerAudioPlayer() throws IOException {
		ByteArrayInputStream byteStream;

		try (InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("resources/audio/speaker-test.wav")) {
			if (isNull(inputStream)) {
				throw new IOException("Load speaker test file failed");
			}

			byteStream = new ByteArrayInputStream(inputStream.readAllBytes());
		}

		DynamicInputStream stream = new DynamicInputStream(byteStream);
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(stream);

		AudioInputStreamSource audioSource = new AudioInputStreamSource(audioStream,
				audioStream.getAudioFormat());

		AudioPlayer player = audioSystemProvider.createAudioPlayer();
		player.setAudioDeviceName(audioConfig.getPlaybackDeviceName());
		player.setAudioVolume(audioConfig.getPlaybackVolume());
		player.setAudioSource(audioSource);
		player.addStateListener((oldState, newState) -> {
			if (newState == ExecutableState.Stopped) {
				testSpeaker.set(false);
			}
		});

		return player;
	}

	private boolean hasDevice(AudioDevice[] devices, String name) {
		return Arrays.stream(devices)
				.anyMatch(device -> device.getName().equals(name));
	}
}
