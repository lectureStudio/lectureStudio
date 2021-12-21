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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.web.api.client.ClientFailover;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.janus.JanusHandlerException.Type;
import org.lecturestudio.web.api.janus.message.JanusEditRoomMessage;
import org.lecturestudio.web.api.janus.message.JanusErrorMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomKickRequest;
import org.lecturestudio.web.api.janus.message.JanusRoomModerateRequest;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherJoinedMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionTimeoutMessage;
import org.lecturestudio.web.api.janus.state.DestroyRoomState;
import org.lecturestudio.web.api.janus.state.InfoState;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamSpeechPublishedAction;
import org.lecturestudio.web.api.stream.StreamContext;

public class JanusHandler extends JanusStateHandler {

	private final StreamEventRecorder eventRecorder;

	private final ClientFailover clientFailover;

	private ScheduledExecutorService executorService;

	private ScheduledFuture<?> timeoutFuture;

	private Map<Class<? extends JanusMessage>, Consumer<? extends JanusMessage>> messageHandlers;

	private Map<Long, JanusPublisher> speechPublishers;

	private List<JanusStateHandler> handlers;


	public JanusHandler(JanusMessageTransmitter transmitter,
			StreamContext streamContext,
			StreamEventRecorder eventRecorder, ClientFailover clientFailover) {
		super(new JanusPeerConnectionFactory(streamContext), transmitter);

		this.eventRecorder = eventRecorder;
		this.clientFailover = clientFailover;
	}

	public void startRemoteSpeech(long requestId, String userName) {
		if (!started()) {
			return;
		}

		JanusPublisher activePublisher = getFirstPublisher();

		if (nonNull(activePublisher)) {
			if (isNull(activePublisher.getId())) {
				speechPublishers.values()
						.removeIf(value -> value.equals(activePublisher));
			}
			else {
				stopRemoteSpeech(activePublisher.getId());
			}
		}

		JanusPublisher speechPublisher = new JanusPublisher();
		speechPublisher.setDisplayName(userName);

		speechPublishers.put(requestId, speechPublisher);

		editRoom(2);
	}

	public void stopRemoteSpeech(BigInteger peerId) {
		if (!started()) {
			return;
		}

		editRoom(1);

		var entry = speechPublishers.entrySet().stream()
				.filter(e -> e.getValue().getId().equals(peerId))
				.findFirst()
				.orElse(null);

		if (isNull(entry)) {
			return;
		}

		JanusPublisher speechPublisher = speechPublishers.remove(entry.getKey());

		if (nonNull(speechPublisher)) {
			kickParticipant(speechPublisher);

			for (JanusStateHandler handler : handlers) {
				if (handler instanceof JanusSubscriberHandler) {
					JanusSubscriberHandler subHandler = (JanusSubscriberHandler) handler;

					if (subHandler.getPublisher().equals(speechPublisher)) {
						removeStateHandler(handler);
						break;
					}
				}
			}
		}
	}

	@Override
	public void handleMessage(JanusMessage message) throws Exception {
		if (message.getEventType() == JanusMessageType.ACK) {
			// Do not process ack events.
			return;
		}

		if (messageHandlers.containsKey(message.getClass())) {
			processMessage(message);
		}
		else {
			for (JanusStateHandler handler : handlers) {
				try {
					handler.handleMessage(message);
				}
				catch (Exception e) {
					logException(e, "Handle Janus message failed");
				}
			}

			try {
				super.handleMessage(message);
			}
			catch (Exception e) {
				logException(e, "Handle Janus message failed");
			}
		}
	}

	@Override
	public void setSessionId(BigInteger id) {
		super.setSessionId(id);

		requireNonNull(info);

		// Trigger periodic keep-alive messages with half the session timeout.
		long period = info.getSessionTimeout() / 2;

		timeoutFuture = executorService.scheduleAtFixedRate(this::sendKeepAliveMessage, period,
				period, TimeUnit.SECONDS);
	}

	@Override
	public void setPluginId(BigInteger id) {
		super.setPluginId(id);

		// Start publishing.
		startPublisher();
	}

