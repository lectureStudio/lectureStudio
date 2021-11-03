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

package org.lecturestudio.web.api.janus;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCAnswerOptions;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelInit;
import dev.onvoid.webrtc.RTCDataChannelObserver;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceConnectionState;
import dev.onvoid.webrtc.RTCIceGatheringState;
import dev.onvoid.webrtc.RTCOfferOptions;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpReceiver;
import dev.onvoid.webrtc.RTCRtpSender;
import dev.onvoid.webrtc.RTCRtpTransceiver;
import dev.onvoid.webrtc.RTCRtpTransceiverDirection;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioOptions;
import dev.onvoid.webrtc.media.audio.AudioSource;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDesktopSource;
import dev.onvoid.webrtc.media.video.VideoDevice;
import dev.onvoid.webrtc.media.video.VideoDeviceSource;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.web.api.stream.config.VideoConfiguration;
import org.lecturestudio.web.api.stream.config.WebRtcConfiguration;

public class JanusPeerConnection implements PeerConnectionObserver {

	private final static Logger LOGGER = System.getLogger(JanusPeerConnection.class.getName());

	private final JanusPeerConnectionFactory factory;

	private final ExecutorService executor;

	private final WebRtcConfiguration config;

	private final RTCPeerConnection peerConnection;

	private RTCDataChannel dataChannel;

	private RTCDataChannel remoteDataChannel;

	/*
	 * Queued remote ICE candidates are consumed only after both local and
	 * remote descriptions are set. Similarly local ICE candidates are sent to
	 * remote peer after both local and remote description are set.
	 */
	private List<RTCIceCandidate> queuedRemoteCandidates;

	private VideoDesktopSource desktopSource;

	private VideoDeviceSource videoSource;

	private Consumer<JanusPeerConnectionException> onException;

	private Consumer<RTCSessionDescription> onLocalSessionDescription;

	private Consumer<RTCIceCandidate> onIceCandidate;

	private Consumer<RTCPeerConnectionState> onPeerConnectionState;

	private Consumer<RTCIceConnectionState> onIceConnectionState;

	private Consumer<RTCIceGatheringState> onIceGatheringState;

	private Consumer<RTCDataChannelBuffer> onDataChannelBuffer;

	private Consumer<Boolean> onRemoteVideoStream;

	private Consumer<VideoFrame> onRemoteVideoFrame;


	public JanusPeerConnection(JanusPeerConnectionFactory factory) {
		this.factory = factory;
		this.config = factory.getConfig();
		this.executor = factory.getExecutor();
		this.queuedRemoteCandidates = new ArrayList<>();
		this.peerConnection = factory.createPeerConnection(this);
	}

	public void setOnException(Consumer<JanusPeerConnectionException> callback) {
		onException = callback;
	}

	public void setOnLocalSessionDescription(Consumer<RTCSessionDescription> callback) {
		onLocalSessionDescription = callback;
	}

	public void setOnIceCandidate(Consumer<RTCIceCandidate> callback) {
		onIceCandidate = callback;
	}

	public void setOnIceGatheringState(Consumer<RTCIceGatheringState> callback) {
		onIceGatheringState = callback;
	}

	public void setOnPeerConnectionState(Consumer<RTCPeerConnectionState> callback) {
		onPeerConnectionState = callback;
	}

	public void setOnIceConnectionState(Consumer<RTCIceConnectionState> callback) {
		onIceConnectionState = callback;
	}

	public void setOnDataChannelBuffer(Consumer<RTCDataChannelBuffer> callback) {
		onDataChannelBuffer = callback;
	}

	public void setOnRemoteVideoStream(Consumer<Boolean> callback) {
		onRemoteVideoStream = callback;
	}

	public void setOnRemoteVideoFrame(Consumer<VideoFrame> callback) {
		onRemoteVideoFrame = callback;
	}

	public boolean hasLocalVideoStream() {
		return nonNull(videoSource);
	}

	public boolean hasRemoteVideoStream() {
		RTCRtpReceiver[] receivers = peerConnection.getReceivers();

		if (nonNull(receivers)) {
			for (RTCRtpReceiver receiver : receivers) {
				if (receiver.getTrack().getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
					return true;
				}
			}
		}

		return false;
	}

	public void sendData(byte[] data) throws Exception {
		if (isNull(dataChannel)) {
			throw new IllegalStateException("Data channel has not been initialized");
		}

		ByteBuffer dataBuffer = ByteBuffer.wrap(data);

		dataChannel.send(new RTCDataChannelBuffer(dataBuffer, true));
	}

