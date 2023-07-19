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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.web.api.client.ClientFailover;
import org.lecturestudio.web.api.net.SSLContextFactory;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.StreamContext;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.action.StreamActionType;
import org.lecturestudio.web.api.stream.action.StreamMediaChangeAction;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;

/**
 * Streaming WebSocket client implementation. This client sends the current
 * document and event state to the server. Clients joining the stream obtain the
 * current state from the server.
 *
 * @author Alex Andres
 */
public class StreamWebSocketClient extends ExecutableBase {

	private final Consumer<StreamAction> actionConsumer;

	private final ExecutableStateListener recStateConsumer;

	private final ServiceParameters serviceParameters;

	private final WebSocketHeaderProvider headerProvider;

	private final StreamContext streamContext;

	private final StreamEventRecorder eventRecorder;

	private final ClientFailover clientFailover;

	private ChangeListener<Boolean> enableMicListener;

	private ChangeListener<Boolean> enableCamListener;

	private ChangeListener<Boolean> enableScreenListener;

	private WebSocket webSocket;


	public StreamWebSocketClient(ServiceParameters parameters,
			WebSocketHeaderProvider headerProvider, StreamContext streamContext,
			StreamEventRecorder eventRecorder) {
		requireNonNull(parameters);
		requireNonNull(headerProvider);
		requireNonNull(eventRecorder);

		this.serviceParameters = parameters;
		this.headerProvider = headerProvider;
		this.streamContext = streamContext;
		this.eventRecorder = eventRecorder;
		this.clientFailover = new ClientFailover();
		this.clientFailover.addExecutable(getReconnectExecutable());

		actionConsumer = this::send;
		recStateConsumer = this::onRecorderState;
	}

	public Executable getReconnectExecutable() {
		return new Reconnect();
	}

	@Override
	protected void initInternal() throws ExecutableException {
		eventRecorder.addRecordedActionConsumer(actionConsumer);
		eventRecorder.addStateListener(recStateConsumer);

		enableMicListener = (observable, oldValue, newValue) -> {
			sendMediaChangeAction(StreamActionType.STREAM_MICROPHONE_CHANGE, newValue);
		};
		enableCamListener = (observable, oldValue, newValue) -> {
			sendMediaChangeAction(StreamActionType.STREAM_CAMERA_CHANGE, newValue);
		};
		enableScreenListener = (observable, oldValue, newValue) -> {
			sendMediaChangeAction(StreamActionType.STREAM_SCREEN_SHARE_CHANGE, newValue);
		};

		streamContext.getAudioContext().sendAudioProperty().addListener(enableMicListener);
		streamContext.getVideoContext().sendVideoProperty().addListener(enableCamListener);
		streamContext.getScreenContext().sendVideoProperty().addListener(enableScreenListener);
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
		eventRecorder.removeStateListener(recStateConsumer);

		streamContext.getAudioContext().sendAudioProperty().removeListener(enableMicListener);
		streamContext.getVideoContext().sendVideoProperty().removeListener(enableCamListener);
		streamContext.getScreenContext().sendVideoProperty().removeListener(enableScreenListener);

		closeWebsocket();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}

	private void createWebsocket() {
		HttpClient httpClient = HttpClient.newBuilder()
				.sslContext(SSLContextFactory.createSSLContext())
				.build();

		Builder webSocketBuilder = httpClient.newWebSocketBuilder();
		webSocketBuilder.subprotocols("state-protocol");
		webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));

		headerProvider.setHeaders(webSocketBuilder);

		webSocket = webSocketBuilder.buildAsync(
				URI.create(serviceParameters.getUrl()), new WebSocketHandler())
				.join();
	}

	private void closeWebsocket() {
		if (nonNull(webSocket) && !webSocket.isOutputClosed()) {
			webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect").join();
			webSocket.abort();
		}
	}

	private void onRecorderState(ExecutableState oldState, ExecutableState newState) {
		// Send media stream state when course state has been initialized.
		if (newState == ExecutableState.Started) {
			boolean camEnabled = streamContext.getVideoContext().getSendVideo();
			boolean micEnabled = streamContext.getAudioContext().getSendAudio();
			boolean screenEnabled = streamContext.getScreenContext().getSendVideo();

			sendMediaChangeAction(StreamActionType.STREAM_CAMERA_CHANGE, camEnabled);
			sendMediaChangeAction(StreamActionType.STREAM_MICROPHONE_CHANGE, micEnabled);
			sendMediaChangeAction(StreamActionType.STREAM_SCREEN_SHARE_CHANGE, screenEnabled);
		}
	}

	private void send(StreamAction action) {
		try {
			webSocket.sendBinary(ByteBuffer.wrap(action.toByteArray()), true);
		}
		catch (IOException e) {
			logException(e, "Send event state failed");
		}
	}

	private void sendMediaChangeAction(StreamActionType type, boolean enabled) {
		send(new StreamMediaChangeAction(type, enabled));
	}



	private class Reconnect extends ExecutableBase {

		@Override
		protected void initInternal() {

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
			try {
				closeWebsocket();
			}
			catch (Throwable e) {
				throw new ExecutableException(e);
			}
		}

		@Override
		protected void destroyInternal() {

		}
	}



	private class WebSocketHandler extends WebSocketClientHandler {

		@Override
		protected void onText(WebSocket webSocket, String text) {

		}

		@Override
		public void onOpen(WebSocket webSocket) {
			super.onOpen(webSocket);

			// Reconnection not needed anymore, if recovery previously startet.
			if (clientFailover.started()) {
				try {
					clientFailover.stop();
				}
				catch (ExecutableException e) {
					logException(e, "Stop connection failover failed");
				}
			}
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			super.onError(webSocket, error);

			if (started()) {
				// Start recovery process.
				try {
					clientFailover.start();
				}
				catch (ExecutableException e) {
					logException(e, "Start connection failover failed");
				}
			}
		}
	}
}
