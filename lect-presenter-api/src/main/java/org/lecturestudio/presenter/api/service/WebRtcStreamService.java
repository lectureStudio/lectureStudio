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

import com.google.common.eventbus.Subscribe;

import dev.onvoid.webrtc.RTCBundlePolicy;
import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.RTCIceTransportPolicy;
import dev.onvoid.webrtc.media.Device;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.codec.VideoCodecConfiguration;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.event.CameraStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.janus.client.JanusWebSocketClient;
import org.lecturestudio.web.api.message.SpeechAcceptMessage;
import org.lecturestudio.web.api.message.SpeechRejectMessage;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamWebSocketClient;
import org.lecturestudio.web.api.stream.config.WebRtcConfiguration;
import org.lecturestudio.web.api.stream.service.StreamService;
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

	private final DocumentService documentService;

	private final WebRtcStreamEventRecorder eventRecorder;

	@Inject
	@Named("stream.janus.websocket.url")
	private String janusWebSocketUrl;

	@Inject
	@Named("stream.state.websocket.url")
	private String streamStateWebSocketUrl;

	@Inject
	@Named("stream.publisher.api.url")
	private String streamPublisherApiUrl;

	@Inject
	@Named("stream.stun.servers")
	private String streamStunServers;

	private WebRtcConfiguration webRtcConfig;

	private StreamService streamService;

	private StreamWebSocketClient streamStateClient;

	private JanusWebSocketClient janusClient;

	private ExecutableState streamState;

	private ExecutableState cameraState;


	@Inject
	public WebRtcStreamService(ApplicationContext context,
			DocumentService documentService,
			WebRtcStreamEventRecorder eventRecorder)
			throws ExecutableException {
		this.context = context;
		this.documentService = documentService;
		this.eventRecorder = eventRecorder;

		eventRecorder.init();
	}

	@Subscribe
	public void onEvent(SpeechAcceptMessage message) {
		if (!started()) {
			return;
		}

		String userName = String.format("%s %s", message.getFirstName(), message.getFamilyName());

		janusClient.startRemoteSpeech(message.getRequestId(), userName);
		streamService.acceptSpeechRequest(message.getRequestId());
	}

	@Subscribe
	public void onEvent(SpeechRejectMessage message) {
		if (!started()) {
			return;
		}

		streamService.rejectSpeechRequest(message.getRequestId());
	}

	public void startCameraStream() throws ExecutableException {
		if (streamState != ExecutableState.Started
			|| cameraState == ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Starting);

		webRtcConfig.getVideoConfiguration().setSendVideo(true);

		setCameraState(ExecutableState.Started);
	}

	public void stopCameraStream() throws ExecutableException {
		if (cameraState != ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Stopping);

		webRtcConfig.getVideoConfiguration().setSendVideo(false);

		setCameraState(ExecutableState.Stopped);
	}

	public void mutePeerAudio(boolean mute) {
		if (!started()) {
			return;
		}

		webRtcConfig.getAudioConfiguration().setReceiveAudio(mute);
	}

	public void mutePeerVideo(boolean mute) {
		if (!started()) {
			return;
		}

		webRtcConfig.getVideoConfiguration().setReceiveVideo(mute);
	}

	public void stopPeerConnection() {
		if (!started()) {
			return;
		}

		janusClient.stopRemoteSpeech(0);
	}

	@Override
	protected void initInternal() {
		streamState = ExecutableState.Stopped;
		cameraState = ExecutableState.Stopped;
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (streamState == ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Starting);

		context.getEventBus().register(this);

		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();

		boolean streamCamera = config.getStreamConfig().getCameraEnabled();

		if (streamCamera) {
			setCameraState(ExecutableState.Starting);
		}

		webRtcConfig = createWebRtcConfig(config);
		streamStateClient = createStreamStateClient(config);
		janusClient = createJanusClient(webRtcConfig);

		eventRecorder.start();
		streamStateClient.start();
		janusClient.start();

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

		context.getEventBus().unregister(this);

		eventRecorder.stop();

		streamStateClient.stop();
		streamStateClient.destroy();

		janusClient.stop();
		janusClient.destroy();

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
	 * Sets the new camera state of this controller.
	 *
	 * @param state The new state.
	 */
	private void setCameraState(ExecutableState state) {
		this.cameraState = state;

		context.getEventBus().post(new CameraStateEvent(cameraState));
	}

	private StreamWebSocketClient createStreamStateClient(PresenterConfiguration config) {
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters stateWsParameters = new ServiceParameters();
		stateWsParameters.setUrl(streamStateWebSocketUrl);

		ServiceParameters streamApiParameters = new ServiceParameters();
		streamApiParameters.setUrl(streamPublisherApiUrl);

		TokenProvider tokenProvider = streamConfig::getAccessToken;

		streamService = new StreamService(streamApiParameters,
				tokenProvider);

		WebSocketHeaderProvider headerProvider = new WebSocketBearerTokenProvider(
				tokenProvider);

		return new StreamWebSocketClient(context.getEventBus(), stateWsParameters,
				headerProvider, eventRecorder, documentService, streamService,
				streamConfig.getCourse());
	}

	private JanusWebSocketClient createJanusClient(WebRtcConfiguration webRtcConfig) {
		ServiceParameters janusWsParameters = new ServiceParameters();
		janusWsParameters.setUrl(janusWebSocketUrl);

		return new JanusWebSocketClient(janusWsParameters, webRtcConfig,
				eventRecorder);
	}

	private WebRtcConfiguration createWebRtcConfig(PresenterConfiguration config) {
		AudioConfiguration audioConfig = config.getAudioConfig();
		StreamConfiguration streamConfig = config.getStreamConfig();
		VideoCodecConfiguration cameraConfig = streamConfig.getCameraCodecConfig();

		Rectangle2D cameraViewRect = cameraConfig.getViewRect();

		AudioDevice audioCaptureDevice = getDeviceByName(
				MediaDevices.getAudioCaptureDevices(),
				audioConfig.getInputDeviceName());
		VideoDevice videoCaptureDevice = getDeviceByName(
				MediaDevices.getVideoCaptureDevices(),
				streamConfig.getCameraName());

		RTCIceServer iceServer = new RTCIceServer();
		iceServer.urls.add(streamStunServers);

		WebRtcConfiguration webRtcConfig = new WebRtcConfiguration();
		webRtcConfig.getAudioConfiguration().setSendAudio(streamConfig.getMicrophoneEnabled());
		webRtcConfig.getAudioConfiguration().setReceiveAudio(true);
		webRtcConfig.getAudioConfiguration().setRecordingDevice(audioCaptureDevice);

		webRtcConfig.getVideoConfiguration().setSendVideo(streamConfig.getCameraEnabled());
		webRtcConfig.getVideoConfiguration().setReceiveVideo(true);
		webRtcConfig.getVideoConfiguration().setCaptureDevice(videoCaptureDevice);
		webRtcConfig.getVideoConfiguration().setCaptureCapability(
				new VideoCaptureCapability((int) cameraViewRect.getWidth(),
						(int) cameraViewRect.getHeight(),
						(int) streamConfig.getCameraFormat().getFrameRate()));
		webRtcConfig.getVideoConfiguration().setBitrate(cameraConfig.getBitRate());

		webRtcConfig.getRTCConfig().iceTransportPolicy = RTCIceTransportPolicy.ALL;
		webRtcConfig.getRTCConfig().bundlePolicy = RTCBundlePolicy.MAX_BUNDLE;
		webRtcConfig.getRTCConfig().iceServers.add(iceServer);

		webRtcConfig.setCourse(streamConfig.getCourse());

		webRtcConfig.setPeerStateConsumer(event -> {
			context.getEventBus().post(event);
		});
//		webRtcConfig.setOnRemoteVideoFrame(videoFrame -> {
//			context.getEventBus().post(videoFrame);
//		});

		streamConfig.enableMicrophoneProperty().addListener((o, oldValue, newValue) -> {
			webRtcConfig.getAudioConfiguration().setSendAudio(newValue);
		});

		return webRtcConfig;
	}
}
