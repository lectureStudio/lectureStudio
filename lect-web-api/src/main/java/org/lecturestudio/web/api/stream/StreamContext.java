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

package org.lecturestudio.web.api.stream;

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.RTCBundlePolicy;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCIceTransportPolicy;

import java.util.function.Consumer;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.model.UserInfo;
import org.lecturestudio.web.api.stream.model.Course;

public class StreamContext {

	private final StreamAudioContext audioContext;

	private final StreamVideoContext videoContext;

	private final StreamScreenContext screenContext;

	private final RTCConfiguration rtcConfig;

	private final BooleanProperty enableMessenger;

	private final BooleanProperty previewStream;

	private Consumer<PeerStateEvent> peerStateConsumer;

	private Course course;

	private UserInfo userInfo;


	public StreamContext() {
		audioContext = new StreamAudioContext();
		videoContext = new StreamVideoContext();
		screenContext = new StreamScreenContext();
		enableMessenger = new BooleanProperty();
		previewStream = new BooleanProperty();

		rtcConfig = new RTCConfiguration();
		rtcConfig.iceTransportPolicy = RTCIceTransportPolicy.ALL;
		rtcConfig.bundlePolicy = RTCBundlePolicy.MAX_BUNDLE;
	}

	public StreamAudioContext getAudioContext() {
		return audioContext;
	}

	public StreamVideoContext getVideoContext() {
		return videoContext;
	}

	public StreamScreenContext getScreenContext() {
		return screenContext;
	}

	public RTCConfiguration getRTCConfig() {
		return rtcConfig;
	}

	public boolean getMessengerEnabled() {
		return enableMessenger.get();
	}

	public void setMessengerEnabled(boolean enabled) {
		enableMessenger.set(enabled);
	}

	public BooleanProperty enableMessengerProperty() {
		return enableMessenger;
	}

	public boolean getPreviewStream() {
		return previewStream.get();
	}

	public void setPreviewStream(boolean enabled) {
		previewStream.set(enabled);
	}

	public BooleanProperty previewStreamProperty() {
		return previewStream;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public void setPeerStateConsumer(Consumer<PeerStateEvent> consumer) {
		peerStateConsumer = consumer;
	}

	public void setPeerStateEvent(PeerStateEvent event) {
		if (nonNull(peerStateConsumer)) {
			peerStateConsumer.accept(event);
		}
	}
}