	@Override
	public void onRenegotiationNeeded() {
		if (nonNull(peerConnection.getRemoteDescription())) {
			LOGGER.log(Level.INFO, "Renegotiation needed");

			createOffer();
		}
	}

	@Override
	public void onConnectionChange(RTCPeerConnectionState state) {
		notify(onPeerConnectionState, state);
	}

	@Override
	public void onIceConnectionChange(RTCIceConnectionState state) {
		LOGGER.log(Level.INFO, "ICE connection state: " + state);

		notify(onIceConnectionState, state);
	}

	@Override
	public void onIceGatheringChange(RTCIceGatheringState state) {
		notify(onIceGatheringState, state);
	}

	@Override
	public void onDataChannel(RTCDataChannel channel) {
		remoteDataChannel = channel;

		initDataChannel(channel);
	}

	@Override
	public void onIceCandidate(RTCIceCandidate candidate) {
		if (isNull(peerConnection)) {
			LOGGER.log(Level.ERROR, "PeerConnection was not initialized");
			return;
		}

		try {
			notify(onIceCandidate, candidate);
		}
		catch (Exception e) {
			LOGGER.log(Level.ERROR, "Send RTCIceCandidate failed", e);
		}
	}

	@Override
	public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {
		if (isNull(peerConnection)) {
			LOGGER.log(Level.ERROR, "PeerConnection was not initialized");
			return;
		}

		// TODO
		try {
//			signalingClient.send(contact, candidates);
		}
		catch (Exception e) {
			LOGGER.log(Level.ERROR, "Send removed RTCIceCandidates failed", e);
		}
	}

	@Override
	public void onTrack(RTCRtpTransceiver transceiver) {
		MediaStreamTrack track = transceiver.getReceiver().getTrack();

		if (track.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
			VideoTrack videoTrack = (VideoTrack) track;
			videoTrack.addSink(frame -> publishFrame(onRemoteVideoFrame, frame));

			notify(onRemoteVideoStream, true);
		}
	}

	@Override
	public void onRemoveTrack(RTCRtpReceiver receiver) {
		MediaStreamTrack track = receiver.getTrack();

		if (track.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
			notify(onRemoteVideoStream, false);
		}
	}

	public CompletableFuture<Void> close() {
		return CompletableFuture.runAsync(() -> {
			if (nonNull(desktopSource)) {
				desktopSource.stop();
				desktopSource.dispose();
				desktopSource = null;
			}
			if (nonNull(videoSource)) {
				videoSource.stop();
				videoSource.dispose();
				videoSource = null;
			}
			if (nonNull(dataChannel)) {
				dataChannel.unregisterObserver();
				dataChannel.close();
				dataChannel.dispose();
				dataChannel = null;
			}
			if (nonNull(remoteDataChannel)) {
				remoteDataChannel.unregisterObserver();
				remoteDataChannel.close();
				remoteDataChannel.dispose();
				remoteDataChannel = null;
			}
			if (nonNull(peerConnection)) {
				peerConnection.close();
			}
		}, executor);
	}

	public void setupConnection(RTCRtpTransceiverDirection audio,
			RTCRtpTransceiverDirection video) {
		execute(() -> {
			addAudio(audio);
			addVideo(video);
			addDataChannel();

			createOffer();
		});
	}

	public void enableMicrophone(boolean enable) {
		execute(() -> {
			enableSenderTrack(MediaStreamTrack.AUDIO_TRACK_KIND, enable);
		});
	}

