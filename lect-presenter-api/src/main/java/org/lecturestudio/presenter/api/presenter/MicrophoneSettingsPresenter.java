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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.avdev.AudioSink;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.audio.bus.event.AudioSignalEvent;
import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.audio.source.AudioSource;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.MapChangeListener;
import org.lecturestudio.core.util.ObservableMap;
import org.lecturestudio.media.avdev.AVdevAudioInputDevice;
import org.lecturestudio.media.avdev.AVdevAudioOutputDevice;
import org.lecturestudio.media.avdev.AvdevAudioPlayer;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.presenter.command.AdjustAudioCaptureLevelCommand;
import org.lecturestudio.presenter.api.view.MicrophoneSettingsView;

public class MicrophoneSettingsPresenter extends Presenter<MicrophoneSettingsView> {

	private final AudioConfiguration audioConfig;

	private final String soundSystem;

	private AvdevAudioPlayer audioPlayer;

	private AVdevAudioInputDevice levelDevice;

	private ByteArrayInputStream testPlaybackStream;

	private ByteArrayOutputStream testCaptureStream;

	private BooleanProperty testCapture;

	private BooleanProperty testPlayback;

	private BooleanProperty captureEnabled;

	private BooleanProperty playbackEnabled;

	private boolean captureAudio;


	@Inject
	MicrophoneSettingsPresenter(ApplicationContext context, MicrophoneSettingsView view) {
		super(context, view);

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		audioConfig = config.getAudioConfig();
		soundSystem = audioConfig.getSoundSystem();
	}

