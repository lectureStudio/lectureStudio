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
import java.util.*;
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
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherJoiningMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherLeftMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionTimeoutMessage;
import org.lecturestudio.web.api.janus.state.InfoState;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamSpeechPublishedAction;
import org.lecturestudio.web.api.stream.StreamContext;

/**
 * Manages communication with a Janus WebRTC server for handling real-time audio/video communication
 * in a lecture/presentation environment. This handler is responsible for: <br>
 * - Establishing and maintaining a connection with the Janus server <br>
 * - Managing participant sessions and room interactions <br>
 * - Handling publisher and subscriber operations <br>
 * - Processing and routing Janus messages <br>
 * - Managing remote speech requests and permissions <br>
 * - Maintaining participants' state <br>
 * <p>
 * The handler implements the state pattern through JanusStateHandler to manage different
 * states of the connection lifecycle.
 */
public class JanusHandler extends JanusStateHandler {

	/** Records stream events for later playback. */
	private final StreamEventRecorder eventRecorder;

	/** Executor for scheduling periodic tasks like keep-alive messages. */
	private ScheduledExecutorService executorService;

	/** Future representing the scheduled keep-alive task. */
	private ScheduledFuture<?> timeoutFuture;

	/** Registry of message handlers mapped by message class type. */
	private Map<Class<? extends JanusMessage>, Consumer<? extends JanusMessage>> messageHandlers;

	/** Maps participant IDs to their publisher objects. */
	private Map<BigInteger, JanusPublisher> participants;

	/** Maps participant contexts to their publisher objects. */
	private Map<JanusParticipantContext, JanusPublisher> speechPublishers;

	/** State handlers for managing different aspects of the Janus communication. */
	private List<JanusStateHandler> handlers;


	/**
	 * Constructs a new JanusHandler with the given dependencies.
	 *
	 * @param transmitter   The message transmitter for sending messages to the Janus server.
	 * @param streamContext The stream context containing configuration and state information.
	 * @param eventRecorder The event recorder for capturing stream events.
	 */
	public JanusHandler(JanusMessageTransmitter transmitter,
			StreamContext streamContext, StreamEventRecorder eventRecorder) {
		super(new JanusPeerConnectionFactory(streamContext), transmitter);

		this.eventRecorder = eventRecorder;
	}

	/**
	 * Starts a remote speech.
	 *
	 * @param context The participant context for the peer who initiated the speech request.
	 */
	public void startRemoteSpeech(JanusParticipantContext context) {
		if (!started()) {
			return;
		}

		Map.Entry<JanusParticipantContext, JanusPublisher> entry = getFirstPublisherEntry();

		if (nonNull(entry)) {
			JanusPublisher activePublisher = entry.getValue();
			JanusParticipantContext pContext = entry.getKey();

			if (isNull(activePublisher.getId())) {
				speechPublishers.remove(pContext);
			}
			else {
				stopRemoteSpeech(pContext);
			}
		}

		JanusPublisher speechPublisher = new JanusPublisher();
		speechPublisher.setDisplayName(context.getDisplayName());

		speechPublishers.put(context, speechPublisher);

		editRoom(3);
	}

