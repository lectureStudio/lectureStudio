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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.web.api.model.ScreenSource;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCAnswerOptions;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelInit;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceConnectionState;
import dev.onvoid.webrtc.RTCIceGatheringState;
import dev.onvoid.webrtc.RTCOfferOptions;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpReceiver;
import dev.onvoid.webrtc.RTCRtpSendParameters;
import dev.onvoid.webrtc.RTCRtpSender;
import dev.onvoid.webrtc.RTCRtpTransceiver;
import dev.onvoid.webrtc.RTCRtpTransceiverDirection;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;

import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.MediaStreamTrackState;
import dev.onvoid.webrtc.media.audio.AudioOptions;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.audio.AudioTrackSource;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDesktopSource;
import dev.onvoid.webrtc.media.video.VideoDevice;
import dev.onvoid.webrtc.media.video.VideoDeviceSource;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSink;

public class JanusPeerConnection implements PeerConnectionObserver {

	private static final Logger LOGGER = LogManager.getLogger(JanusPeerConnection.class);

	private static final String MICROPHONE_TRACK = "microphone";
	private static final String CAMERA_TRACK = "camera";
	private static final String SCREEN_TRACK = "screen";

	private final JanusScreenShareConfig screenShareConfig = new JanusScreenShareConfig();

	private final JanusPeerConnectionFactory factory;

	private final ExecutorService executor;

	private final RTCPeerConnection peerConnection;

	private RTCDataChannel dataChannel;

	private RTCDataChannel remoteDataChannel;

	private VideoDesktopSource desktopSource;

	private VideoDeviceSource cameraSource;

	private VideoDevice cameraDevice;

	private VideoCaptureCapability cameraCapability;

	private VideoTrack localVideoTrack;

	private VideoTrackSink localVideoTrackSink;

	private Consumer<JanusPeerConnectionException> onException;

	private Consumer<RTCSessionDescription> onLocalSessionDescription;

	private Consumer<RTCIceCandidate> onIceCandidate;

	private Consumer<RTCIceConnectionState> onIceConnectionState;

	private Consumer<RTCIceGatheringState> onIceGatheringState;

	private Consumer<RTCPeerConnectionState> onPeerConnectionState;

	private Consumer<VideoFrame> onLocalVideoFrame;
	private Consumer<VideoFrame> onRemoteVideoFrame;

	private Consumer<MediaStreamTrack> onReplacedTrack;

	private AudioTrackSink audioTrackSink;


	public JanusPeerConnection(JanusPeerConnectionFactory factory) {
		this.factory = factory;
		this.executor = factory.getExecutor();
		this.peerConnection = factory.createPeerConnection(this);
	}

