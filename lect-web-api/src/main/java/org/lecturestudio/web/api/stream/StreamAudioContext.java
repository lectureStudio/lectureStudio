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

package org.lecturestudio.web.api.stream;

import dev.onvoid.webrtc.media.audio.AudioDevice;

import java.util.function.Consumer;

import org.lecturestudio.core.audio.AudioFrame;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;

public class StreamAudioContext {

	private final ObjectProperty<AudioDevice> playbackDevice = new ObjectProperty<>();

	private final ObjectProperty<AudioDevice> recordingDevice = new ObjectProperty<>();

	private final BooleanProperty receiveAudio = new BooleanProperty();

	private final BooleanProperty sendAudio = new BooleanProperty();

	private final DoubleProperty playbackVolume = new DoubleProperty();

	private Consumer<AudioFrame> remoteFrameConsumer;


	public StreamAudioContext() {

	}

	public ObjectProperty<AudioDevice> playbackDeviceProperty() {
		return playbackDevice;
	}

	public AudioDevice getPlaybackDevice() {
		return playbackDevice.get();
	}

	public void setPlaybackDevice(AudioDevice device) {
		playbackDevice.set(device);
	}

	public double getPlaybackVolume() {
		return playbackVolume.get();
	}

	public void setPlaybackVolume(double volume) {
		this.playbackVolume.set(volume);
	}

	public DoubleProperty playbackVolumeProperty() {
		return playbackVolume;
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

	public boolean getReceiveAudio() {
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

	public Consumer<AudioFrame> getRemoteFrameConsumer() {
		return remoteFrameConsumer;
	}

	public void setRemoteFrameConsumer(Consumer<AudioFrame> consumer) {
		this.remoteFrameConsumer = consumer;
	}
}
