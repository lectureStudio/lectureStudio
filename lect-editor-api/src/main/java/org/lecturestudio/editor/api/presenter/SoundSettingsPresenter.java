/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.editor.api.config.DefaultConfiguration;
import org.lecturestudio.editor.api.view.SoundSettingsView;

public class SoundSettingsPresenter extends Presenter<SoundSettingsView> {

	private final AudioConfiguration audioConfig;

	private final AudioSystemProvider audioSystemProvider;


	@Inject
	SoundSettingsPresenter(ApplicationContext context, SoundSettingsView view,
			AudioSystemProvider audioSystemProvider) {
		super(context, view);

		this.audioConfig = context.getConfiguration().getAudioConfig();
		this.audioSystemProvider = audioSystemProvider;
	}

	@Override
	public void initialize() throws Exception {
		if (isNull(audioConfig.getPlaybackDeviceName())) {
			setDefaultPlaybackDevice();
		}

		var devices = Arrays.stream(audioSystemProvider.getPlaybackDevices())
				.map(AudioDevice::getName).collect(Collectors.toList());

		view.setAudioPlaybackDevices(devices);
		view.setAudioPlaybackDevice(audioConfig.playbackDeviceNameProperty());

		view.setOnClose(this::close);
		view.setOnReset(this::reset);
	}

	private void reset() {
		Configuration config = context.getConfiguration();
		DefaultConfiguration defaultConfig = new DefaultConfiguration();

		config.getAudioConfig().setPlaybackDeviceName(
				defaultConfig.getAudioConfig().getPlaybackDeviceName());
	}

	private void setDefaultPlaybackDevice() {
		AudioDevice playbackDevice = audioSystemProvider.getDefaultPlaybackDevice();

		// Select first available playback device.
		if (nonNull(playbackDevice)) {
			audioConfig.setPlaybackDeviceName(playbackDevice.getName());
		}
	}
}