	public void enableCamera(boolean enable) {
		execute(() -> {
			RTCRtpTransceiverDirection camDirection;

			if (enable) {
				if (nonNull(videoSource)) {
					videoSource.start();

					camDirection = RTCRtpTransceiverDirection.SEND_ONLY;
				}
				else {
					addVideo(RTCRtpTransceiverDirection.SEND_ONLY);
					return;
				}
			}
			else {
				if (nonNull(videoSource)) {
					videoSource.stop();
				}

				camDirection = RTCRtpTransceiverDirection.INACTIVE;
			}

			for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
				MediaStreamTrack track = transceiver.getSender().getTrack();

				if (nonNull(track) && track.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
					transceiver.setDirection(camDirection);
					break;
				}
			}
		});
	}

	public void enableRemoteAudio(boolean enable) {
		execute(() -> {
			enableReceiverTrack(MediaStreamTrack.AUDIO_TRACK_KIND, enable);
		});
	}

	public void enableRemoteVideo(boolean enable) {
		execute(() -> {
			enableReceiverTrack(MediaStreamTrack.VIDEO_TRACK_KIND, enable);
		});
	}

	public String getAudioMid() {
		return getMid(MediaStreamTrack.AUDIO_TRACK_KIND);
	}

	public String getVideoMid() {
		return getMid(MediaStreamTrack.VIDEO_TRACK_KIND);
	}

	private String getMid(String kind) {
		String mid = null;

		for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
			MediaStreamTrack track = transceiver.getSender().getTrack();

			if (nonNull(track) && track.getKind().equals(kind)) {
				mid = transceiver.getMid();
				break;
			}
		}

		return mid;
	}

	private void addAudio(RTCRtpTransceiverDirection direction) {
		if (!sendMedia(direction)) {
			return;
		}

		AudioOptions audioOptions = new AudioOptions();
		audioOptions.echoCancellation = true;
		audioOptions.noiseSuppression = true;
		audioOptions.highpassFilter = true;
		audioOptions.typingDetection = true;
		audioOptions.residualEchoDetector = true;

		AudioSource audioSource = factory.getFactory().createAudioSource(audioOptions);
		AudioTrack audioTrack = factory.getFactory().createAudioTrack("audioTrack", audioSource);

		peerConnection.addTrack(audioTrack, List.of("stream"));

		for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
			MediaStreamTrack track = transceiver.getSender().getTrack();

			if (nonNull(track) && track.getKind().equals(MediaStreamTrack.AUDIO_TRACK_KIND)) {
				transceiver.setDirection(direction);

				// Initialize with desired mute setting.
				track.setEnabled(config.getAudioConfiguration().getSendAudio());
				break;
			}
		}
	}

	private void addVideo(RTCRtpTransceiverDirection direction) {
		if (!sendMedia(direction)) {
			return;
		}

		VideoConfiguration videoConfig = config.getVideoConfiguration();
		VideoDevice device = videoConfig.getCaptureDevice();
		VideoCaptureCapability capability = videoConfig.getCaptureCapability();

		videoSource = new VideoDeviceSource();

		if (nonNull(device)) {
			LOGGER.log(Level.INFO, "Video capture device: " + device);

			videoSource.setVideoCaptureDevice(device);
		}
		if (nonNull(capability)) {
			LOGGER.log(Level.INFO, "Video capture capability: " + capability);

			videoSource.setVideoCaptureCapability(capability);
		}

		VideoTrack videoTrack = factory.getFactory().createVideoTrack("videoTrack", videoSource);
		if (direction == RTCRtpTransceiverDirection.SEND_ONLY ||
			direction == RTCRtpTransceiverDirection.SEND_RECV) {
			try {
				videoSource.start();
			}
			catch (Throwable e) {
				videoSource.dispose();
				videoSource = null;

				notify(onException, new JanusPeerConnectionMediaException(
						MediaType.Camera, "Start video capture source failed", e));
				return;
			}
		}

		peerConnection.addTrack(videoTrack, List.of("stream"));

		for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
			MediaStreamTrack track = transceiver.getSender().getTrack();

			if (nonNull(track) && track.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
				transceiver.setDirection(direction);
				break;
			}
		}
	}

	private void addDataChannel() {
		RTCDataChannelInit dict = new RTCDataChannelInit();
		dict.protocol = "stream-messaging";

		dataChannel = peerConnection.createDataChannel("data", dict);
	}

	private void initDataChannel(final RTCDataChannel channel) {
		channel.registerObserver(new RTCDataChannelObserver() {

			@Override
			public void onBufferedAmountChange(long previousAmount) {
				execute(() -> {
					LOGGER.log(Level.INFO, "RTCDataChannel \"{0}\" buffered amount changed to {1}",
							channel.getLabel(),
							previousAmount);
				});
			}

			@Override
			public void onStateChange() {
				execute(() -> {
					LOGGER.log(Level.INFO, "RTCDataChannel \"{0}\" state: {1}",
							channel.getLabel(),
							channel.getState());
				});
			}

			@Override
			public void onMessage(RTCDataChannelBuffer buffer) {
				execute(() -> {
					if (nonNull(onDataChannelBuffer)) {
						onDataChannelBuffer.accept(buffer);
					}
				});
			}
		});
	}

	private void enableReceiverTrack(String type, boolean enable) {
		for (RTCRtpReceiver receiver : peerConnection.getReceivers()) {
			MediaStreamTrack track = receiver.getTrack();

			if (nonNull(track) && track.getKind().equals(type)) {
				track.setEnabled(enable);

				LOGGER.log(Level.INFO, "Receiver track \"{0}\" set enabled to \"{1}\"",
						track.getId(), enable);
				break;
			}
		}
	}

	private void enableSenderTrack(String type, boolean enable) {
		for (RTCRtpSender sender : peerConnection.getSenders()) {
			MediaStreamTrack track = sender.getTrack();

			if (nonNull(track) && track.getKind().equals(type)) {
				track.setEnabled(enable);

				LOGGER.log(Level.INFO, "Sender track \"{0}\" set enabled to \"{1}\"",
						track.getId(), enable);
				break;
			}
		}
	}

	public void setSessionDescription(RTCSessionDescription description) {
		execute(() -> {
			boolean receivingCall = description.sdpType == RTCSdpType.OFFER;

			peerConnection.setRemoteDescription(description, new SetSDObserver() {

				@Override
				public void onSuccess() {
					super.onSuccess();

					if (receivingCall) {
						addAudio(RTCRtpTransceiverDirection.RECV_ONLY);
						addVideo(RTCRtpTransceiverDirection.RECV_ONLY);

						createAnswer();
					}
				}

			});
		});
	}

	public void addIceCandidate(RTCIceCandidate candidate) {
		execute(() -> {
			if (nonNull(queuedRemoteCandidates)) {
				queuedRemoteCandidates.add(candidate);
			}
			else {
				peerConnection.addIceCandidate(candidate);
			}
		});
	}

	public void removeIceCandidates(RTCIceCandidate[] candidates) {
		execute(() -> {
			drainIceCandidates();

			peerConnection.removeIceCandidates(candidates);
		});
	}

	private void drainIceCandidates() {
		if (nonNull(queuedRemoteCandidates)) {
			LOGGER.log(Level.INFO, "Add " + queuedRemoteCandidates.size() + " remote candidates");

			queuedRemoteCandidates.forEach(peerConnection::addIceCandidate);
			queuedRemoteCandidates = null;
		}
	}

	private void createOffer() {
		RTCOfferOptions options = new RTCOfferOptions();

		peerConnection.createOffer(options, new CreateSDObserver());
	}

	private void createAnswer() {
		RTCAnswerOptions options = new RTCAnswerOptions();

		peerConnection.createAnswer(options, new CreateSDObserver());
	}

	private void setLocalDescription(RTCSessionDescription description) {
		peerConnection.setLocalDescription(description, new SetSDObserver() {

			@Override
			public void onSuccess() {
				super.onSuccess();

				try {
					JanusPeerConnection.this.notify(onLocalSessionDescription, description);
				}
				catch (Exception e) {
					LOGGER.log(Level.ERROR, "Send RTCSessionDescription failed", e);
				}
			}

		});
	}

	private void publishFrame(Consumer<VideoFrame> consumer, VideoFrame frame) {
		if (isNull(consumer)) {
			return;
		}

		frame.retain();
		notify(consumer, frame);
		frame.release();
	}

