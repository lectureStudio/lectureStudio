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

package org.lecturestudio.web.api.message;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.data.bind.CoursePresenceMessageAdapter;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.data.bind.MessengerMessageAdapter;
import org.lecturestudio.web.api.data.bind.QuizAnswerMessageAdapter;
import org.lecturestudio.web.api.data.bind.SpeechMessageAdapter;
import org.lecturestudio.web.api.net.SSLContextFactory;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.action.StreamStartAction;
import org.lecturestudio.web.api.stream.client.WebSocketClientHandler;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;

public class WebSocketTransport extends ExecutableBase implements MessageTransport {

	private final Map<Class<? extends WebMessage>, List<Consumer<WebMessage>>> listenerMap;

	private final Course course;

	private final ServiceParameters serviceParameters;

	private final WebSocketHeaderProvider headerProvider;

	private WebSocket webSocket;

	private Jsonb jsonb;


	public WebSocketTransport(ServiceParameters serviceParameters,
			WebSocketHeaderProvider headerProvider, Course course) {
		this.serviceParameters = serviceParameters;
		this.headerProvider = headerProvider;
		this.course = course;
		this.listenerMap = new HashMap<>();
	}

	@Override
	public <T extends WebMessage> void addListener(Class<T> cls,
			Consumer<T> listener) {
		List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

		if (isNull(consumerList)) {
			consumerList = new ArrayList<>();

			listenerMap.put(cls, consumerList);
		}

		consumerList.add((Consumer<WebMessage>) listener);
	}

	@Override
	public <T extends WebMessage> void removeListener(Class<T> cls,
			Consumer<T> listener) {
		List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

		if (nonNull(consumerList)) {
			consumerList.remove(listener);
		}
	}

	@Override
	public void sendMessage(WebMessage message) {

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
		HttpClient httpClient = HttpClient.newBuilder()
				.sslContext(SSLContextFactory.createSSLContext())
				.build();

		Builder webSocketBuilder = httpClient.newWebSocketBuilder();
		webSocketBuilder.subprotocols("lecture-feature-protocol");
		webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));

		headerProvider.setHeaders(webSocketBuilder);

		webSocket = webSocketBuilder
				.buildAsync(URI.create(serviceParameters.getUrl()),
						new WebSocketHandler())
				.join();

		try {
			send(new StreamStartAction(course.getId()));
		}
		catch (Exception e) {
			throw new ExecutableException("Send action failed", e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect").join();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private void send(StreamAction action) throws IOException {
		webSocket.sendBinary(ByteBuffer.wrap(action.toByteArray()), true);
	}

	private void handleMessage(WebMessage message) {
		Class<? extends WebMessage> cls = message.getClass();
		List<Consumer<WebMessage>> consumerList = listenerMap.get(cls);

		if (nonNull(consumerList)) {
			for (Consumer<WebMessage> listener : consumerList) {
				listener.accept(message);
			}
		}
	}



	private class WebSocketHandler extends WebSocketClientHandler {

		@Override
		protected void onText(WebSocket webSocket, String text) {
			StringReader reader = new StringReader(text);

			try {
				JsonObject body = Json.createReader(reader).readObject();
				reader.reset();

				String type = body.getString("_type");
				WebMessage message = createMessage(reader, type);

				handleMessage(message);
			}
			catch (NoSuchElementException e) {
				logException(e, "Non existing Janus event type received");
			}
			catch (Exception e) {
				logException(e, "Process message failed");
			}
		}

		protected <T extends WebMessage> T createMessage(StringReader reader,
				String type) throws ClassNotFoundException {
			String packageName = WebMessage.class.getPackageName();

			return jsonb.fromJson(reader,
					(Type) Class.forName(packageName + "." + type));
		}
	}
}
