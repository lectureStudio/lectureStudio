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

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceConnectionState;
import dev.onvoid.webrtc.RTCIceGatheringState;
import dev.onvoid.webrtc.RTCSessionDescription;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.event.VideoFrameEvent;
import org.lecturestudio.web.api.janus.JanusPeerConnection;
import org.lecturestudio.web.api.janus.JanusStateHandler;
import org.lecturestudio.web.api.janus.message.JanusMediaMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusPluginMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherUnpublishedMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomStateMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomSubscribeMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomSubscribeRequest;
import org.lecturestudio.web.api.janus.message.JanusTrickleMessage;
import org.lecturestudio.web.api.stream.config.WebRtcConfiguration;

/**
 * This state starts publishing media (audio, video and data) to a joined
 * video-room on the Janus WebRTC server. By default, send-only media streams
 * are established.
 *
 * @author Alex Andres
 */
public class SubscriberJoinedRoomState implements JanusState {

	private final RTCSessionDescription offer;

	private final BigInteger peerId;

	private final String userName;

	private Consumer<PeerStateEvent> peerStateConsumer;

	private Consumer<VideoFrameEvent> videoFrameConsumer;

	private JanusRoomSubscribeMessage subscribeRequest;


	public SubscriberJoinedRoomState(RTCSessionDescription offer,
			BigInteger peerId, String userName) {
		this.offer = offer;
		this.peerId = peerId;
		this.userName = userName;
	}

	@Override
	public void initialize(JanusStateHandler handler) {
		handler.createPeerConnection();

		WebRtcConfiguration webRtcConfig = handler.getWebRtcConfig();
		JanusPeerConnection peerConnection = handler.getPeerConnection();

		webRtcConfig.getAudioConfiguration().receiveAudioProperty()
				.addListener((observable, oldValue, newValue) -> {
					peerConnection.enableRemoteAudio(newValue);
				});
		webRtcConfig.getVideoConfiguration().receiveVideoProperty()
				.addListener((observable, oldValue, newValue) -> {
					peerConnection.enableRemoteVideo(newValue);
				});

		peerStateConsumer = webRtcConfig.getPeerStateConsumer();
		videoFrameConsumer = webRtcConfig.getVideoFrameConsumer();

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
		peerConnection.setOnIceConnectionState(state -> {
			if (state == RTCIceConnectionState.CONNECTED) {
				setPeerState(ExecutableState.Started);
			}
			else if (state == RTCIceConnectionState.DISCONNECTED) {
				setPeerState(ExecutableState.Stopped);
			}
		});
		peerConnection.setOnRemoteVideoFrame(videoFrame -> {
			if (nonNull(videoFrameConsumer)) {
				videoFrameConsumer.accept(new VideoFrameEvent(videoFrame, peerId.toString()));
			}
		});

		peerConnection.setSessionDescription(offer);
	}

	@Override
	public void handleMessage(JanusStateHandler handler, JanusMessage message) {
		JanusMessageType type = message.getEventType();

		if (type == JanusMessageType.WEBRTC_UP) {
			logDebug("Janus WebRTC connection is up (subscriber)");

			//setPeerState(ExecutableState.Started);
			return;
		}
		if (type == JanusMessageType.HANGUP) {
			logDebug("Janus WebRTC connection hangup (subscriber)");

			//setPeerState(ExecutableState.Stopped);
			return;
		}
		else if (type == JanusMessageType.MEDIA) {
			JanusMediaMessage mediaMessage = (JanusMediaMessage) message;

			logDebug("Janus %s receiving our %s",
					(mediaMessage.isReceiving() ? "started" : "stopped"),
					mediaMessage.getType());
			return;
		}
		else if (message instanceof JanusRoomPublisherUnpublishedMessage) {
			JanusRoomPublisherUnpublishedMessage unpubMessage = (JanusRoomPublisherUnpublishedMessage) message;

			if (unpubMessage.getPublisherId().equals(peerId)) {
//				setPeerState(ExecutableState.Stopped);
			}
		}
		else if (message instanceof JanusRoomStateMessage) {
			// Ignore.
			return;
		}

		try {
			checkTransaction(subscribeRequest, message);
		}
		catch (Exception e) {
			return;
		}
	}

	private void sendRequest(JanusStateHandler handler, String sdp) {
		JanusRoomSubscribeRequest request = new JanusRoomSubscribeRequest();
		request.setRoomId(handler.getRoomId());

		subscribeRequest = new JanusRoomSubscribeMessage(handler.getSessionId(),
				handler.getPluginId());
		subscribeRequest.setTransaction(UUID.randomUUID().toString());
		subscribeRequest.setSdp(sdp);
		subscribeRequest.setBody(request);

		handler.sendMessage(subscribeRequest);
	}

	private void sendIceCandidate(JanusStateHandler handler, RTCIceCandidate candidate) {
		JanusTrickleMessage message = new JanusTrickleMessage(handler.getSessionId(),
				handler.getPluginId());
		message.setTransaction(UUID.randomUUID().toString());
		message.setSdp(candidate.sdp);
		message.setSdpMid(candidate.sdpMid);
		message.setSdpMLineIndex(candidate.sdpMLineIndex);

		handler.sendMessage(message);
	}

	private void sendEndOfCandidates(JanusStateHandler handler) {
		JanusPluginMessage message = new JanusPluginMessage(handler.getSessionId(),
				handler.getPluginId()) {

			final Map<String, Object> candidate = Map.of("completed", true);

		};
		message.setEventType(JanusMessageType.TRICKLE);
		message.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(message);
	}

	private void setPeerState(ExecutableState state) {
		if (nonNull(peerStateConsumer)) {
			peerStateConsumer.accept(new PeerStateEvent(userName, state));
		}
	}
}
