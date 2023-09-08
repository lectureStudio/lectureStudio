package org.lecturestudio.web.api.message;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.client.ClientFailover;
import org.lecturestudio.web.api.data.bind.*;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.websocket.WebSocketStompHeaderProvider;

import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.DefaultStompSession;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WebSocketStompTransport extends ExecutableBase implements MessageTransport {

	private static final int HEARTBEAT_MS = 1000;

	private final Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap;

	private final Course course;

	private final ClientFailover clientFailover;

	private final ServiceParameters serviceParameters;

	private final WebSocketStompHeaderProvider headerProvider;

	private WebSocketStompClient stompClient;

	private Jsonb jsonb;

	private DefaultStompSession session;


	public WebSocketStompTransport(ServiceParameters serviceParameters,
			WebSocketStompHeaderProvider headerProvider, Course course) {
		this.serviceParameters = serviceParameters;
		this.headerProvider = headerProvider;
		this.course = course;
		this.listenerMap = new HashMap<>();
		this.clientFailover = new ClientFailover();
		this.clientFailover.addExecutable(new Reconnect());
	}

	@Override
	public <T extends WebMessage> void addListener(Class<T> cls, Consumer<T> listener) {
		List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

		if (isNull(consumerList)) {
			consumerList = new ArrayList<>();

			listenerMap.put(cls, consumerList);
		}

		consumerList.add((Consumer<WebMessage>) listener);
	}

	@Override
	public <T extends WebMessage> void removeListener(Class<T> cls, Consumer<T> listener) {
		List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

		if (nonNull(consumerList)) {
			consumerList.remove(listener);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		JsonbConfig jsonbConfig = JsonConfigProvider.createConfig();
		jsonbConfig.withAdapters(
				new CoursePresenceMessageAdapter(),
				new MessengerMessageAdapter(),
				new QuizAnswerMessageAdapter(),
				new SpeechMessageAdapter()
		);

		jsonb = JsonbBuilder.create(jsonbConfig);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (isNull(this.stompClient)) {
			StandardWebSocketClient standardClient = new StandardWebSocketClient();

			stompClient = new WebSocketStompClient(standardClient);
			stompClient.setMessageConverter(new MappingJackson2MessageConverter());
			stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
			stompClient.setDefaultHeartbeat(new long[] { HEARTBEAT_MS, HEARTBEAT_MS });

			connect();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			this.disconnect();

			if (this.stompClient.isRunning()) {
				this.stompClient.stop();
			}

			this.stompClient = null;
		}
		catch (Exception e) {
			throw new ExecutableException("Stop STOMP client failed", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		// No-op
	}

	@Override
	public void sendMessage(String recipient, Message message) {
		if (started() && nonNull(session)) {
			StompHeaders headers = new StompHeaders();
			headers.add("course-id", course.getId().toString());
			headers.add("recipient", recipient);
			headers.setDestination("/app/message/" + this.course.getId());

			headerProvider.addHeaders(headers);

			session.send(headers, message);
		}
	}

	private void connect() throws ExecutableException {
		WebSocketHttpHeaders headers = headerProvider.getHeaders();

		StompHeaders stompHeaders = new StompHeaders();
		stompHeaders.add("course-id", course.getId().toString());
		stompHeaders.setHeartbeat(new long[] { HEARTBEAT_MS, HEARTBEAT_MS });

		MessengerStompSessionHandler sessionHandler = new MessengerStompSessionHandler(course, jsonb, listenerMap);

		var listenableSession = stompClient.connectAsync(serviceParameters.getUrl(), headers, stompHeaders, sessionHandler);

		try {
			this.session = (DefaultStompSession) listenableSession.get();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	private void disconnect() {
		if (nonNull(this.session) && this.session.isConnected()) {
			this.session.disconnect();
			this.session = null;
		}
	}



	private class Reconnect extends ExecutableBase {

		@Override
		protected void initInternal() {

		}

		@Override
		protected void startInternal() throws ExecutableException {
			if (!WebSocketStompTransport.this.started()) {
				WebSocketStompTransport.this.start();
			}
		}

		@Override
		protected void stopInternal() throws ExecutableException {
			if (!WebSocketStompTransport.this.stopped()) {
				WebSocketStompTransport.this.stop();
			}
		}

		@Override
		protected void destroyInternal() {

		}
	}



	private class MessengerStompSessionHandler implements StompSessionHandler {

		private final Course course;

		private final Jsonb jsonb;

		private final Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap;


		public MessengerStompSessionHandler(Course course, Jsonb jsonb,
				Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap) {
			this.course = course;
			this.jsonb = jsonb;
			this.listenerMap = listenerMap;
		}

		private void handleMessage(WebMessage message) {
			Class<? extends WebMessage> cls = message.getClass();

			for (var keyClass : listenerMap.keySet()) {
				if (keyClass.isAssignableFrom(cls)) {
					var consumerList = listenerMap.get(keyClass);

					if (nonNull(consumerList)) {
						for (Consumer<WebMessage> listener : consumerList) {
							listener.accept(message);
						}
					}
				}
			}
		}

		@Override
		public void afterConnected(@NonNull StompSession stompSession,
				@NonNull StompHeaders stompHeaders) {
			// Reconnection not needed anymore, if recovery previously startet.
			if (clientFailover.started()) {
				try {
					clientFailover.stop();
				}
				catch (ExecutableException e) {
					logException(e, "Stop connection failover failed");
				}
			}

			subscribe(stompSession, "/topic/course/event/{id}/presence");
			subscribe(stompSession, "/topic/course/{id}/chat");
			subscribe(stompSession, "/user/queue/course/{id}/chat");
			subscribe(stompSession, "/user/queue/course/{id}/presence");
			subscribe(stompSession, "/user/queue/course/{id}/speech");
			subscribe(stompSession, "/user/queue/course/{id}/quiz");
		}

		@Override
		public void handleException(@NonNull StompSession stompSession,
				StompCommand stompCommand, @NonNull StompHeaders stompHeaders,
				@NonNull byte[] bytes, Throwable throwable) {
			throwable.printStackTrace();
		}

		@Override
		public void handleTransportError(@NonNull StompSession stompSession,
				@NonNull Throwable throwable) {
			if (started()) {
				// Stop transport to start from clean state.
				try {
					stop();
				}
				catch (ExecutableException e) {
					logException(e, "Stop STOMP client failed");
				}

				// Start recovery process.
				try {
					clientFailover.start();
				}
				catch (ExecutableException e) {
					logException(e, "Start connection failover failed");
				}
			}
		}

		@Override
		@NonNull
		public Type getPayloadType(@NonNull StompHeaders stompHeaders) {
			return Object.class;
		}

		@Override
		public void handleFrame(@NonNull StompHeaders stompHeaders, Object o) {
			if (nonNull(o)) {
				String payloadType = stompHeaders.get("payloadType").get(0);
				String payloadJson = new String((byte[]) o, StandardCharsets.UTF_8);

				try {
					WebMessage message = createMessage(payloadJson, payloadType);

					handleMessage(message);
				}
				catch (ClassNotFoundException e) {
					logException(e, "Non existing message type received");
				}
			}
		}

		private void subscribe(StompSession session, String topic) {
			StompHeaders headers = new StompHeaders();
			headers.setDestination(topic.replaceFirst("\\{id\\}", this.course.getId().toString()));
			headers.add("courseId", this.course.getId().toString());

			session.subscribe(headers, this);
		}

		private <T extends WebMessage> T createMessage(String payload,
				String payloadType) throws ClassNotFoundException {
			String packageName = WebMessage.class.getPackageName();

			return jsonb.fromJson(payload,
					(Type) Class.forName(packageName + "." + payloadType));
		}
	}
}