	public JanusScreenShareConfig getScreenShareConfig() {
		return screenShareConfig;
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

	public void setOnIceConnectionState(Consumer<RTCIceConnectionState> callback) {
		onIceConnectionState = callback;
	}

	public void setOnPeerConnectionState(Consumer<RTCPeerConnectionState> callback) {
		onPeerConnectionState = callback;
	}

	public void setOnLocalVideoFrame(Consumer<VideoFrame> callback) {
		onLocalVideoFrame = callback;

		execute(() -> {
			// Remove old video track callback.
			if (nonNull(localVideoTrack) && nonNull(localVideoTrackSink)) {
				localVideoTrack.removeSink(localVideoTrackSink);
			}

			if (nonNull(onLocalVideoFrame)) {
				localVideoTrackSink = frame -> publishFrame(onLocalVideoFrame, frame);

				if (nonNull(localVideoTrack)) {
					localVideoTrack.addSink(localVideoTrackSink);
				}
			}
			else {
				localVideoTrackSink = null;
			}
		});
	}

	public void setOnRemoteVideoFrame(Consumer<VideoFrame> callback) {
		onRemoteVideoFrame = callback;
	}

	public void setOnReplacedTrack(Consumer<MediaStreamTrack> callback) {
		this.onReplacedTrack = callback;
	}

	public void setAudioTrackSink(AudioTrackSink sink) {
		audioTrackSink = sink;
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
		LOGGER.debug("Peer connection state: " + state);

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
		String kind = track.getKind();

		if (kind.equals(MediaStreamTrack.AUDIO_TRACK_KIND) && nonNull(audioTrackSink)) {
			AudioTrack audioTrack = (AudioTrack) track;
			audioTrack.addSink(audioTrackSink);
		}
		if (kind.equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
			VideoTrack videoTrack = (VideoTrack) track;
			videoTrack.addSink(frame -> publishFrame(onRemoteVideoFrame, frame));
		}
	}

	public RTCRtpTransceiver[] getTransceivers() {
		return peerConnection.getTransceivers();
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

	public void close() {
		execute(() -> {
			disposeCameraSource();
			disposeDataChannel();
			disposeDesktopSource();
			disposePeerConnection();
		});
	}

	public void setup(RTCRtpTransceiverDirection audio,
			RTCRtpTransceiverDirection video,
			RTCRtpTransceiverDirection screen) {
		execute(() -> {
			addDataChannel();
			addAudio(audio);
			addVideo(video);
			addScreenVideo(screen);

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
			setSenderTrackEnabled(MICROPHONE_TRACK, enable);
		});
	}

	public void setCameraEnabled(boolean enable) {
		execute(() -> {
			if (isNull(cameraSource)) {
				notify(onException, new JanusPeerConnectionMediaException(
						MediaType.Camera, "No camera source created"));
				return;
			}

			if (enable) {
				try {
					if (nonNull(cameraDevice)) {
						LOGGER.debug("Video capture device: " + cameraDevice);

						cameraSource.setVideoCaptureDevice(cameraDevice);
					}
					if (nonNull(cameraCapability)) {
						var nearestCapability = getNearestCameraFormat(
								cameraCapability);

						LOGGER.debug("Video capture capability: " + cameraCapability);
						LOGGER.debug("Video capture nearest capability: " + nearestCapability);

						cameraSource.setVideoCaptureCapability(nearestCapability);
					}

					cameraSource.start();
				}
				catch (Throwable e) {
					notify(onException, new JanusPeerConnectionMediaException(
							MediaType.Camera, "Start video capture source failed", e));
					return;
				}
			}
			else {
				cameraSource.stop();
			}

			setSenderTrackEnabled(CAMERA_TRACK, enable);
		});
	}

	public void setScreenShareEnabled(boolean enable) {
		if (isNull(desktopSource)) {
			notify(onException, new JanusPeerConnectionMediaException(
					MediaType.Screen, "No screen capture source created"));
			return;
		}

		execute(() -> {
			if (enable) {
				ScreenSource screenSource = screenShareConfig.getScreenSource();

				if (isNull(screenSource)) {
					notify(onException, new JanusPeerConnectionMediaException(
							MediaType.Screen, "Start screen capture source failed"));
					return;
				}

				LOGGER.debug("Screen-Share capability: FrameRate={}, BitRate={}",
						screenShareConfig.getFrameRate(), screenShareConfig.getBitRate());

				for (RTCRtpSender sender : peerConnection.getSenders()) {
					MediaStreamTrack track = sender.getTrack();

					// Check if the current track has ended and create a new one.
					if (nonNull(track) && track.getId().equals(SCREEN_TRACK)) {
						// Set screen encoding constraints.
						setSenderConstraints(sender,
								screenShareConfig.getFrameRate(),
								screenShareConfig.getBitRate());

						if (track.getState() == MediaStreamTrackState.ENDED) {
							VideoTrack videoTrack = factory.getFactory()
									.createVideoTrack(SCREEN_TRACK, desktopSource);

							sender.replaceTrack(videoTrack);

							notify(onReplacedTrack, videoTrack);
						}
					}
				}

				try {
					desktopSource.setSourceId(screenSource.getId(),
							screenSource.isWindow());
					desktopSource.setFrameRate(screenShareConfig.getFrameRate());
					desktopSource.start();
				}
				catch (Throwable e) {
					notify(onException, new JanusPeerConnectionMediaException(
							MediaType.Screen, "Start screen capture source failed", e));
					return;
				}
			}
			else {
				desktopSource.stop();
			}

			setSenderTrackEnabled(SCREEN_TRACK, enable);
		});
	}

	public void setCameraDevice(VideoDevice device) {
		if (isNull(device)) {
			return;
		}
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

		AudioTrackSource audioSource = factory.getFactory().createAudioSource(audioOptions);
		AudioTrack audioTrack = factory.getFactory().createAudioTrack(MICROPHONE_TRACK, audioSource);

		peerConnection.addTrack(audioTrack, List.of("stream"));

		setTransceiverDirection(MICROPHONE_TRACK, direction);
	}

	private void addVideo(RTCRtpTransceiverDirection direction) {
		if (!sendMedia(direction)) {
			return;
		}

		cameraSource = new VideoDeviceSource();

		localVideoTrack = factory.getFactory()
				.createVideoTrack(CAMERA_TRACK, cameraSource);

		if (nonNull(onLocalVideoFrame) && nonNull(localVideoTrackSink)) {
			localVideoTrack.addSink(localVideoTrackSink);
		}

		peerConnection.addTrack(localVideoTrack, List.of("stream"));

		setTransceiverDirection(CAMERA_TRACK, direction);
	}

	private void addScreenVideo(RTCRtpTransceiverDirection direction) {
		if (!sendMedia(direction)) {
			return;
		}

		desktopSource = new VideoDesktopSource();

		VideoTrack videoTrack = factory.getFactory()
				.createVideoTrack(SCREEN_TRACK, desktopSource);

		peerConnection.addTrack(videoTrack, List.of("stream"));

		setTransceiverDirection(SCREEN_TRACK, direction);
	}

	private void addDataChannel() {
		RTCDataChannelInit dict = new RTCDataChannelInit();
		dict.protocol = "stream-messaging";

		dataChannel = peerConnection.createDataChannel("events", dict);
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

	private void setSenderConstraints(RTCRtpSender sender, double framerate, int bitrate) {
		RTCRtpSendParameters sendParams = sender.getParameters();
		int minBitrate = bitrate * 500;
		int maxBitrate = bitrate * 1000;

		for (var encoding : sendParams.encodings) {
			encoding.minBitrate = minBitrate;
			encoding.maxBitrate = maxBitrate;
			encoding.maxFramerate = framerate;
		}

		sender.setParameters(sendParams);

		LOGGER.debug("Sender encoding parameters set to: minBitrate = {}, maxBitrate = {}, maxFramerate = {}",
				minBitrate, maxBitrate, framerate);
	}

	private void setSenderTrackEnabled(String trackId, boolean enable) {
		for (RTCRtpSender sender : peerConnection.getSenders()) {
			MediaStreamTrack track = sender.getTrack();

			if (nonNull(track) && track.getId().equals(trackId)) {
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

	private void setTransceiverDirection(String trackId,
			RTCRtpTransceiverDirection direction) {
		for (RTCRtpTransceiver transceiver : peerConnection.getTransceivers()) {
			MediaStreamTrack track = transceiver.getSender().getTrack();

			if (nonNull(track) && track.getId().equals(trackId)) {
				transceiver.setDirection(direction);
				break;
			}
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

		}

		@Override
		public void onFailure(String error) {
			execute(() -> {
				LOGGER.error("Set RTCSessionDescription failed: " + error);
			});
		}
	}
}
