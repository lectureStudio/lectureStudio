package org.lecturestudio.web.api.message;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
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
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WebSocketStompTransport extends ExecutableBase implements MessageTransport {

	private final Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap;

	private final Course course;

	private final ServiceParameters serviceParameters;

	private final WebSocketStompHeaderProvider headerProvider;

	private WebSocketStompClient stompClient;

	private Jsonb jsonb;

	private DefaultStompSession session;

	private String userId;


	public WebSocketStompTransport(ServiceParameters serviceParameters,
			WebSocketStompHeaderProvider headerProvider, Course course) {
		this.serviceParameters = serviceParameters;
		this.headerProvider = headerProvider;
		this.course = course;
		this.listenerMap = new HashMap<>();
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

			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {

						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(X509Certificate[] certs,
								String authType) {
						}

						public void checkServerTrusted(X509Certificate[] certs,
								String authType) {
						}
					}
			};

			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(
						sc.getSocketFactory());

				Map<String, Object> properties = new HashMap<>();
				properties.put("org.apache.tomcat.websocket.SSL_CONTEXT", sc);
				standardClient.setUserProperties(properties);
			}
			catch (Throwable e) {
				throw new ExecutableException(e);
			}

			stompClient = new WebSocketStompClient(standardClient);
			stompClient.setMessageConverter(new MappingJackson2MessageConverter());

			connect();
		}
		else if (! this.stompClient.isRunning()) {
			this.stompClient.start();
		}
	}

	public void connect() {
		WebSocketHttpHeaders headers = headerProvider.getHeaders();

		StompHeaders stompHeaders = new StompHeaders();
		stompHeaders.add("courseId", course.getId().toString());

		MessengerStompSessionHandler sessionHandler = new MessengerStompSessionHandler(course, jsonb, listenerMap);

		ListenableFuture<StompSession> listenableSession = stompClient.connect(serviceParameters.getUrl(), headers, stompHeaders, sessionHandler);

		try {
			this.session = (DefaultStompSession) listenableSession.get();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		if (nonNull(this.session)) {
			this.session.disconnect();
			this.session = null;
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		this.disconnect();

		if (this.stompClient.isRunning()) {
			this.stompClient.stop();
			this.stompClient = null;
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		// No-op
	}

	@Override
	public void sendMessage(String recipient, Message message) {
		if (super.started() && nonNull(session)) {
			StompHeaders headers = new StompHeaders();
			headers.add("courseId", this.course.getId().toString());
			headers.add("recipient", recipient);
			headers.setDestination("/app/message/" + this.course.getId());

			headerProvider.addHeaders(headers);

			session.send(headers, message);
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
				StompHeaders stompHeaders) {
			userId = stompHeaders.getFirst("user-name");

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
				Throwable throwable) {
			throwable.printStackTrace();
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

				System.out.println(payloadType + " " + payloadJson);

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
