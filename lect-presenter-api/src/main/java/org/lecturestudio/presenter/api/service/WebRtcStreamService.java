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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.event.CameraStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.web.api.janus.client.JanusWebSocketClient;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamWebSocketClient;
import org.lecturestudio.web.api.stream.config.WebRtcConfiguration;
import org.lecturestudio.web.api.stream.config.WebRtcDefaultConfiguration;
import org.lecturestudio.web.api.stream.service.StreamService;

/**
 * The {@code WebRtcStreamService} is the interface between user interface and
 * the WebRTC servers.
 *
 * @author Alex Andres
 */
@Singleton
public class WebRtcStreamService extends ExecutableBase {

	@Inject
	private ApplicationContext context;

	@Inject
	private DocumentService documentService;

	private StreamWebSocketClient streamStateClient;

	private JanusWebSocketClient janusClient;

	private ExecutableState streamState;

	private ExecutableState cameraState;


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
	protected void initInternal() throws ExecutableException {
		streamState = ExecutableState.Stopped;
		cameraState = ExecutableState.Stopped;

		PresenterConfiguration config = (PresenterConfiguration) context
				.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters janusWsParameters = new ServiceParameters();
		janusWsParameters.setUrl("wss://lecturestudio.dek.e-technik.tu-darmstadt.de:8989");

		ServiceParameters stateWsParameters = new ServiceParameters();
		stateWsParameters.setUrl("wss://lecturestudio.dek.e-technik.tu-darmstadt.de/api/publisher/course-state");

		ServiceParameters streamApiParameters = new ServiceParameters();
		streamApiParameters.setUrl("https://lecturestudio.dek.e-technik.tu-darmstadt.de");

		WebRtcConfiguration webRtcConfig = new WebRtcDefaultConfiguration();
		webRtcConfig.setCourse(streamConfig.getCourse());

		StreamService streamService = new StreamService(streamApiParameters,
				streamConfig::getAccessToken);

		LectureRecorder lectureRecorder = new WebRtcStreamEventRecorder(
				documentService);

		streamStateClient = new StreamWebSocketClient(stateWsParameters,
				lectureRecorder, streamService);

		janusClient = new JanusWebSocketClient(janusWsParameters, webRtcConfig,
				lectureRecorder);
		janusClient.init();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (streamState == ExecutableState.Started) {
			return;
		}

		setStreamState(ExecutableState.Starting);

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

		streamStateClient.stop();
		streamStateClient.destroy();

		janusClient.stop();
		janusClient.destroy();

		setStreamState(ExecutableState.Stopped);
	}

	@Override
	protected void destroyInternal() {

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
}
