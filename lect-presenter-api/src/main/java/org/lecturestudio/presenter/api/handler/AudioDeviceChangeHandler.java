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

package org.lecturestudio.presenter.api.handler;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioDeviceChangeListener;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.service.RecordingService;

public class AudioDeviceChangeHandler extends PresenterHandler {

	private final AudioSystemProvider audioSystemProvider;

	private final RecordingService recordingService;


	public AudioDeviceChangeHandler(PresenterContext context,
			AudioSystemProvider audioSystemProvider,
			RecordingService recordingService) {
		super(context);

		this.audioSystemProvider = audioSystemProvider;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		audioSystemProvider.addDeviceChangeListener(new AudioDeviceChangeListener() {

			@Override
			public void deviceConnected(AudioDevice device) {
				showNotification(device, true);
			}

			@Override
			public void deviceDisconnected(AudioDevice device) {
				showNotification(device, false);
			}
		});
	}

	private void showNotification(AudioDevice device, boolean connected) {
		Dictionary dict = context.getDictionary();
		String devName = device.getName();
		String message = connected
				? "audio.device.connected"
				: "audio.device.disconnected";

		context.showNotificationPopup(dict.get(message), devName);
	}
}
