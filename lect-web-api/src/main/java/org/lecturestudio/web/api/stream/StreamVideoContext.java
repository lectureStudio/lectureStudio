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

import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;

public class StreamVideoContext {

	private final ObjectProperty<VideoDevice> captureDevice;

	private final ObjectProperty<VideoCaptureCapability> captureCapability;

	private final IntegerProperty bitrate;

	private final BooleanProperty receiveVideo;

	private final BooleanProperty sendVideo;

	private final BooleanProperty captureLocalVideo;


	public StreamVideoContext() {
		captureDevice = new ObjectProperty<>();
		captureCapability = new ObjectProperty<>();
		bitrate = new IntegerProperty();
		receiveVideo = new BooleanProperty();
		sendVideo = new BooleanProperty();
		captureLocalVideo = new BooleanProperty();
	}

	public ObjectProperty<VideoDevice> captureDeviceProperty() {
		return captureDevice;
	}

	public VideoDevice getCaptureDevice() {
		return captureDevice.get();
	}

	public void setCaptureDevice(VideoDevice device) {
		captureDevice.set(device);
	}

	public ObjectProperty<VideoCaptureCapability> captureCapabilityProperty() {
		return captureCapability;
	}

	public VideoCaptureCapability getCaptureCapability() {
		return captureCapability.get();
	}

	public void setCaptureCapability(VideoCaptureCapability capability) {
		captureCapability.set(capability);
	}

	public IntegerProperty bitrateProperty() {
		return bitrate;
	}

	public int getBitrate() {
		return bitrate.get();
	}

	public void setBitrate(int rate) {
		bitrate.set(rate);
	}

	public BooleanProperty receiveVideoProperty() {
		return receiveVideo;
	}

	public boolean getReceiveVideo() {
		return receiveVideo.get();
	}

	public void setReceiveVideo(boolean receive) {
		receiveVideo.set(receive);
	}

	public BooleanProperty sendVideoProperty() {
		return sendVideo;
	}

	public boolean getSendVideo() {
		return sendVideo.get();
	}

	public void setSendVideo(boolean send) {
		sendVideo.set(send);
	}

	public BooleanProperty captureLocalVideoProperty() {
		return captureLocalVideo;
	}

	public boolean getCaptureLocalVideo() {
		return captureLocalVideo.get();
	}

	public void setCaptureLocalVideo(boolean send) {
		captureLocalVideo.set(send);
	}

}