	@Override
	public void initialize() {
		setAudioCaptureDevices(soundSystem);

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
				handleException(e, "Test playback failed", "microphone.settings.test.playback.error");
			}
		});

		view.setAudioCaptureDevice(audioConfig.inputDeviceNameProperty());
		view.bindAudioCaptureLevel(audioConfig.recordingMasterVolumeProperty());
		view.setOnViewVisible(this::captureAudioLevel);
		view.setOnAdjustAudioCaptureLevel(this::adjustAudioCaptureLevel);
		view.bindTestCaptureEnabled(captureEnabled);
		view.bindTestPlaybackEnabled(playbackEnabled);
		view.setOnTestCapture(testCapture);
		view.setOnTestPlayback(testPlayback);
		view.setOnReset(this::reset);
		view.setOnClose(this::close);

		if (isNull(audioConfig.getInputDeviceName())) {
			setDefaultCaptureDevice();
		}
		if (AudioUtils.getAudioCaptureDevices(soundSystem).length < 1) {
			view.setViewEnabled(false);
		}

		audioConfig.inputDeviceNameProperty().addListener((observable, oldDevice, newDevice) -> {
			if (isNull(newDevice)) {
				setDefaultCaptureDevice();
			}
			else if (newDevice.equals(oldDevice)) {
				return;
			}

			recordingDeviceChanged(newDevice);
		});

		audioConfig.soundSystemProperty().addListener((observable, oldSystem, newSystem) -> {
			setAudioCaptureDevices(newSystem);
		});

		audioConfig.recordingMasterVolumeProperty().addListener((observable, oldValue, newValue) -> {
			String deviceName = audioConfig.getInputDeviceName();

			if (nonNull(deviceName)) {
				audioConfig.setRecordingVolume(deviceName, newValue);
			}
			if (nonNull(levelDevice)) {
				levelDevice.setVolume(newValue.doubleValue());
			}
		});

		audioConfig.getRecordingVolumes().addListener(new MapChangeListener<>() {

			@Override
			public void mapChanged(ObservableMap<String, Double> map) {
				Double deviceVolume = nonNull(levelDevice) ?
						map.get(levelDevice.getName()) :
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
			if (nonNull(levelDevice)) {
				levelDevice.setVolume(1);
			}
		}, () -> {
			// Reset capture volume, when canceled.
			if (nonNull(levelDevice)) {
				Double devVolume = audioConfig.getRecordingVolume(levelDevice.getName());

				if (isNull(devVolume)) {
					devVolume = (double) audioConfig.getMasterRecordingVolume();
				}

				levelDevice.setVolume(devVolume);
			}
		}));
	}

	private void recordCaptureTest(boolean capture) {
		playbackEnabled.set(!capture);

		if (capture) {
			testCaptureStream.reset();
		}
	}

	private void playCaptureTest(boolean play) throws Exception {
		captureEnabled.set(!play);

		if (play) {
			testPlaybackStream = new ByteArrayInputStream(testCaptureStream.toByteArray());

			if (isNull(audioPlayer)) {
				AudioOutputDevice device = AudioUtils.getDefaultAudioPlaybackDevice(soundSystem);

				if (nonNull(device)) {
					audioPlayer = new AvdevAudioPlayer(
							(AVdevAudioOutputDevice) device,
							new TestAudioSource(), new SyncState());
				}
				else {
					showError("microphone.settings.test.playback.error",
							"microphone.settings.test.playback.error.message");
				}
			}

			audioPlayer.start();
		}
		else {
			audioPlayer.stop();
		}
	}

	private void setAudioCaptureDevices(String soundSystem) {
		view.setAudioCaptureDevices(AudioUtils.getAudioCaptureDevices(soundSystem));
	}

	private void captureAudioLevel(boolean capture) {
		if (captureAudio == capture) {
			return;
		}

		captureAudio = capture;

		if (capture) {
			startAudioLevelCapture();
		}
		else {
			stopAudioLevelCapture();
		}
	}

	private void setDefaultCaptureDevice() {
		AudioInputDevice[] captureDevices = AudioUtils.getAudioCaptureDevices(soundSystem);

		// Select first available capture device.
		if (captureDevices.length > 0) {
			audioConfig.setInputDeviceName(captureDevices[0].getName());
		}
		else {
			view.setViewEnabled(false);
		}
	}

	private void reset() {
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		audioConfig.setSoundSystem(defaultConfig.getAudioConfig().getSoundSystem());
		audioConfig.setInputDeviceName(defaultConfig.getAudioConfig().getInputDeviceName());
		audioConfig.setDefaultRecordingVolume(defaultConfig.getAudioConfig().getDefaultRecordingVolume());
		audioConfig.setMasterRecordingVolume(defaultConfig.getAudioConfig().getMasterRecordingVolume());
		audioConfig.getRecordingVolumes().clear();
	}

	private void recordingDeviceChanged(String name) {
		if (nonNull(levelDevice) && levelDevice.getName().equals(name)) {
			return;
		}

		Double deviceVolume = audioConfig.getRecordingVolume(name);

		if (nonNull(deviceVolume)) {
			audioConfig.setMasterRecordingVolume(deviceVolume.floatValue());
		}

		stopAudioLevelCapture();
		startAudioLevelCapture();
	}

	private void startAudioLevelCapture() {
		String inputDeviceName = audioConfig.getInputDeviceName();

		if (!soundSystem.equals("AVdev")) {
			return;
		}
		if (!AudioUtils.hasAudioCaptureDevice(soundSystem, inputDeviceName)) {
			// Select default device.
			AudioInputDevice[] devices = AudioUtils.getAudioCaptureDevices(soundSystem);

			inputDeviceName = (devices.length > 0) ? devices[0].getName() : null;
		}
		if (isNull(inputDeviceName)) {
			return;
		}

		AudioFormat format = audioConfig.getRecordingFormat();
		AudioInputDevice inputDevice = AudioUtils.getAudioInputDevice(soundSystem, inputDeviceName);

		double volume = audioConfig.getMasterRecordingVolume();
		Double devVolume = audioConfig.getRecordingVolume(inputDeviceName);

		if (nonNull(devVolume)) {
			volume = devVolume;
		}

		testCaptureStream = new ByteArrayOutputStream();

		levelDevice = (AVdevAudioInputDevice) inputDevice;
		levelDevice.setSink(new AudioSink() {

			@Override
			public void write(byte[] data, int length) {
				double level = getSignalPowerLevel(data);

				if (testCapture.get()) {
					testCaptureStream.write(data, 0, length);
				}

				view.setAudioCaptureLevel(level);

				context.getAudioBus().post(new AudioSignalEvent(level));
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
		levelDevice.setAudioFormat(format);
		levelDevice.setMute(false);
		levelDevice.setVolume(volume);

		try {
			levelDevice.open();
			levelDevice.start();
		}
		catch (Exception e) {
			logException(e, "Start audio capture device failed");
		}
	}

	private void stopAudioLevelCapture() {
		if (nonNull(levelDevice)) {
			try {
				levelDevice.stop();
				levelDevice.close();
				levelDevice = null;
			}
			catch (Exception e) {
				logException(e, "Stop audio capture device failed");
			}
		}
	}



	private class TestAudioSource implements AudioSource {

		@Override
		public int read(byte[] data, int offset, int length) {
			int read = testPlaybackStream.read(data, offset, length);
			if (read < 0) {
				CompletableFuture.runAsync(() -> testPlayback.set(false));
			}

			return read;
		}

		@Override
		public void close() throws IOException {
			testPlaybackStream.close();
		}

		@Override
		public void reset() {
			testPlaybackStream.reset();
		}

		@Override
		public long skip(long n) {
			return testPlaybackStream.skip(n);
		}

		@Override
		public long getInputSize() {
			return testPlaybackStream.available();
		}

		@Override
		public AudioFormat getAudioFormat() {
			return levelDevice.getAudioFormat();
		}
	}
}
