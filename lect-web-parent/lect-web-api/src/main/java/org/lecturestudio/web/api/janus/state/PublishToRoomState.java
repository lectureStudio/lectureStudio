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

package org.lecturestudio.web.api.janus.state;

import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceGatheringState;
import dev.onvoid.webrtc.RTCRtpTransceiverDirection;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

import org.lecturestudio.web.api.janus.JanusHandler;
import org.lecturestudio.web.api.janus.JanusPeerConnection;
import org.lecturestudio.web.api.janus.message.JanusJsepMessage;
import org.lecturestudio.web.api.janus.message.JanusMediaMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusPluginMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublishMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublishRequest;
import org.lecturestudio.web.api.janus.message.JanusTrickleMessage;

/**
 * This state starts publishing media (audio, video and data) to a joined
 * video-room on the Janus WebRTC server. By default send-only media streams are
 * established.
 *
 * @author Alex Andres
 */
public class PublishToRoomState implements JanusState {

	private JanusRoomPublishMessage publishRequest;


	@Override
	public void initialize(JanusHandler handler) {
		JanusPeerConnection peerConnection = handler.getPeerConnection();

		peerConnection.setOnLocalSessionDescription(description -> {
			sendRequest(handler, description.sdp);
		});
		peerConnection.setOnIceCandidate(iceCandidate -> {
			sendIceCandidate(handler, iceCandidate);
		});
		peerConnection.setOnIceGatheringState(state -> {
			if (state == RTCIceGatheringState.COMPLETE) {
				sendEndOfCandidates(handler);
			}
		});

		// Publishers are send-only.
		var mediaDirection = RTCRtpTransceiverDirection.SEND_ONLY;

		peerConnection.setupConnection(mediaDirection, mediaDirection);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		JanusMessageType type = message.getEventType();

		if (type == JanusMessageType.WEBRTC_UP) {
			logDebug("Janus WebRTC connection is up");
			return;
		}
		else if (type == JanusMessageType.MEDIA) {
			JanusMediaMessage mediaMessage = (JanusMediaMessage) message;

			logDebug("Janus %s receiving our %s",
					(mediaMessage.isReceiving() ? "started" : "stopped"),
					mediaMessage.getType());
			return;
		}

		checkTransaction(publishRequest, message);

		if (message instanceof JanusJsepMessage) {
			JanusJsepMessage jsepMessage = (JanusJsepMessage) message;
			String sdp = jsepMessage.getSdp();
			RTCSessionDescription answer = new RTCSessionDescription(RTCSdpType.ANSWER, sdp);

			handler.getPeerConnection().setSessionDescription(answer);
			handler.getPeerConnection().setOnDataChannelBuffer(this::onDataChannelBuffer);
		}
	}

	private void sendRequest(JanusHandler handler, String sdp) {
		JanusRoomPublishRequest request = new JanusRoomPublishRequest();
		request.setAudio(true);
		request.setVideo(true);
		request.setData(true);

		publishRequest = new JanusRoomPublishMessage(handler.getSessionId(),
				handler.getPluginId());
		publishRequest.setSdp(sdp);
		publishRequest.setTransaction(UUID.randomUUID().toString());
		publishRequest.setBody(request);

		handler.sendMessage(publishRequest);
	}

	private void sendIceCandidate(JanusHandler handler, RTCIceCandidate candidate) {
		JanusTrickleMessage message = new JanusTrickleMessage(handler.getSessionId(),
				handler.getPluginId());
		message.setTransaction(UUID.randomUUID().toString());
		message.setSdp(candidate.sdp);
		message.setSdpMid(candidate.sdpMid);
		message.setSdpMLineIndex(candidate.sdpMLineIndex);

		handler.sendMessage(message);
	}

	private void sendEndOfCandidates(JanusHandler handler) {
		JanusPluginMessage message = new JanusPluginMessage(handler.getSessionId(),
				handler.getPluginId()) {

			final Map<String, Object> candidate = Map.of("completed", true);

		};
		message.setEventType(JanusMessageType.TRICKLE);
		message.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(message);
	}

	private void onDataChannelBuffer(RTCDataChannelBuffer buffer) {
		ByteBuffer byteBuffer = buffer.data;
		byte[] payload;

		if (byteBuffer.hasArray()) {
			payload = byteBuffer.array();
		}
		else {
			payload = new byte[byteBuffer.limit()];

			byteBuffer.get(payload);
		}

		System.out.println(new String(payload));
	}
}
