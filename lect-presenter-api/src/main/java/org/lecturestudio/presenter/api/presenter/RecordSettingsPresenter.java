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

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.app.configuration.ScreenCaptureConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.screencapture.ScreenCaptureFormat;
import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.config.DefaultConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.RecordSettingsView;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class RecordSettingsPresenter extends Presenter<RecordSettingsView> {

	private final AudioConfiguration audioConfig;
	private final ScreenCaptureConfiguration screenCaptureConfig;

	private final ViewContextFactory viewFactory;


	@Inject
	RecordSettingsPresenter(ApplicationContext context, RecordSettingsView view, ViewContextFactory viewFactory) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.audioConfig = context.getConfiguration().getAudioConfig();
		this.screenCaptureConfig = context.getConfiguration().getScreenCaptureConfig();
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		String soundSystemName = audioConfig.getSoundSystem();
		String inputDeviceName = audioConfig.getInputDeviceName();

		loadAudioFormats(soundSystemName, inputDeviceName);
		loadScreenCaptureFormats();

		view.setNotifyToRecord(config.notifyToRecordProperty());
		view.setConfirmStopRecording(config.confirmStopRecordingProperty());
		view.setPageRecordingTimeout(config.pageRecordingTimeoutProperty());
		view.setRecordingAudioFormat(audioConfig.recordingFormatProperty());
		view.setRecordingPath(audioConfig.recordingPathProperty());
		view.setOnSelectRecordingPath(this::selectRecordingPath);
		view.setRecordingScreenCaptureFormat(screenCaptureConfig.recordingFormatProperty());
		view.setOnReset(this::reset);

		audioConfig.inputDeviceNameProperty().addListener((observable, oldDevice, newDevice) -> {
			loadAudioFormats(soundSystemName, newDevice);
		});
	}

	private void loadAudioFormats(String providerName, String deviceName) {
		if (!AudioUtils.hasAudioCaptureDevice(providerName, deviceName)) {
			// Select default device.
			AudioInputDevice[] devices = AudioUtils.getAudioCaptureDevices(providerName);

			deviceName = (devices.length > 0) ? devices[0].getName() : null;
		}
		if (isNull(deviceName)) {
			view.setRecordingAudioFormats(List.of());
			return;
		}

		AudioInputDevice captureDevice = AudioUtils.getAudioInputDevice(providerName, deviceName);

		if (nonNull(captureDevice)) {
			List<AudioFormat> formats = captureDevice.getSupportedFormats();

			view.setRecordingAudioFormats(formats);
		}
	}

	private void loadScreenCaptureFormats() {
		List<ScreenCaptureFormat> formats = new ArrayList<>();
		for (int frameRate : ScreenCaptureFormat.DEFAULT_FRAME_RATES) {
			formats.add(new ScreenCaptureFormat(frameRate));
		}
		view.setRecordingScreenCaptureFormats(formats);
	}

	private void selectRecordingPath() {
		File initDirectory = new File(audioConfig.getRecordingPath());

		DirectoryChooserView dirChooser = viewFactory.createDirectoryChooserView();
		dirChooser.setInitialDirectory(initDirectory);

		File selectedFile = dirChooser.show(view);

		if (nonNull(selectedFile)) {
			audioConfig.setRecordingPath(selectedFile.getAbsolutePath());
		}
	}

	private void reset() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.setNotifyToRecord(defaultConfig.getNotifyToRecord());
		config.setConfirmStopRecording(defaultConfig.getConfirmStopRecording());
		config.setPageRecordingTimeout(defaultConfig.getPageRecordingTimeout());

		audioConfig.setRecordingFormat(defaultConfig.getAudioConfig().getRecordingFormat());
		audioConfig.setRecordingPath(defaultConfig.getAudioConfig().getRecordingPath());

		screenCaptureConfig.setRecordingFormat(defaultConfig.getScreenCaptureConfig().getRecordingFormat());
	}
}