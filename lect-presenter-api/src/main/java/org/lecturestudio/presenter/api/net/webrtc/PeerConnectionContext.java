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

package org.lecturestudio.presenter.api.net.webrtc;

import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpTransceiverDirection;
import dev.onvoid.webrtc.RTCStatsReport;
import dev.onvoid.webrtc.media.video.VideoFrame;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lecturestudio.presenter.api.model.ChatMessage;
import org.lecturestudio.presenter.api.model.Contact;

public class PeerConnectionContext {

	public RTCRtpTransceiverDirection audioDirection;

	public RTCRtpTransceiverDirection videoDirection;

	public Consumer<VideoFrame> onLocalFrame;

	public Consumer<Boolean> onLocalVideoStream;

	public Consumer<RTCStatsReport> onStatsReport;

	public BiConsumer<Contact, ChatMessage> onMessage;

	public BiConsumer<Contact, VideoFrame> onRemoteFrame;

	public BiConsumer<Contact, Boolean> onRemoteVideoStream;

	public BiConsumer<Contact, RTCPeerConnectionState> onPeerConnectionState;

}