	@Override
	protected void initInternal() throws ExecutableException {
		executorService = Executors.newSingleThreadScheduledExecutor();
		messageHandlers = new HashMap<>();
		speechPublishers = new ConcurrentHashMap<>();
		handlers = new CopyOnWriteArrayList<>();

		getStreamContext().getAudioContext().receiveAudioProperty()
				.addListener((observable, oldValue, newValue) -> {
					JanusPublisher speechPublisher = getFirstPublisher();

					if (nonNull(speechPublisher)) {
						muteParticipant(speechPublisher, !newValue, MediaType.Audio);
					}
				});
		getStreamContext().getVideoContext().receiveVideoProperty()
				.addListener((observable, oldValue, newValue) -> {
					JanusPublisher speechPublisher = getFirstPublisher();

					if (nonNull(speechPublisher)) {
						muteParticipant(speechPublisher, !newValue, MediaType.Camera);
					}
				});

		setRoomId(BigInteger.valueOf(getStreamContext().getCourse().getId()));

		registerHandler(JanusErrorMessage.class, this::handleError);
		registerHandler(JanusSessionTimeoutMessage.class, this::handleSessionTimeout);
		registerHandler(JanusRoomPublisherJoinedMessage.class, this::handlePublisherJoined);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		setState(new InfoState());
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		setState(new DestroyRoomState());

		for (JanusStateHandler handler : handlers) {
			handler.destroy();
		}

		handlers.clear();

		peerConnectionFactory.dispose();

		if (nonNull(timeoutFuture) && !timeoutFuture.isCancelled()) {
			timeoutFuture.cancel(true);
		}
		executorService.shutdownNow();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private void addStateHandler(JanusStateHandler handler) {
		try {
			handler.start();

			handlers.add(handler);
		}
		catch (ExecutableException e) {
			logException(e, "Start Janus handler failed");
		}
	}

	private void removeStateHandler(JanusStateHandler handler) {
		handlers.remove(handler);

		if (handler.destroyed()) {
			return;
		}

		try {
			handler.destroy();
		}
		catch (ExecutableException e) {
			logException(e, "Stop Janus handler failed");
		}
	}

	private <T extends JanusMessage> void registerHandler(Class<T> msgClass,
			Consumer<T> handler) {
		messageHandlers.put(msgClass, handler);
	}

	@SuppressWarnings("unchecked")
	private <T extends JanusMessage> void processMessage(T message) {
		Consumer<T> handler = (Consumer<T>) messageHandlers.get(message.getClass());

		if (nonNull(handler)) {
			handler.accept(message);
		}
	}

	private void handleError(JanusErrorMessage message) {
		logErrorMessage("Janus error: {0} (error code: {1})",
				message.getReason(), message.getCode());
	}

	private void handleSessionTimeout(JanusSessionTimeoutMessage message) {
		logErrorMessage("Janus session '%s' timed out", message.getId());
	}

	private void handlePublisherJoined(JanusRoomPublisherJoinedMessage message) {
		JanusPublisher publisher = message.getPublisher();

		// Only one speech at a time.
		JanusPublisher speechPublisher = getFirstPublisher();
		speechPublisher.setId(publisher.getId());

		startSubscriber(speechPublisher);
	}

	private void startPublisher() {
		JanusStateHandler pubHandler = new JanusPublisherHandler(
				peerConnectionFactory,
				transmitter, eventRecorder);
		pubHandler.setSessionId(getSessionId());
		pubHandler.setRoomId(getRoomId());
		pubHandler.addJanusStateHandlerListener(new JanusStateHandlerListener() {

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

				setConnected();
			}

			@Override
			public void disconnected() {
				if (started()) {
					// Upon unexpected disruption start recover process.
					try {
						clientFailover.start();
					}
					catch (ExecutableException e) {
						logException(e, "Start connection failover failed");
					}
				}

				setDisconnected();
			}

			@Override
			public void error(Throwable throwable) {
				setError(new JanusHandlerException(Type.PUBLISHER, throwable));
			}
		});

		addStateHandler(pubHandler);
	}

