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

package org.lecturestudio.web.api.janus.client;

import static java.util.Objects.requireNonNull;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionStage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.NotSupportedException;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.janus.JanusHandler;
import org.lecturestudio.web.api.janus.JanusMessageTransmitter;
import org.lecturestudio.web.api.janus.JanusParticipantContext;
import org.lecturestudio.web.api.janus.JanusStateHandlerListener;
import org.lecturestudio.web.api.janus.json.JanusMessageFactory;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.net.SSLContextFactory;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.StreamContext;

/**
 * Janus WebSocket signaling client implementation. This client sends, receives,
 * decodes and encodes Janus JSON-formatted signaling messages.
 *
 * @author Alex Andres
 */
public class JanusWebSocketClient extends ExecutableBase implements JanusMessageTransmitter {

	private final ServiceParameters serviceParameters;

	private final StreamContext streamContext;

	private final StreamEventRecorder eventRecorder;

	private final List<JanusMessage> bufferedMessages = new ArrayList<>();

	private final List<JanusStateHandlerListener> stateListeners = new ArrayList<>();

	private WebSocket webSocket;

	private Jsonb jsonb;

	private JanusHandler handler;


	public JanusWebSocketClient(ServiceParameters parameters,
			StreamContext streamContext, StreamEventRecorder eventRecorder) {
		this.serviceParameters = parameters;
		this.streamContext = streamContext;
		this.eventRecorder = eventRecorder;
	}

	public void addJanusStateHandlerListener(JanusStateHandlerListener listener) {
		stateListeners.add(listener);
	}

	public void startRemoteSpeech(JanusParticipantContext context) {
		if (!started()) {
			return;
		}

		handler.startRemoteSpeech(context);
	}

	public void stopRemoteSpeech(JanusParticipantContext context) {
		if (!started()) {
			return;
		}

		handler.stopRemoteSpeech(context);
	}

	@Override
	public void sendMessage(JanusMessage message) {
		if (getState() != ExecutableState.Starting && getState() != ExecutableState.Started) {
			bufferedMessages.add(message);
			return;
		}
		if (webSocket.isOutputClosed()) {
			logDebugMessage("Could not send message. Websocket is closed.");
			return;
		}

		String messageTxt = jsonb.toJson(message);

		logTraceMessage("WebSocket ->: {0}", messageTxt);

		webSocket.sendText(messageTxt, true)
				.exceptionally(throwable -> {
					logException(throwable, "Send Janus message failed");
					return null;
				});
	}

	@Override
	protected void initInternal() throws ExecutableException {
		requireNonNull(streamContext.getCourse());

		jsonb = JsonbBuilder.create(JsonConfigProvider.createConfig());
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			HttpClient httpClient = HttpClient.newBuilder()
					.sslContext(SSLContextFactory.createSSLContext())
					.build();

			Builder webSocketBuilder = httpClient.newWebSocketBuilder();
			webSocketBuilder.subprotocols("janus-protocol");
			webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));

			webSocket = webSocketBuilder.buildAsync(
					URI.create(serviceParameters.getUrl()),
					new WebSocketListener()).join();

			handler = new JanusHandler(this, streamContext, eventRecorder);
			handler.addJanusStateHandlerListeners(stateListeners);
			handler.init();
			handler.start();

			if (!bufferedMessages.isEmpty()) {
				bufferedMessages.forEach(this::sendMessage);
				bufferedMessages.clear();
			}
		}
		catch (Throwable e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (!handler.stopped()) {
			handler.stop();
		}

		if (!webSocket.isOutputClosed()) {
			webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect").join();
			webSocket.abort();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		handler.destroy();
	}



	private class WebSocketListener implements Listener {

		/** Accumulating message buffer. */
		private StringBuffer buffer = new StringBuffer();


		@Override
		public void onError(WebSocket socket, Throwable error) {
			logException(error, "Janus websocket error");
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			logTraceMessage("WebSocket <-: {0}", data);

			webSocket.request(1);

			buffer.append(data);

			if (last) {
				StringReader reader = new StringReader(buffer.toString());

				try {
					JsonObject body = Json.createReader(reader).readObject();

					JanusMessageType type = JanusMessageType.fromString(body.getString("janus"));
					JanusMessage message = JanusMessageFactory.createMessage(jsonb, body, type);

					handler.handleMessage(message);
				}
				catch (NotSupportedException e) {
					logDebugMessage("Received unsupported message");
				}
				catch (NoSuchElementException e) {
					logException(e, "Non existing Janus event type received");
				}
				catch (Exception e) {
					logException(e, "Process Janus message failed");
				}

				buffer = new StringBuffer();
			}

			return null;
		}
	}
}
