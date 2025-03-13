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

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.media.Device;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.audio.AudioConverter;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateObserver;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioFrame;
import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.CameraStateEvent;
import org.lecturestudio.presenter.api.event.ScreenShareEndEvent;
import org.lecturestudio.presenter.api.event.ScreenShareStateEvent;
import org.lecturestudio.presenter.api.event.StreamReconnectStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.presenter.api.net.ScreenShareProfile;
import org.lecturestudio.web.api.client.ClientFailover;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.exception.StreamMediaException;
import org.lecturestudio.web.api.janus.JanusHandlerException;
import org.lecturestudio.web.api.janus.JanusHandlerException.Type;
import org.lecturestudio.web.api.janus.JanusParticipantContext;
import org.lecturestudio.web.api.janus.JanusPeerConnectionMediaException;
import org.lecturestudio.web.api.janus.JanusStateHandlerListener;
import org.lecturestudio.web.api.janus.client.JanusWebSocketClient;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.model.StreamConfig;
import org.lecturestudio.web.api.model.UserInfo;
import org.lecturestudio.web.api.net.Heartbeat;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.StreamAudioContext;
import org.lecturestudio.web.api.stream.StreamScreenContext;
import org.lecturestudio.web.api.stream.StreamVideoContext;
import org.lecturestudio.web.api.stream.client.StreamWebSocketClient;
import org.lecturestudio.web.api.stream.StreamContext;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.model.CourseParticipant;
import org.lecturestudio.web.api.stream.service.StreamProviderService;
import org.lecturestudio.web.api.websocket.WebSocketBearerTokenProvider;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;

/**
 * The {@code WebRtcStreamService} is the interface between user interface and
 * the WebRTC servers.
 *
 * @author Alex Andres
 */
@Singleton
public class WebRtcStreamService extends ExecutableBase {

	private final ApplicationContext context;

	private final WebRtcStreamEventRecorder eventRecorder;

	private final ClientFailover clientFailover;

	private final WebServiceInfo webServiceInfo;

	private final RecordingService recordingService;

	private final ExecutableStateObserver stateObserver;

	private final Heartbeat heartbeat;

	private final StreamConfig streamConfig;

	private StreamContext streamContext;

	private StreamProviderService streamProviderService;

	private StreamWebSocketClient streamStateClient;

	private JanusWebSocketClient janusSignalingClient;

	private AudioFrameProcessor audioProcessor;

	private ChangeListener<Rectangle2D> cameraFormatListener;

	private ChangeListener<String> cameraDeviceListener;

	private ChangeListener<String> captureDeviceListener;

	private ChangeListener<String> playbackDeviceListener;

	private ChangeListener<Double> playbackVolumeListener;

	private ChangeListener<ScreenShareProfile> screenProfileListener;

	private ExecutableState streamState;

	private ExecutableState cameraState;

	private ExecutableState screenShareState;

	private boolean hasFailed = false;


	@Inject
	public WebRtcStreamService(ApplicationContext context,
			WebServiceInfo webServiceInfo,
			WebRtcStreamEventRecorder eventRecorder,
			RecordingService recordingService)
			throws ExecutableException {
		this.context = context;
		this.webServiceInfo = webServiceInfo;
		this.eventRecorder = eventRecorder;
		this.recordingService = recordingService;
		this.streamConfig = new StreamConfig();
		this.stateObserver = new ExecutableStateObserver();
		this.heartbeat = new Heartbeat(context.getEventBus(), webServiceInfo.getStreamPublisherApiUrl(), 3000,
			Duration.ofSeconds(3));
		this.clientFailover = new ClientFailover();
		this.clientFailover.addStateListener((oldState, newState) -> {
			setReconnectionState(newState);
		});

		eventRecorder.init();
	}

	public StreamConfig getStreamConfig() {
		return streamConfig;
	}

	public void acceptSpeechRequest(JanusParticipantContext context) {
		if (!started()) {
			return;
		}

		janusSignalingClient.startRemoteSpeech(context);
		streamProviderService.acceptSpeechRequest(context.getRequestId());
	}

	public void rejectSpeechRequest(SpeechBaseMessage message) {
		if (!started()) {
			return;
		}

		streamProviderService.rejectSpeechRequest(message.getRequestId());
	}