	/**
	 * Stops a remote speech.
	 *
	 * @param context The participant context for the peer who initiated the speech request.
	 */
	public void stopRemoteSpeech(JanusParticipantContext context) {
		if (!started()) {
			return;
		}

		editRoom(3);

		JanusPublisher speechPublisher = speechPublishers.remove(context);

		if (nonNull(speechPublisher)) {
			kickParticipant(speechPublisher);

			boolean foundHandler = false;

			for (JanusStateHandler handler : handlers) {
				if (handler instanceof JanusSubscriberHandler subHandler) {
					if (subHandler.getPublisher().equals(speechPublisher)) {
						removeStateHandler(handler);
						foundHandler = true;
						break;
					}
				}
			}

			if (!foundHandler) {
				// Handle non-authorized or pending context.
				var peerStateConsumer = getStreamContext().getPeerStateConsumer();
				if (nonNull(peerStateConsumer)) {
					peerStateConsumer.accept(new PeerStateEvent(context, ExecutableState.Stopped));
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
		participants = new ConcurrentHashMap<>();
		handlers = new CopyOnWriteArrayList<>();

		setRoomId(BigInteger.valueOf(getStreamContext().getCourse().getId()));
		setOpaqueId(getStreamContext().getUserInfo().getUserId());

		registerHandler(JanusErrorMessage.class, this::handleError);
		registerHandler(JanusSessionTimeoutMessage.class, this::handleSessionTimeout);
		registerHandler(JanusRoomPublisherJoiningMessage.class, this::handlePublisherJoining);
		registerHandler(JanusRoomPublisherJoinedMessage.class, this::handlePublisherJoined);
		registerHandler(JanusRoomPublisherLeftMessage.class, this::handlePublisherLeft);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		setState(new InfoState());
	}

	@Override
	protected void stopInternal() throws ExecutableException {
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
		// Left empty for now.
	}

	/**
	 * Adds a state handler to the list of state handlers.
	 *
	 * @param handler The state handler to be added.
	 */
	private void addStateHandler(JanusStateHandler handler) {
		try {
			handler.start();

			handlers.add(handler);
		}
		catch (ExecutableException e) {
			logException(e, "Start Janus handler failed");
		}
	}

	/**
	 * Removes a state handler from the list of state handlers.
	 *
	 * @param handler The state handler to be removed.
	 */
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

	/**
	 * Registers a message handler for the specified message class.
	 *
	 * @param msgClass The message class.
	 * @param handler The message handler.
	 *
	 * @param <T> The message type.
	 */
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

	private void handlePublisherJoining(JanusRoomPublisherJoiningMessage message) {
		JanusPublisher participant = message.getPublisher();

		participants.put(participant.getId(), participant);

		// Left empty for now.
	}

	private void handlePublisherJoined(JanusRoomPublisherJoinedMessage message) {
		JanusPublisher publisher = message.getPublisher();

		// Only one speech at a time.
		var entry = getFirstPublisherEntry();

		if (nonNull(entry)) {
			entry.getValue().setId(publisher.getId());
			entry.getValue().setStreams(publisher.getStreams());

			startSubscriber(entry.getValue(), entry.getKey());
		}
		else {
			// Handle non-authorized publisher.
			kickParticipant(publisher);
		}
	}

	private void handlePublisherLeft(JanusRoomPublisherLeftMessage message) {
		JanusPublisher participant = participants.remove(message.getPublisherId());

		if (nonNull(participant)) {
			// Left empty for now.
		}
	}

	private void startPublisher() {
		JanusStateHandler pubHandler = new JanusPublisherHandler(
				peerConnectionFactory,
				transmitter, eventRecorder);
		pubHandler.setSessionId(getSessionId());
		pubHandler.setRoomId(getRoomId());
		pubHandler.setOpaqueId(getStreamContext().getUserInfo().getUserId());
		pubHandler.addJanusStateHandlerListener(new JanusStateHandlerListener() {

			@Override
			public void connected() {
				setConnected();
			}

			@Override
			public void disconnected() {
				setDisconnected();
			}

			@Override
			public void failed() {
				setFailed();
			}

			@Override
			public void error(Throwable throwable) {
				setError(new JanusHandlerException(Type.PUBLISHER, throwable));
			}
		});

		addStateHandler(pubHandler);
	}

	private void startSubscriber(final JanusPublisher publisher, final JanusParticipantContext context) {
		getStreamContext().getAudioContext().setReceiveAudio(true);
		getStreamContext().getVideoContext().setReceiveVideo(true);

		JanusSubscriberHandler subHandler = new JanusSubscriberHandler(publisher,
				peerConnectionFactory, transmitter);
		subHandler.setJanusParticipantContext(context);
		subHandler.setSessionId(getSessionId());
		subHandler.setRoomId(getRoomId());
		subHandler.setOpaqueId(getStreamContext().getUserInfo().getUserId());
		subHandler.addJanusStateHandlerListener(new JanusStateHandlerListener() {

			@Override
			public void connected() {
				// Propagate "speech published" to passive participants.
				for (JanusStateHandler handler : handlers) {
					if (handler instanceof JanusPublisherHandler pubHandler) {
						// TODO: move to http api
						pubHandler.sendStreamAction(
								new StreamSpeechPublishedAction(
										publisher.getId(),
										publisher.getDisplayName()));
						break;
					}
				}
			}

			@Override
			public void disconnected() {
				removeStateHandler(subHandler);
				editRoom(3);
			}

			@Override
			public void error(Throwable throwable) {
				setError(new JanusHandlerException(Type.SUBSCRIBER, throwable));
			}
		});

		context.audioActiveProperty().addListener((o, oldValue, newValue) ->
				muteParticipant(subHandler.getPublisher(), !newValue, MediaType.Audio));
		context.videoActiveProperty().addListener((o, oldValue, newValue) ->
				muteParticipant(subHandler.getPublisher(), !newValue, MediaType.Camera));

		addStateHandler(subHandler);
	}

	private Map.Entry<JanusParticipantContext, JanusPublisher> getFirstPublisherEntry() {
		if (speechPublishers.isEmpty()) {
			return null;
		}

		return speechPublishers.entrySet().iterator().next();
	}

	private void muteParticipant(JanusPublisher publisher, boolean mute, MediaType type) {
		if (isNull(publisher.getStreams())) {
			logErrorMessage("Cannot mute publisher (no stream info).");
			return;
		}

		JanusRoomModerateRequest request = new JanusRoomModerateRequest();
		request.setParticipantId(publisher.getId());
		request.setRoomId(getRoomId());
		request.setSecret(getRoomSecret());
		request.setMute(mute);

		for (JanusPublisherStream stream : publisher.getStreams()) {
			if (type == MediaType.Audio && Objects.equals(stream.getType(), "audio") ||
				type == MediaType.Camera && Objects.equals(stream.getType(), "video") ||
				type == MediaType.Event && Objects.equals(stream.getType(), "data")) {
				request.setMid(stream.getMid());
				break;
			}
		}

		JanusPluginDataMessage message = new JanusPluginDataMessage(
				getSessionId(), getPluginId());
		message.setTransaction(UUID.randomUUID().toString());
		message.setBody(request);

		sendMessage(message);
	}

	private void kickParticipant(JanusPublisher publisher) {
		if (isNull(publisher.getId())) {
			return;
		}

		logDebugMessage("Kick participant from room: " + getRoomId());

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
