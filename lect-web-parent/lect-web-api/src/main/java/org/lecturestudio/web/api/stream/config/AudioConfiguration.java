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

package org.lecturestudio.web.api.stream.config;

import dev.onvoid.webrtc.media.audio.AudioDevice;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;

public class AudioConfiguration {

	private final ObjectProperty<AudioDevice> playoutDevice;

	private final ObjectProperty<AudioDevice> recordingDevice;

	private final BooleanProperty receiveAudio;

	private final BooleanProperty sendAudio;


	public AudioConfiguration() {
		playoutDevice = new ObjectProperty<>();
		recordingDevice = new ObjectProperty<>();
		receiveAudio = new BooleanProperty();
		sendAudio = new BooleanProperty();
	}

	public ObjectProperty<AudioDevice> playoutDeviceProperty() {
		return playoutDevice;
	}

	public AudioDevice getPlayoutDevice() {
		return playoutDevice.get();
	}

	public void setPlayoutDevice(AudioDevice device) {
		playoutDevice.set(device);
	}

	public ObjectProperty<AudioDevice> recordingDeviceProperty() {
		return recordingDevice;
	}

	public AudioDevice getRecordingDevice() {
		return recordingDevice.get();
	}

	public void setRecordingDevice(AudioDevice device) {
		recordingDevice.set(device);
	}

	public BooleanProperty receiveAudioProperty() {
		return receiveAudio;
	}

	public boolean getAudioVideo() {
		return receiveAudio.get();
	}

	public void setReceiveAudio(boolean receive) {
		receiveAudio.set(receive);
	}

	public BooleanProperty sendAudioProperty() {
		return sendAudio;
	}

	public boolean getSendAudio() {
		return sendAudio.get();
	}

	public void setSendAudio(boolean send) {
		sendAudio.set(send);
	}
}
