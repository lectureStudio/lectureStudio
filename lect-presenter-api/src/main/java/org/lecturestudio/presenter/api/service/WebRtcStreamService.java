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
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.event.CameraStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.janus.client.JanusWebSocketClient;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamWebSocketClient;
import org.lecturestudio.web.api.stream.config.WebRtcConfiguration;
import org.lecturestudio.web.api.stream.config.WebRtcDefaultConfiguration;
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

	public void startCameraStream() throws ExecutableException {
		if (cameraState == ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Starting);


		setCameraState(ExecutableState.Started);
	}

	public void stopCameraStream() throws ExecutableException {
		if (cameraState != ExecutableState.Started) {
			return;
		}

		setCameraState(ExecutableState.Stopping);


		setCameraState(ExecutableState.Stopped);
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

		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();

		streamStateClient = createStreamStateClient(config);
		janusClient = createJanusClient(createWebRtcConfig(config));

		eventRecorder.start();
		streamStateClient.start();
		janusClient.start();

		setStreamState(ExecutableState.Started);
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (streamState != ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Stopping);

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

		StreamService streamService = new StreamService(streamApiParameters,
				tokenProvider);

		WebSocketHeaderProvider headerProvider = new WebSocketBearerTokenProvider(
				tokenProvider);

		return new StreamWebSocketClient(stateWsParameters,
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

		Rectangle2D cameraViewRect = streamConfig.getCameraCodecConfig().getViewRect();

		AudioDevice audioCaptureDevice = getDeviceByName(
				MediaDevices.getAudioCaptureDevices(),
				audioConfig.getInputDeviceName());
		VideoDevice videoCaptureDevice = getDeviceByName(
				MediaDevices.getVideoCaptureDevices(),
				streamConfig.getCameraName());

		WebRtcConfiguration webRtcConfig = new WebRtcDefaultConfiguration();
		webRtcConfig.getAudioConfiguration().setRecordingDevice(audioCaptureDevice);
		webRtcConfig.getVideoConfiguration().setCaptureDevice(videoCaptureDevice);
		webRtcConfig.getVideoConfiguration().setCaptureCapability(
				new VideoCaptureCapability((int) cameraViewRect.getWidth(),
						(int) cameraViewRect.getHeight(),
						(int) streamConfig.getCameraFormat().getFrameRate()));
		webRtcConfig.setCourse(streamConfig.getCourse());

		return webRtcConfig;
	}
}
