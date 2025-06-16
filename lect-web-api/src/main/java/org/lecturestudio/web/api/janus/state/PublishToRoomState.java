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

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;
import org.lecturestudio.web.api.janus.JanusParticipantContext;
import org.lecturestudio.web.api.janus.JanusPeerConnection;
import org.lecturestudio.web.api.janus.JanusStateHandler;
import org.lecturestudio.web.api.janus.message.JanusJsepMessage;
import org.lecturestudio.web.api.janus.message.JanusMediaMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusPluginMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublishMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublishRequest;
import org.lecturestudio.web.api.janus.message.JanusRoomStateMessage;
import org.lecturestudio.web.api.janus.message.JanusTrickleMessage;
import org.lecturestudio.web.api.stream.StreamAudioContext;
import org.lecturestudio.web.api.stream.StreamScreenContext;
import org.lecturestudio.web.api.stream.StreamVideoContext;
import org.lecturestudio.web.api.stream.StreamContext;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.video.VideoTrack;

/**
 * This state starts publishing media (audio, video and data) to a joined
 * video-room on the Janus WebRTC server. By default, send-only media streams
 * are established.
 *
 * @author Alex Andres
 */
public class PublishToRoomState implements JanusState {

	private JanusRoomPublishMessage publishRequest;

	private Consumer<LocalScreenVideoFrameEvent> localScreenFrameConsumer;

	private JanusParticipantContext participantContext;

	private Runnable screenSourceEndedCallback;


	@Override
	public void initialize(JanusStateHandler handler) throws Exception {
		StreamContext streamContext = handler.getStreamContext();
		StreamAudioContext audioContext = streamContext.getAudioContext();
		StreamVideoContext videoContext = streamContext.getVideoContext();
		StreamScreenContext screenContext = streamContext.getScreenContext();
		JanusPeerConnection peerConnection = handler.createPeerConnection();

		participantContext = handler.getParticipantContext();
		screenSourceEndedCallback = screenContext.getScreenSourceEndedCallback();
		localScreenFrameConsumer = screenContext.getLocalFrameConsumer();

		peerConnection.setOnLocalSessionDescription(description -> {
			sendRequest(handler, description.sdp);
		});
		peerConnection.setOnIceCandidate(iceCandidate -> {
			logDebug("ICE Candidate: " + iceCandidate);

			sendIceCandidate(handler, iceCandidate);
		});
		peerConnection.setOnIceGatheringState(state -> {
			logDebug("ICE Gathering State: " + state);

			if (state == RTCIceGatheringState.COMPLETE) {
				sendEndOfCandidates(handler);
			}
		});

		// Register for local video frames.
		setLocalVideoFrameConsumer(peerConnection, videoContext.getCaptureLocalVideo());

		videoContext.captureLocalVideoProperty().addListener((o, oldValue, newValue) -> {
			setLocalVideoFrameConsumer(peerConnection, newValue);
		});

		// Publishers are send-only.
		var audioDirection = RTCRtpTransceiverDirection.SEND_ONLY;
		var videoDirection = RTCRtpTransceiverDirection.SEND_ONLY;
		var screenDirection = RTCRtpTransceiverDirection.SEND_ONLY;

		try {
			peerConnection.setCameraCapability(videoContext.getCaptureCapability());
			peerConnection.setCameraDevice(videoContext.getCaptureDevice());
			peerConnection.getScreenShareConfig().setBitRate(screenContext.getBitrate());
			peerConnection.getScreenShareConfig().setFrameRate(screenContext.getFramerate());
			peerConnection.setup(audioDirection, videoDirection, screenDirection);

			// Initialize with desired mute and device settings.
			peerConnection.setMicrophoneEnabled(audioContext.getSendAudio());
			peerConnection.setCameraEnabled(videoContext.getSendVideo());
		}
		catch (Exception e) {
			logError(e, "Start peer connection failed");
		}
	}