	private void startSubscriber(JanusPublisher publisher) {
		getStreamContext().getAudioContext().setReceiveAudio(true);
		getStreamContext().getVideoContext().setReceiveVideo(true);

		JanusStateHandler subHandler = new JanusSubscriberHandler(publisher,
				peerConnectionFactory, transmitter);
		subHandler.setSessionId(getSessionId());
		subHandler.setRoomId(getRoomId());
		subHandler.addJanusStateHandlerListener(new JanusStateHandlerListener() {

			@Override
			public void connected() {
				setPeerState(publisher, ExecutableState.Started);

				// Propagate "speech published" to passive participants.
				for (JanusStateHandler handler : handlers) {
					if (handler instanceof JanusPublisherHandler) {
						JanusPublisherHandler pubHandler = (JanusPublisherHandler) handler;
						pubHandler.sendStreamAction(new StreamSpeechPublishedAction(publisher.getId()));
						break;
					}
				}
			}

			@Override
			public void disconnected() {
				setPeerState(publisher, ExecutableState.Stopped);

				removeStateHandler(subHandler);
				editRoom(1);
				kickParticipant(publisher);
			}

			@Override
			public void error(Throwable throwable) {
				setError(new JanusHandlerException(Type.SUBSCRIBER, throwable));
			}
		});

		addStateHandler(subHandler);
	}

	private void setPeerState(JanusPublisher publisher, ExecutableState state) {
		var peerStateConsumer = getStreamContext().getPeerStateConsumer();

		if (nonNull(peerStateConsumer)) {
			peerStateConsumer.accept(new PeerStateEvent(publisher.getId(),
					publisher.getDisplayName(), state));
		}
	}

	private JanusPublisher getFirstPublisher() {
		if (speechPublishers.isEmpty()) {
			return null;
		}

		return speechPublishers.entrySet().iterator().next().getValue();
	}

	private void muteParticipant(JanusPublisher publisher, boolean mute, MediaType... types) {
		JanusRoomModerateRequest request = new JanusRoomModerateRequest();
		request.setParticipantId(publisher.getId());
		request.setRoomId(getRoomId());
		request.setSecret(getRoomSecret());

		for (MediaType type : types) {
			if (type == MediaType.Audio) {
				request.setMuteAudio(mute);
			}
			else if (type == MediaType.Camera) {
				request.setMuteVideo(mute);
			}
			if (type == MediaType.Event) {
				request.setMuteData(mute);
			}
		}

		JanusPluginDataMessage message = new JanusPluginDataMessage(
				getSessionId(), getPluginId());
		message.setTransaction(UUID.randomUUID().toString());
		message.setBody(request);

		sendMessage(message);
	}

	private void kickParticipant(JanusPublisher publisher) {
		System.out.println("kick participant: " + getRoomId());

		JanusRoomKickRequest request = new JanusRoomKickRequest();
		request.setParticipantId(publisher.getId());
		request.setRoomId(getRoomId());
		request.setSecret(getRoomSecret());

		JanusPluginDataMessage message = new JanusPluginDataMessage(
				getSessionId(), getPluginId());
		message.setTransaction(UUID.randomUUID().toString());
		message.setBody(request);

		sendMessage(message);
	}

	private void sendKeepAliveMessage() {
		JanusSessionMessage message = new JanusSessionMessage(sessionId);
		message.setEventType(JanusMessageType.KEEP_ALIVE);
		message.setTransaction(UUID.randomUUID().toString());

		sendMessage(message);
	}

	private void editRoom(int publisherCount) {
		JanusEditRoomMessage request = new JanusEditRoomMessage();
		request.setRoomId(getRoomId());
		request.setPublishers(publisherCount);
		//request.setSecret(handler.getRoomSecret());

		var requestMessage = new JanusPluginDataMessage(getSessionId(),
				getPluginId());
		requestMessage.setTransaction(UUID.randomUUID().toString());
		requestMessage.setBody(request);

		sendMessage(requestMessage);
	}
}