	public void ban(CourseParticipant user) {
		if (!started()) {
			return;
		}

		long courseId = streamContext.getCourse().getId();

		streamProviderService.banParticipantFromCourse(courseId, user.getUserId());
	}

	public void startCameraStream() throws ExecutableException {
		if (streamState != ExecutableState.Started || cameraState == ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Starting);

		streamContext.getVideoContext().setSendVideo(true);

		setCameraState(ExecutableState.Started);
	}

	public void stopCameraStream() throws ExecutableException {
		if (cameraState != ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Stopping);

		streamContext.getVideoContext().setSendVideo(false);

		setCameraState(ExecutableState.Stopped);
	}

	public void setCaptureLocalCameraVideo(boolean capture) {
		if (cameraState != ExecutableState.Started) {
			return;
		}

		streamContext.getVideoContext().setCaptureLocalVideo(capture);
	}

	public void setScreenShareContext(ScreenShareContext context) {
		streamContext.getScreenContext().setFramerate(context.getProfile().getFramerate());
		streamContext.getScreenContext().setBitrate(context.getProfile().getBitrate());
		streamContext.getScreenContext().setScreenSource(context.getSource());
	}

	public void startScreenShare() throws ExecutableException {
		if (streamState != ExecutableState.Started
				|| screenShareState == ExecutableState.Started) {
			return;
		}

		setScreenShareState(ExecutableState.Starting);

		streamContext.getScreenContext().setSendVideo(true);

		setScreenShareState(ExecutableState.Started);
	}

	public void stopScreenShare() throws ExecutableException {
		if (screenShareState != ExecutableState.Started) {
			return;
		}

		setScreenShareState(ExecutableState.Stopping);

		streamContext.getScreenContext().setSendVideo(false);

		setScreenShareState(ExecutableState.Stopped);
	}

	public void stopPeerConnection(JanusParticipantContext context) {
		if (!started()) {
			return;
		}

		streamProviderService.rejectSpeechRequest(context.getRequestId());
		janusSignalingClient.stopRemoteSpeech(context);
	}

	public void shareDocument(Document document) throws IOException {
		if (streamState == ExecutableState.Started) {
			eventRecorder.shareDocument(document);
		}
	}

	public ExecutableState getScreenShareState() {
		return screenShareState;
	}