//	private void publishFrame(BiConsumer<Contact, VideoFrame> consumer, VideoFrame frame) {
//		if (isNull(consumer)) {
//			return;
//		}
//
//		frame.retain();
//		consumer.accept(contact, frame);
//		frame.release();
//	}

	private <T> void notify(Consumer<T> consumer, T value) {
		if (nonNull(consumer)) {
			execute(() -> consumer.accept(value));
		}
	}

//	private <T> void notify(BiConsumer<Contact, T> consumer, T value) {
//		if (nonNull(consumer)) {
//			execute(() -> consumer.accept(contact, value));
//		}
//	}

	private void execute(Runnable runnable) {
		executor.execute(runnable);
	}

	private void executeAndWait(Runnable runnable) {
		try {
			executor.submit(runnable).get();
		}
		catch (Exception e) {
			LOGGER.log(Level.ERROR, "Execute task failed", e);
		}
	}

	private static boolean sendMedia(RTCRtpTransceiverDirection direction) {
		return direction == RTCRtpTransceiverDirection.SEND_RECV
				|| direction == RTCRtpTransceiverDirection.SEND_ONLY;
	}



	private class CreateSDObserver implements CreateSessionDescriptionObserver {

		@Override
		public void onSuccess(RTCSessionDescription description) {
			execute(() -> setLocalDescription(description));
		}

		@Override
		public void onFailure(String error) {
			execute(() -> {
				LOGGER.log(Level.ERROR, "Create RTCSessionDescription failed: " + error);
			});
		}

	}



	private class SetSDObserver implements SetSessionDescriptionObserver {

		@Override
		public void onSuccess() {
			execute(() -> {
				if (nonNull(peerConnection.getLocalDescription()) &&
					nonNull(peerConnection.getRemoteDescription())) {
					drainIceCandidates();
				}
			});
		}

		@Override
		public void onFailure(String error) {
			execute(() -> {
				LOGGER.log(Level.ERROR, "Set RTCSessionDescription failed: " + error);
			});
		}
	}
}
