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

package org.lecturestudio.web.api.stream.client;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.web.api.data.bind.CoursePresenceMessageAdapter;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.data.bind.SpeechMessageAdapter;
import org.lecturestudio.web.api.message.CoursePresenceMessage;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.net.SSLContextFactory;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;

/**
 * Streaming WebSocket client implementation. This client sends the current
 * document and event state to the server. Clients joining the stream obtain the
 * current state from the server.
 *
 * @author Alex Andres
 */
public class StreamWebSocketClient extends ExecutableBase {

	private final Consumer<StreamAction> actionConsumer = this::send;

	private final EventBus eventBus;

	private final ServiceParameters serviceParameters;

	private final WebSocketHeaderProvider headerProvider;

	private final StreamEventRecorder eventRecorder;

	private final Course course;

	private final Jsonb jsonb;

	private WebSocket webSocket;


	public StreamWebSocketClient(EventBus eventBus,
			ServiceParameters parameters,
			WebSocketHeaderProvider headerProvider,
			StreamEventRecorder eventRecorder,
			Course course) {
		requireNonNull(eventBus);
		requireNonNull(parameters);
		requireNonNull(headerProvider);
		requireNonNull(eventRecorder);
		requireNonNull(course);

		this.eventBus = eventBus;
		this.serviceParameters = parameters;
		this.headerProvider = headerProvider;
		this.eventRecorder = eventRecorder;
		this.course = course;

		JsonbConfig config = JsonConfigProvider.createConfig().withAdapters(
				new SpeechMessageAdapter(),
				new CoursePresenceMessageAdapter());

		this.jsonb = JsonbBuilder.create(config);
	}

	public Executable getReconnectExecutable() {
		return new Reconnect();
	}

	@Override
	protected void initInternal() throws ExecutableException {
		eventRecorder.addRecordedActionConsumer(actionConsumer);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			createWebsocket();
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		eventRecorder.removeRecordedActionConsumer(actionConsumer);

		if (!webSocket.isOutputClosed()) {
			webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect").join();
			webSocket.abort();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private void createWebsocket() {
		HttpClient httpClient = HttpClient.newBuilder()
				.sslContext(SSLContextFactory.createSSLContext()).build();

		Builder webSocketBuilder = httpClient.newWebSocketBuilder();
		webSocketBuilder.subprotocols("state-protocol");
		webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));

		headerProvider.setHeaders(webSocketBuilder);

		webSocket = webSocketBuilder.buildAsync(
				URI.create(serviceParameters.getUrl()), new WebSocketHandler())
				.join();
	}

	private void send(StreamAction action) {
		try {
			webSocket.sendBinary(ByteBuffer.wrap(action.toByteArray()), true);
		}
		catch (IOException e) {
			logException(e, "Send event state failed");
		}
	}



	private class Reconnect extends ExecutableBase {

		@Override
		protected void initInternal() throws ExecutableException {

		}

		@Override
		protected void startInternal() throws ExecutableException {
			try {
				createWebsocket();
			}
			catch (Throwable e) {
				throw new ExecutableException(e);
			}
		}

		@Override
		protected void stopInternal() throws ExecutableException {

		}

		@Override
		protected void destroyInternal() throws ExecutableException {

		}
	}



	private class WebSocketHandler extends WebSocketClientHandler {

		@Override
		protected void onText(WebSocket webSocket, String text) {
			StringReader reader = new StringReader(text);

			try {
				Object message = null;

				JsonReader jsonReader = Json.createReader(reader);
				JsonObject jsonObject = jsonReader.readObject();
				String typeStr = jsonObject.getString("type");

				if (typeStr.startsWith("Speech")) {
					message = jsonb.fromJson(text, SpeechBaseMessage.class);
				}
				else if (typeStr.startsWith("CourseParticipant")) {
					message = jsonb.fromJson(text, CoursePresenceMessage.class);
				}

				if (nonNull(message)) {
					eventBus.post(message);
				}
			}
			catch (Exception e) {
				logException(e, "Process message failed");
			}
		}
	}
}