	@Override
	protected void initInternal() {
		streamState = ExecutableState.Stopped;
		cameraState = ExecutableState.Stopped;
		screenShareState = ExecutableState.Stopped;
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (streamState == ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Starting);

		PresenterContext pContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		Course course = pContext.getCourse();

		boolean streamCamera = streamConfig.getCameraEnabled();

		if (streamCamera) {
			setCameraState(ExecutableState.Starting);
		}

		StreamStateHandler streamStateHandler = new StreamStateHandler();

		audioProcessor = new AudioFrameProcessor(config.getAudioConfig()
				.getRecordingFormat());

		streamContext = createStreamContext(course, config);
		streamStateClient = createStreamStateClient(config);
		janusSignalingClient = createJanusClient(streamContext);
		janusSignalingClient.addStateListener((oldState, newState) -> {
			if (newState == ExecutableState.Started) {
				if (hasFailed) {
					streamProviderService.restartedStream(course.getId());
				}

				hasFailed = false;
			}
		});
		janusSignalingClient.addJanusStateHandlerListener(new JanusStateHandlerListener() {

			@Override
			public void connected() {
				if (clientFailover.started()) {
					try {
						clientFailover.stop();
					}
					catch (ExecutableException e) {
						logException(e, "Stop connection failover failed");
					}
				}
				else {
					setReconnectionState(ExecutableState.Stopped);
				}
			}

			@Override
			public void disconnected() {
				if (started()) {
					setReconnectionState(ExecutableState.Started);
				}
			}

			@Override
			public void failed() {
				hasFailed = true;

				if (started()) {
					// Upon unexpected disruption start the recovery process.
					try {
						clientFailover.start();
					}
					catch (ExecutableException e) {
						logException(e, "Start connection failover failed");
					}
				}
			}

			@Override
			public void error(Throwable throwable) {
				logException(throwable, "Janus state error");

				if (throwable instanceof JanusHandlerException handlerException) {
					Throwable cause = throwable.getCause();

					if (handlerException.getType() == Type.PUBLISHER
							&& cause instanceof JanusPeerConnectionMediaException pcMediaException) {

						context.getEventBus().post(new StreamMediaException(
								pcMediaException.getMediaType(),
								pcMediaException));
					}
				}
			}
		});
		janusSignalingClient.addJanusStateHandlerListener(streamStateHandler);

		UserInfo userInfo = streamProviderService.getUserInfo();

		pContext.getUserPrivilegeService().setUserInfo(userInfo);
		pContext.getUserPrivilegeService().setPrivileges(
				streamProviderService.getUserPrivileges(course.getId())
						.getPrivileges());

		streamContext.setUserInfo(userInfo);

		eventRecorder.setCourse(course);
		eventRecorder.setStreamProviderService(streamProviderService);

		clientFailover.addExecutable(janusSignalingClient);
		clientFailover.addExecutable(streamStateClient.getReconnectExecutable());

		stateObserver.addExecutable(eventRecorder);
		stateObserver.addExecutable(streamStateHandler);
		stateObserver.setStartedListener(() -> {
			sendMediaStreamState();
			streamProviderService.startedStream(course.getId());

			if (this.streamConfig.getStartChat()) {
				pContext.setMessengerStarted(true);
			}
		});

		try {
			streamStateClient.start();
			janusSignalingClient.start();

			// As of now, it's mandatory to start the event-recorder after the
			// clients started.
			eventRecorder.start();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		cameraFormatListener = (o, oldValue, newValue) -> {
			streamContext.getVideoContext().setCaptureCapability(
					new VideoCaptureCapability((int) newValue.getWidth(),
							(int) newValue.getHeight(),
							(int) streamConfig.getCameraFormat()
									.getFrameRate()));
			streamContext.getVideoContext().setBitrate(
					streamConfig.getCameraCodecConfig().getBitRate());
		};
		cameraDeviceListener = (o, oldValue, newValue) -> {
			VideoDevice cameraDevice = getDeviceByName(
					MediaDevices.getVideoCaptureDevices(),
					streamConfig.getCameraName());

			streamContext.getVideoContext().setCaptureDevice(cameraDevice);
		};
		captureDeviceListener = (o, oldValue, newValue) -> {
			AudioDevice captureDevice = getDeviceByName(
					MediaDevices.getAudioCaptureDevices(),
					audioConfig.getCaptureDeviceName());

			streamContext.getAudioContext().setRecordingDevice(captureDevice);
		};
		playbackDeviceListener = (o, oldValue, newValue) -> {
			AudioDevice playbackDevice = getDeviceByName(
					MediaDevices.getAudioRenderDevices(),
					audioConfig.getPlaybackDeviceName());

			streamContext.getAudioContext().setPlaybackDevice(playbackDevice);
		};
		playbackVolumeListener = (o, oldValue, newValue) -> {
			streamContext.getAudioContext().setPlaybackVolume(newValue);
		};
		screenProfileListener = (o, oldValue, newValue) -> {
			streamContext.getScreenContext().setFramerate(newValue.getFramerate());
			streamContext.getScreenContext().setBitrate(newValue.getBitrate());
		};

		streamConfig.screenProfileProperty().addListener(screenProfileListener);
		streamConfig.getCameraCodecConfig().viewRectProperty().addListener(cameraFormatListener);
		streamConfig.cameraNameProperty().addListener(cameraDeviceListener);
		audioConfig.captureDeviceNameProperty().addListener(captureDeviceListener);
		audioConfig.playbackDeviceNameProperty().addListener(playbackDeviceListener);
		audioConfig.playbackVolumeProperty().addListener(playbackVolumeListener);

		heartbeat.start();

		setStreamState(ExecutableState.Started);

		if (streamCamera) {
			setCameraState(ExecutableState.Started);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (streamState != ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Stopping);

		stateObserver.removeAllExecutables();

		try {
			eventRecorder.stop();

			disposeExecutable(clientFailover);
			disposeExecutable(streamStateClient);
			disposeExecutable(janusSignalingClient);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		heartbeat.stop();

		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();

		streamConfig.screenProfileProperty().removeListener(screenProfileListener);
		streamConfig.getCameraCodecConfig().viewRectProperty().removeListener(cameraFormatListener);
		streamConfig.cameraNameProperty().removeListener(cameraDeviceListener);
		audioConfig.captureDeviceNameProperty().removeListener(captureDeviceListener);
		audioConfig.playbackDeviceNameProperty().removeListener(playbackDeviceListener);
		audioConfig.playbackVolumeProperty().removeListener(playbackVolumeListener);

		setStreamState(ExecutableState.Stopped);
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		eventRecorder.destroy();
	}

	/**
	 * Searches the provided list for a device with the provided name.
	 *
	 * @param devices The device list in which to search for the device.
	 * @param name    The name of the device to search for.
	 * @param <T>     The device type.
	 *
	 * @return The device with the specified name or {@code null} if not found.
	 */
	private <T extends Device> T getDeviceByName(List<T> devices, String name) {
		return devices.stream()
				.filter(device -> device.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Sets the new stream state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setStreamState(ExecutableState state) {
		this.streamState = state;

		context.getEventBus().post(new StreamingStateEvent(streamState));
	}

	/**
	 * Sets the new screen-sharing state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setScreenShareState(ExecutableState state) {
		this.screenShareState = state;

		context.getEventBus().post(new ScreenShareStateEvent(
				streamContext.getScreenContext().getScreenSource(),
				screenShareState));
	}

	/**
	 * Sets the new camera state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setCameraState(ExecutableState state) {
		this.cameraState = state;

		context.getEventBus().post(new CameraStateEvent(cameraState));
	}

	private void sendMediaStreamState() {
		long courseId = streamContext.getCourse().getId();

		// Send media stream state when course state has been initialized.
		boolean camEnabled = streamContext.getVideoContext().getSendVideo();
		boolean micEnabled = streamContext.getAudioContext().getSendAudio();
		boolean screenEnabled = streamContext.getScreenContext().getSendVideo();

		Map<MediaType, Boolean> mediaStateMap = new HashMap<>();
		mediaStateMap.put(MediaType.Audio, micEnabled);
		mediaStateMap.put(MediaType.Camera, camEnabled);
		mediaStateMap.put(MediaType.Screen, screenEnabled);

		streamProviderService.updateStreamMediaState(courseId, mediaStateMap);
	}

	private void disposeExecutable(Executable executable) throws ExecutableException {
		if (executable.started()) {
			executable.stop();
		}
		if (executable.stopped()) {
			executable.destroy();
		}
	}

	private void setReconnectionState(ExecutableState state) {
		context.getEventBus().post(new StreamReconnectStateEvent(state));
	}

	private StreamWebSocketClient createStreamStateClient(
			PresenterConfiguration config) {
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters stateWsParameters = new ServiceParameters();
		stateWsParameters.setUrl(webServiceInfo.getStreamStateWebSocketUrl());

		ServiceParameters streamApiParameters = new ServiceParameters();
		streamApiParameters.setUrl(webServiceInfo.getStreamPublisherApiUrl());

		TokenProvider tokenProvider = streamConfig::getAccessToken;

		streamProviderService = new StreamProviderService(streamApiParameters,
				tokenProvider);

		WebSocketHeaderProvider headerProvider = new WebSocketBearerTokenProvider(tokenProvider);

		return new StreamWebSocketClient(stateWsParameters, headerProvider,
				eventRecorder);
	}

	private JanusWebSocketClient createJanusClient(StreamContext streamContext) {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters janusWsParameters = new ServiceParameters();
		janusWsParameters.setUrl(MessageFormat.format(webServiceInfo.getJanusWebSocketUrl(),
				streamConfig.getServerName()));

		return new JanusWebSocketClient(janusWsParameters, streamContext,
				eventRecorder);
	}

	private StreamContext createStreamContext(Course course, PresenterConfiguration config) {
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		Rectangle2D cameraViewRect = cameraConfig.getViewRect();

		AudioDevice audioPlaybackDevice = getDeviceByName(
				MediaDevices.getAudioRenderDevices(),
				audioConfig.getPlaybackDeviceName());
		AudioDevice audioCaptureDevice = getDeviceByName(
				MediaDevices.getAudioCaptureDevices(),
				audioConfig.getCaptureDeviceName());
		VideoDevice videoCaptureDevice = getDeviceByName(
				MediaDevices.getVideoCaptureDevices(),
				streamConfig.getCameraName());

		StreamContext streamContext = new StreamContext();
		StreamAudioContext audioContext = streamContext.getAudioContext();
		StreamVideoContext videoContext = streamContext.getVideoContext();
		StreamScreenContext screenContext = streamContext.getScreenContext();

		audioContext.setSendAudio(streamConfig.getMicrophoneEnabled());
		audioContext.setReceiveAudio(true);
		audioContext.setRecordingDevice(audioCaptureDevice);
		audioContext.setPlaybackDevice(audioPlaybackDevice);
		audioContext.setPlaybackVolume(audioConfig.getPlaybackVolume());
		audioContext.setRemoteFrameConsumer(this::processAudioFrame);

		videoContext.setSendVideo(streamConfig.getCameraEnabled());
		videoContext.setReceiveVideo(true);
		videoContext.setCaptureDevice(videoCaptureDevice);
		videoContext.setCaptureLocalVideo(true);
		videoContext.setBitrate(cameraConfig.getBitRate());

		screenContext.setLocalFrameConsumer(videoFrameEvent -> {
			context.getEventBus().post(videoFrameEvent);
		});
		screenContext.setScreenSourceEndedCallback(() -> {
			context.getEventBus().post(new ScreenShareEndEvent(true));
		});

		// Register media stream state handlers.
		audioContext.sendAudioProperty().addListener((o, oldValue, newValue) -> {
			streamProviderService.updateStreamMediaState(course.getId(),
					Map.of(MediaType.Audio, newValue));
		});
		videoContext.sendVideoProperty().addListener((o, oldValue, newValue) -> {
			streamProviderService.updateStreamMediaState(course.getId(),
					Map.of(MediaType.Camera, newValue));
		});
		screenContext.sendVideoProperty().addListener((o, oldValue, newValue) -> {
			streamProviderService.updateStreamMediaState(course.getId(),
					Map.of(MediaType.Screen, newValue));
		});

		if (nonNull(streamConfig.getCameraFormat())) {
			videoContext.setCaptureCapability(new VideoCaptureCapability(
					(int) cameraViewRect.getWidth(),
					(int) cameraViewRect.getHeight(),
					(int) streamConfig.getCameraFormat().getFrameRate()));
		}

		RTCIceServer iceServer = new RTCIceServer();
		iceServer.urls.add(webServiceInfo.getStreamStunServers());

		streamContext.getRTCConfig().iceServers.add(iceServer);

		streamContext.setCourse(course);

		streamContext.setPeerStateConsumer(event -> {
			context.getEventBus().post(event);
		});

		streamConfig.enableMicrophoneProperty().addListener((o, oldValue, newValue) -> {
			audioContext.setSendAudio(newValue);
		});

		return streamContext;
	}

	private void processAudioFrame(final AudioFrame frame) {
		audioProcessor.onAudioFrame(frame);
	}



	private class AudioFrameProcessor {

		private final AudioFormat audioFormat;

		private AudioConverter audioConverter;

		private int lastSampleRate;

		private int lastChannels;


		public AudioFrameProcessor(AudioFormat audioFormat) {
			this.audioFormat = audioFormat;
		}

		public void onAudioFrame(AudioFrame frame) {
			final int sampleRate = frame.getSampleRate();
			final int channels = frame.getChannels();

			if (nonNull(audioConverter) && (lastSampleRate != sampleRate
					|| lastChannels != channels)) {
				audioConverter.dispose();
				audioConverter = null;
			}
			if (isNull(audioConverter)) {
				lastSampleRate = sampleRate;
				lastChannels = channels;

				audioConverter = new AudioConverter(sampleRate, channels,
						audioFormat.getSampleRate(), audioFormat.getChannels());
			}

			byte[] buffer = new byte[audioConverter.getTargetBufferSize()];

			try {
				int converted = audioConverter.convert(frame.getData(), buffer);

				recordingService.addAudioFrame(new AudioFrame(buffer,
						audioFormat.getBitsPerSample(), audioFormat.getSampleRate(),
						audioFormat.getChannels(), converted));
			}
			catch (Throwable e) {
				logException(e, "Convert peer audio failed");
			}
		}
	}
}