	@Override
	public void handleMessage(JanusStateHandler handler, JanusMessage message) {
		JanusMessageType type = message.getEventType();

		if (type == JanusMessageType.WEBRTC_UP) {
			logDebug("Janus WebRTC connection is up (publisher)");
			return;
		}
		else if (type == JanusMessageType.MEDIA) {
			JanusMediaMessage mediaMessage = (JanusMediaMessage) message;

			logDebug("Janus %s receiving our %s",
					(mediaMessage.isReceiving() ? "started" : "stopped"),
					mediaMessage.getType());
			return;
		}
		else if (message instanceof JanusRoomStateMessage) {
			// Ignore.
			return;
		}

		try {
			checkTransaction(publishRequest, message);
		}
		catch (Exception e) {
			return;
		}

		if (message instanceof JanusJsepMessage jsepMessage) {
			String sdp = jsepMessage.getSdp();
			RTCSessionDescription answer = new RTCSessionDescription(RTCSdpType.ANSWER, sdp);

			handler.getPeerConnection().setSessionDescription(answer);
		}
	}

	private void sendRequest(JanusStateHandler handler, String sdp) {
		JanusRoomPublishRequest request = new JanusRoomPublishRequest();
		JanusPeerConnection peerConnection = handler.getPeerConnection();

		StreamContext streamContext = handler.getStreamContext();
		StreamVideoContext videoContext = streamContext.getVideoContext();

		peerConnection.setOnReplacedTrack(track -> {
			if (track.getId().equals("screen")) {
				addScreenTrackListeners(handler, (VideoTrack) track);
			}
		});

		for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
			MediaStreamTrack track = transceiver.getSender().getTrack();

			if (nonNull(track)) {
				request.addStreamDescription(transceiver.getMid(), track.getId());

				if (track.getId().equals("screen")) {
					addScreenTrackListeners(handler, (VideoTrack) track);
				}
				else if (track.getId().equals("camera")) {
					RTCRtpSender sender = transceiver.getSender();
					RTCRtpSendParameters sendParams = sender.getParameters();

					// Set camera encoding constraints.
					for (var encoding : sendParams.encodings) {
						encoding.minBitrate = videoContext.getBitrate() * 500;
						encoding.maxBitrate = videoContext.getBitrate() * 1000;
						encoding.maxFramerate = 20.0;
					}

					sender.setParameters(sendParams);
				}
			}
		}

		publishRequest = new JanusRoomPublishMessage(handler.getSessionId(), handler.getPluginId());
		publishRequest.setSdp(sdp);
		publishRequest.setTransaction(UUID.randomUUID().toString());
		publishRequest.setBody(request);

		handler.sendMessage(publishRequest);
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
		JanusPluginMessage message = new JanusPluginMessage(handler.getSessionId(), handler.getPluginId()) {

			// This field is mandatory.
			final Map<String, Object> candidate = Map.of("completed", true);

		};
		message.setEventType(JanusMessageType.TRICKLE);
		message.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(message);
	}

	private void addScreenTrackListeners(JanusStateHandler handler, VideoTrack track) {
		track.addTrackEndedListener(endedTrack -> {
			if (nonNull(screenSourceEndedCallback)) {
				screenSourceEndedCallback.run();
			}
		});

		track.addSink(videoFrame -> {
			if (nonNull(localScreenFrameConsumer)) {
				// NOTE: Avoid asynchronous access to the VideoFrame, otherwise the app will crash.
				//       For asynchronous access, the VideoFrame must be copied and released after processing.
				localScreenFrameConsumer.accept(new LocalScreenVideoFrameEvent(videoFrame,
						handler.getParticipantContext().getPeerId()));
			}

			// Release the VideoFrame to avoid memory leaks.
			videoFrame.release();
		});
	}

	private void setLocalVideoFrameConsumer(JanusPeerConnection peerConnection, boolean receiveLocalVideo) {
		if (receiveLocalVideo) {
			peerConnection.setOnLocalVideoFrame(videoFrame -> {
				if (peerConnection.getPeerConnectionState() != RTCPeerConnectionState.CONNECTED) {
					return;
				}

				participantContext.setVideoFrame(videoFrame);

				videoFrame.release();
			});
		}
		else {
			peerConnection.setOnLocalVideoFrame(null);
		}
	}
}
