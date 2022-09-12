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

package org.lecturestudio.web.api.janus;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginMessage;
import org.lecturestudio.web.api.janus.state.AttachPluginState;
import org.lecturestudio.web.api.janus.state.CreateRoomState;
import org.lecturestudio.web.api.model.ScreenSource;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.StreamScreenContext;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.StreamAudioContext;
import org.lecturestudio.web.api.stream.StreamVideoContext;

import dev.onvoid.webrtc.media.video.VideoCaptureCapability;
import dev.onvoid.webrtc.media.video.VideoDevice;

public class JanusPublisherHandler extends JanusStateHandler {

	private final StreamEventRecorder eventRecorder;

	private final StreamAudioContext audioContext;

	private final StreamVideoContext videoContext;

	private final StreamScreenContext screenContext;

	private ChangeListener<Boolean> enableMicListener;

	private ChangeListener<Boolean> enableCamListener;

	private ChangeListener<VideoDevice> camListener;

	private ChangeListener<VideoCaptureCapability> camCapabilityListener;

	private ChangeListener<ScreenSource> screenSourceListener;


	public JanusPublisherHandler(JanusPeerConnectionFactory factory,
			JanusMessageTransmitter transmitter,
			StreamEventRecorder eventRecorder) {
		super(factory, transmitter);

		this.eventRecorder = eventRecorder;
		this.audioContext = getStreamContext().getAudioContext();
		this.videoContext = getStreamContext().getVideoContext();
		this.screenContext = getStreamContext().getScreenContext();
	}

	@Override
	public <T extends JanusMessage> void handleMessage(T message) throws Exception {
		if (message instanceof JanusPluginMessage) {
			JanusPluginMessage pluginMessage = (JanusPluginMessage) message;

			// Accept only messages that are addressed to this handler.
			if (!pluginMessage.getHandleId().equals(getPluginId())) {
				return;
			}
		}

		super.handleMessage(message);
	}

	@Override
	public JanusPeerConnection createPeerConnection() {
		JanusPeerConnection peerConnection = super.createPeerConnection();
		peerConnection.setOnException(this::setError);
		peerConnection.setOnIceConnectionState(state -> {
			switch (state) {
				case CONNECTED:
					setConnected();
					break;

				case DISCONNECTED:
				case CLOSED:
					setDisconnected();
					break;
			}
		});

		audioContext.sendAudioProperty().addListener(enableMicListener);
		videoContext.sendVideoProperty().addListener(enableCamListener);
		videoContext.captureDeviceProperty().addListener(camListener);
		videoContext.captureCapabilityProperty().addListener(camCapabilityListener);
		screenContext.screenSourceProperty().addListener(screenSourceListener);

		return peerConnection;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		enableMicListener = (observable, oldValue, newValue) -> {
			peerConnection.setMicrophoneEnabled(newValue);
		};

		enableCamListener = (observable, oldValue, newValue) -> {
			peerConnection.setCameraEnabled(newValue);
		};

		camListener = (observable, oldDevice, newDevice) -> {
			peerConnection.setCameraDevice(newDevice);
			peerConnection.setCameraEnabled(videoContext.getSendVideo());
		};

		camCapabilityListener = (observable, oldCapability, newCapability) -> {
			peerConnection.setCameraCapability(newCapability);
		};

		screenSourceListener = (observable, oldValue, newValue) -> {
			peerConnection.setScreenSource(newValue);
			peerConnection.setScreenShareEnabled(nonNull(newValue));
		};
	}

	@Override
	protected void startInternal() throws ExecutableException {
		eventRecorder.addRecordedActionConsumer(this::sendStreamAction);

		setState(new AttachPluginState(new CreateRoomState()));
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		eventRecorder.removeRecordedActionConsumer(this::sendStreamAction);

		audioContext.sendAudioProperty().removeListener(enableMicListener);
		videoContext.sendVideoProperty().removeListener(enableCamListener);
		videoContext.captureDeviceProperty().removeListener(camListener);
		videoContext.captureCapabilityProperty().removeListener(camCapabilityListener);

		if (nonNull(peerConnection)) {
			peerConnection.close();
			peerConnection = null;
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	public void sendStreamAction(StreamAction action) {
		JanusPeerConnection peerConnection = getPeerConnection();

		if (nonNull(peerConnection)) {
			try {
				peerConnection.sendData(action.toByteArray());
			}
			catch (Exception e) {
				logDebugMessage("Send event via data channel failed");
			}
		}
	}
}
