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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.net.MediaType;

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
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioOptions;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSource;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDesktopSource;
import dev.onvoid.webrtc.media.video.VideoDevice;
import dev.onvoid.webrtc.media.video.VideoDeviceSource;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;

public class JanusPeerConnection implements PeerConnectionObserver {

	private static final Logger LOGGER = LogManager.getLogger(JanusPeerConnection.class);

	private final JanusPeerConnectionFactory factory;

	private final ExecutorService executor;

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

	private VideoDeviceSource cameraSource;

	private VideoDevice cameraDevice;

	private VideoCaptureCapability cameraCapability;

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
			LOGGER.debug("Renegotiation needed");

			createOffer();
		}
	}

	@Override
	public void onConnectionChange(RTCPeerConnectionState state) {
		notify(onPeerConnectionState, state);
	}

	@Override
	public void onIceConnectionChange(RTCIceConnectionState state) {
		LOGGER.debug("ICE connection state: " + state);

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
			LOGGER.error("PeerConnection was not initialized");
			return;
		}

		try {
			notify(onIceCandidate, candidate);
		}
		catch (Exception e) {
			LOGGER.error("Send RTCIceCandidate failed", e);
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

	public void close() {
		execute(() -> {
			disposeCameraSource();
			disposeDataChannel();
			disposeDesktopSource();
			disposePeerConnection();
		});
	}

	public void setup(RTCRtpTransceiverDirection audio,
			RTCRtpTransceiverDirection video) {
		execute(() -> {
			addAudio(audio);
			addVideo(video);
			addDataChannel();

			createOffer();
		});
	}

	public boolean isReceivingVideo() {
		for (RTCRtpReceiver receiver : peerConnection.getReceivers()) {
			MediaStreamTrack track = receiver.getTrack();

			if (nonNull(track) && track.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
				if (track.isEnabled()) {
					return true;
				}
			}
		}

		return false;
	}

	public void setMicrophoneEnabled(boolean enable) {
		execute(() -> {
			setSenderTrackEnabled(MediaStreamTrack.AUDIO_TRACK_KIND, enable);
		});
	}

	public void setCameraEnabled(boolean enable) {
		execute(() -> {
			RTCRtpTransceiverDirection direction = enable ?
					RTCRtpTransceiverDirection.SEND_ONLY :
					RTCRtpTransceiverDirection.INACTIVE;

			if (enable) {
				if (nonNull(cameraSource)) {
					try {
						cameraSource.start();
					}
					catch (Throwable e) {
						notify(onException, new JanusPeerConnectionMediaException(
								MediaType.Camera, "Start video capture source failed", e));
						return;
					}
				}
				else {
					addVideo(direction);
					return;
				}
			}
			else {
				if (nonNull(cameraSource)) {
					cameraSource.stop();
				}
			}

			setTransceiverDirection(direction, MediaStreamTrack.VIDEO_TRACK_KIND);
			setSenderTrackEnabled(MediaStreamTrack.VIDEO_TRACK_KIND, enable);
		});
	}

	public void setCameraDevice(VideoDevice device) {
		Objects.requireNonNull(device);

		if (Objects.equals(cameraDevice, device)) {
			return;
		}

		this.cameraDevice = device;

		if (nonNull(cameraSource)) {
			try {
				cameraSource.setVideoCaptureDevice(cameraDevice);
			}
			catch (Throwable e) {
				notify(onException, new JanusPeerConnectionMediaException(
						MediaType.Camera, "Set video capture source failed", e));
			}
		}
	}

	public void setCameraCapability(VideoCaptureCapability capability) {
		if (Objects.equals(cameraCapability, capability)) {
			return;
		}

		this.cameraCapability = capability;

		if (nonNull(cameraCapability) && nonNull(cameraDevice)) {
			var nearestCapability = getNearestCameraFormat(cameraCapability);

			LOGGER.debug("Video capture capability: " + cameraCapability);
			LOGGER.debug("Video capture nearest capability: " + nearestCapability);

			if (nonNull(cameraSource)) {
				cameraSource.setVideoCaptureCapability(nearestCapability);

				if (getSenderTrackEnabled("cameraTrack")) {
					cameraSource.stop();

					try {
						cameraSource.start();
					}
					catch (Throwable e) {
						notify(onException, new JanusPeerConnectionMediaException(
								MediaType.Camera, "Start video capture source failed", e));
					}
				}
			}
		}
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

		AudioTrackSource audioSource = factory.getFactory().createAudioSource(audioOptions);
		AudioTrack audioTrack = factory.getFactory().createAudioTrack("audioTrack", audioSource);

		peerConnection.addTrack(audioTrack, List.of("stream"));

		setTransceiverDirection(direction, MediaStreamTrack.AUDIO_TRACK_KIND);
	}

	private void addVideo(RTCRtpTransceiverDirection direction) {
		if (!sendMedia(direction)) {
			return;
		}

		cameraSource = new VideoDeviceSource();

		if (nonNull(cameraDevice)) {
			LOGGER.debug("Video capture device: " + cameraDevice);

			cameraSource.setVideoCaptureDevice(cameraDevice);
		}
		if (nonNull(cameraCapability)) {
			var nearestCapability = getNearestCameraFormat(cameraCapability);

			LOGGER.debug("Video capture capability: " + cameraCapability);
			LOGGER.debug("Video capture nearest capability: " + nearestCapability);

			cameraSource.setVideoCaptureCapability(nearestCapability);
		}

		VideoTrack videoTrack = factory.getFactory().createVideoTrack("cameraTrack",
				cameraSource);

		try {
			cameraSource.start();
		}
		catch (Throwable e) {
			cameraSource.dispose();
			cameraSource = null;

			notify(onException, new JanusPeerConnectionMediaException(
					MediaType.Camera, "Start video capture source failed", e));
			return;
		}

		peerConnection.addTrack(videoTrack, List.of("stream"));

		setTransceiverDirection(direction, MediaStreamTrack.VIDEO_TRACK_KIND);
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
					LOGGER.debug("RTCDataChannel \"{}\" buffered amount changed to {}",
							channel.getLabel(),
							previousAmount);
				});
			}

			@Override
			public void onStateChange() {
				execute(() -> {
					LOGGER.debug("RTCDataChannel \"{}\" state: {}",
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

	private VideoCaptureCapability getNearestCameraFormat(VideoCaptureCapability capability) {
		int width = capability.width;
		int height = capability.height;
		var capabilities = MediaDevices.getVideoCaptureCapabilities(cameraDevice);
		VideoCaptureCapability nearest = null;

		Point2D formatPoint = new Point2D(width, height);
		Point2D tempPoint = new Point2D();
		Point2D tempPoint2 = new Point2D();

		double formatRatio = height / (double) width;

		double pointDistance = Double.MAX_VALUE;
		double ratioDistance = Double.MAX_VALUE;

		for (VideoCaptureCapability cap : capabilities) {
			tempPoint.set(cap.width, cap.height);

			double d = formatPoint.distance(tempPoint);
			double r = cap.height / (double) cap.width;
			double rd = Math.abs(formatRatio - r);

			if (d == 0) {
				// Perfect match.
				nearest = cap;
				break;
			}
			if (pointDistance > d && rd == 0) {
				// Best match within the same aspect ratio.
				pointDistance = d;
				nearest = cap;
			}
			else {
				// Compare point-ratio distance.
				tempPoint2.set(d, rd);

				double prd = tempPoint2.distance(new Point2D());

				if (ratioDistance > prd) {
					ratioDistance = prd;
					nearest = cap;
				}
			}
		}

		return nearest;
	}

	private void setReceiverTrackEnabled(String type, boolean enable) {
		for (RTCRtpReceiver receiver : peerConnection.getReceivers()) {
			MediaStreamTrack track = receiver.getTrack();

			if (nonNull(track) && track.getKind().equals(type)) {
				track.setEnabled(enable);

				LOGGER.debug("Receiver track \"{}\" set enabled to \"{}\"",
						track.getId(), enable);
				break;
			}
		}
	}

	private void setSenderTrackEnabled(String type, boolean enable) {
		for (RTCRtpSender sender : peerConnection.getSenders()) {
			MediaStreamTrack track = sender.getTrack();

			if (nonNull(track) && track.getKind().equals(type)) {
				track.setEnabled(enable);

				LOGGER.debug("Sender track \"{}\" set enabled to \"{}\"",
						track.getId(), enable);
				break;
			}
		}
	}

	private boolean getSenderTrackEnabled(String trackId) {
		for (RTCRtpSender sender : peerConnection.getSenders()) {
			MediaStreamTrack track = sender.getTrack();

			if (nonNull(track) && track.getId().equals(trackId)) {
				return track.isEnabled();
			}
		}

		return false;
	}

	private void setTransceiverDirection(RTCRtpTransceiverDirection direction,
			String kind) {
		for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
			MediaStreamTrack track = transceiver.getSender().getTrack();

			if (nonNull(track) && track.getKind().equals(kind)) {
				transceiver.setDirection(direction);
				break;
			}
		}
	}

	private void removeTrack(String trackName) {
		for (RTCRtpSender sender : peerConnection.getSenders()) {
			MediaStreamTrack track = sender.getTrack();

			if (nonNull(track) && track.getId().equals(trackName)) {
				peerConnection.removeTrack(sender);

				LOGGER.debug("Removed track \"{}\"", track.getId());
				break;
			}
		}
	}

	private void drainIceCandidates() {
		if (nonNull(queuedRemoteCandidates)) {
			LOGGER.debug("Add " + queuedRemoteCandidates.size() + " remote candidates");

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
					LOGGER.error("Send RTCSessionDescription failed", e);
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

	private <T> void notify(Consumer<T> consumer, T value) {
		if (nonNull(consumer)) {
			execute(() -> consumer.accept(value));
		}
	}

	private void execute(Runnable runnable) {
		executor.execute(runnable);
	}

	private static boolean sendMedia(RTCRtpTransceiverDirection direction) {
		return direction == RTCRtpTransceiverDirection.SEND_RECV
				|| direction == RTCRtpTransceiverDirection.SEND_ONLY;
	}

	private void disposeCameraSource() {
		if (nonNull(cameraSource)) {
			cameraSource.stop();
			cameraSource.dispose();
			cameraSource = null;
		}
	}

	private void disposeDataChannel() {
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
	}

	private void disposeDesktopSource() {
		if (nonNull(desktopSource)) {
			desktopSource.stop();
			desktopSource.dispose();
			desktopSource = null;
		}
	}

	private void disposePeerConnection() {
		if (nonNull(peerConnection)) {
			peerConnection.close();
		}
	}



	private class CreateSDObserver implements CreateSessionDescriptionObserver {

		@Override
		public void onSuccess(RTCSessionDescription description) {
			execute(() -> setLocalDescription(description));
		}

		@Override
		public void onFailure(String error) {
			execute(() -> {
				LOGGER.error("Create RTCSessionDescription failed: " + error);
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
				LOGGER.error("Set RTCSessionDescription failed: " + error);
			});
		}
	}
}
