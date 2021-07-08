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
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.web.api.janus.message.JanusErrorMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomListMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherJoinedMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherLeftMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherUnpublishedMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomRequest;
import org.lecturestudio.web.api.janus.message.JanusRoomRequestType;
import org.lecturestudio.web.api.janus.message.JanusSessionMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionTimeoutMessage;
import org.lecturestudio.web.api.janus.state.DestroyRoomState;
import org.lecturestudio.web.api.janus.state.InfoState;
import org.lecturestudio.web.api.janus.state.JanusState;
import org.lecturestudio.web.api.stream.config.WebRtcConfiguration;

public class JanusHandler extends ExecutableBase {

	private final JanusMessageTransmitter transmitter;

	private final WebRtcConfiguration webRtcConfig;

	private final LectureRecorder eventRecorder;

	private ScheduledExecutorService executorService;

	private Map<Class<? extends JanusMessage>, Consumer<? extends JanusMessage>> handlerMap;

	private JanusPeerConnection peerConnection;

	private JanusState state;

	private JanusInfo info;

	private BigInteger sessionId;

	private BigInteger pluginId;

	private BigInteger roomId;

	private String roomSecret;


	public JanusHandler(JanusMessageTransmitter transmitter,
			WebRtcConfiguration webRtcConfig, LectureRecorder lectureRecorder) {
		this.transmitter = transmitter;
		this.webRtcConfig = webRtcConfig;
		this.eventRecorder = lectureRecorder;
	}

	public void createPeerConnection() {
		peerConnection = new JanusPeerConnection(webRtcConfig,
				Executors.newSingleThreadExecutor());
	}

	public JanusPeerConnection getPeerConnection() {
		return peerConnection;
	}

	public WebRtcConfiguration getWebRtcConfig() {
		return webRtcConfig;
	}

	public void listRooms() {
		JanusRoomRequest request = new JanusRoomRequest();
		request.setRequestType(JanusRoomRequestType.LIST);

		var requestMessage = new JanusPluginDataMessage(sessionId, pluginId);
		requestMessage.setTransaction(UUID.randomUUID().toString());
		requestMessage.setBody(request);

		sendMessage(requestMessage);
	}

	public void sendMessage(JanusMessage message) {
		transmitter.sendMessage(message);
	}

	public <T extends JanusMessage> void handleMessage(T message) throws Exception {
		if (message.getEventType() == JanusMessageType.ACK) {
			// Do not process ack events.
			return;
		}

		if (handlerMap.containsKey(message.getClass())) {
			processMessage(message);
		}
		else {
			requireNonNull(state);

			state.handleMessage(this, message);
		}
	}

	public void setState(JanusState state) {
		requireNonNull(state);

		this.state = state;

		state.initialize(this);
	}

	public void setInfo(JanusInfo info) {
		requireNonNull(info);

		this.info = info;
	}

	public BigInteger getSessionId() {
		return sessionId;
	}

	public void setSessionId(BigInteger id) {
		requireNonNull(info);
		requireNonNull(id);

		sessionId = id;

		// Trigger periodic keep-alive messages with half the session timeout.
		long period = info.getSessionTimeout() / 2;
		executorService.scheduleAtFixedRate(this::sendKeepAliveMessage, period,
				period, TimeUnit.SECONDS);
	}

	public BigInteger getPluginId() {
		return pluginId;
	}

	public void setPluginId(BigInteger id) {
		requireNonNull(id);

		pluginId = id;
	}

	public BigInteger getRoomId() {
		return roomId;
	}

	public void setRoomId(BigInteger id) {
		requireNonNull(id);

		roomId = id;
	}

	public String getRoomSecret() {
		return roomSecret;
	}

	public void setRoomSecret(String secret) {
		requireNonNull(secret);

		roomSecret = secret;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		executorService = Executors.newSingleThreadScheduledExecutor();
		handlerMap = new HashMap<>();
		eventRecorder.addRecordedActionConsumer(action -> {
			if (nonNull(peerConnection)) {
				try {
					peerConnection.sendData(action.toByteArray());
				}
				catch (Exception e) {
					logException(e, "Send event via data channel failed");
				}
			}
		});

		registerHandler(JanusErrorMessage.class, this::handleError);
		registerHandler(JanusSessionTimeoutMessage.class, this::handleSessionTimeout);
		registerHandler(JanusRoomListMessage.class, this::handleRoomList);
		registerHandler(JanusRoomPublisherJoinedMessage.class, this::handlePublisherJoined);
		registerHandler(JanusRoomPublisherUnpublishedMessage.class, this::handlePublisherUnpublished);
		registerHandler(JanusRoomPublisherLeftMessage.class, this::handlePublisherLeft);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		setState(new InfoState());
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		setState(new DestroyRoomState());

		executorService.shutdown();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private <T extends JanusMessage> void registerHandler(Class<T> msgClass, Consumer<T> handler) {
		handlerMap.put(msgClass, handler);
	}

	@SuppressWarnings("unchecked")
	private <T extends JanusMessage> void processMessage(T message) {
		Consumer<T> handler = (Consumer<T>) handlerMap.get(message.getClass());

		handler.accept(message);
	}

	private void handleError(JanusErrorMessage message) {
		logErrorMessage("Janus error: {0} (error code: {1})",
				message.getReason(), message.getCode());
	}

	private void handleSessionTimeout(JanusSessionTimeoutMessage message) {

	}

	private void handleRoomList(JanusRoomListMessage message) {
		System.out.println(message.getRooms());
	}

	private void handlePublisherJoined(JanusRoomPublisherJoinedMessage message) {
		System.out.println(message.getPublisher());
	}

	private void handlePublisherUnpublished(JanusRoomPublisherUnpublishedMessage message) {

	}

	private void handlePublisherLeft(JanusRoomPublisherLeftMessage message) {

	}

	private void sendKeepAliveMessage() {
		JanusSessionMessage message = new JanusSessionMessage(sessionId);
		message.setEventType(JanusMessageType.KEEP_ALIVE);
		message.setTransaction(UUID.randomUUID().toString());

		sendMessage(message);
	}
}
