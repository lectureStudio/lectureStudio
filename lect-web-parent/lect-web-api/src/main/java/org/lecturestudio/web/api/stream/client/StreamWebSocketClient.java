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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.data.bind.CourseParticipantMessageAdapter;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.data.bind.SpeechMessageAdapter;
import org.lecturestudio.web.api.message.CourseParticipantMessage;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.net.OwnTrustManager;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCreateAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentSelectAction;
import org.lecturestudio.web.api.stream.action.StreamInitAction;
import org.lecturestudio.web.api.stream.action.StreamPageSelectedAction;
import org.lecturestudio.web.api.stream.action.StreamStartAction;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.service.StreamProviderService;
import org.lecturestudio.web.api.websocket.WebSocketHeaderProvider;

/**
 * Streaming WebSocket client implementation. This client sends the current
 * document and event state to the server. Clients joining the stream obtain the
 * current state from the server.
 *
 * @author Alex Andres
 */
public class StreamWebSocketClient extends ExecutableBase {

	private final EventBus eventBus;

	private final ServiceParameters serviceParameters;

	private final WebSocketHeaderProvider headerProvider;

	private final StreamEventRecorder eventRecorder;

	private final DocumentService documentService;

	private final StreamProviderService streamProviderService;

	private final Course course;

	private final Jsonb jsonb;

	private WebSocket webSocket;


	public StreamWebSocketClient(EventBus eventBus, ServiceParameters parameters,
			WebSocketHeaderProvider headerProvider,
			StreamEventRecorder eventRecorder, DocumentService documentService,
			StreamProviderService streamProviderService, Course course) {
		requireNonNull(eventBus);
		requireNonNull(parameters);
		requireNonNull(headerProvider);
		requireNonNull(eventRecorder);
		requireNonNull(documentService);
		requireNonNull(streamProviderService);
		requireNonNull(course);

		this.eventBus = eventBus;
		this.serviceParameters = parameters;
		this.headerProvider = headerProvider;
		this.eventRecorder = eventRecorder;
		this.documentService = documentService;
		this.streamProviderService = streamProviderService;
		this.course = course;

		JsonbConfig config = JsonConfigProvider.createConfig().withAdapters(
				new SpeechMessageAdapter(),
				new CourseParticipantMessageAdapter());

		this.jsonb = JsonbBuilder.create(config);
	}

	public void setWebRtcUp() {
		if (!started()) {
			return;
		}

		try {
			send(new StreamStartAction(course.getId()));
		}
		catch (IOException e) {
			logException(e, "Send event state failed");
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		eventRecorder.addDocumentConsumer(this::uploadDocument);
		eventRecorder.addRecordedActionConsumer(action -> {
			try {
				send(action);
			}
			catch (Exception e) {
				logException(e, "Send event state failed");
			}
		});
	}

	@Override
	protected void startInternal() throws ExecutableException {
		HttpClient httpClient = HttpClient.newBuilder()
				.sslContext(createSSLContext())
				.build();

		Builder webSocketBuilder = httpClient.newWebSocketBuilder();
		webSocketBuilder.subprotocols("state-protocol");
		webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));

		headerProvider.setHeaders(webSocketBuilder);

		webSocket = webSocketBuilder
				.buildAsync(URI.create(serviceParameters.getUrl()),
						new WebSocketListener())
				.join();

		// Transmit initial document state.
		Document document = documentService.getDocuments().getSelectedDocument();

		StreamInitAction initAction = new StreamInitAction(course.getId());

		StreamDocumentSelectAction docSelectAction = new StreamDocumentSelectAction(document);

		StreamPageSelectedAction pageAction = new StreamPageSelectedAction(
				document.getCurrentPage());

		try {
			send(initAction);

			// Upload all opened PDF documents.
			for (var doc : documentService.getDocuments().asList()) {
				StreamDocumentCreateAction action = uploadDocument(doc);

				if (nonNull(action)) {
					send(action);
				}
			}

			send(List.of(docSelectAction, pageAction));
			send(eventRecorder.getPreRecordedActions());
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

	private void send(Collection<? extends StreamAction> actions) throws IOException {
		for (var action : actions) {
			send(action);
		}
	}

	private StreamDocumentCreateAction uploadDocument(Document document) {
		if (!document.isPDF()) {
			return new StreamDocumentCreateAction(document);
		}

		String docFileName = document.getName() + ".pdf";
		ByteArrayOutputStream docData = new ByteArrayOutputStream();

		try {
			document.toOutputStream(docData);
		}
		catch (IOException e) {
			logException(e, "Convert document failed");
			return null;
		}

		MultipartBody body = new MultipartBody();
		body.addFormData("file",
				new ByteArrayInputStream(docData.toByteArray()),
				MediaType.MULTIPART_FORM_DATA_TYPE, docFileName);

		String remoteFile = streamProviderService.uploadFile(body);

		StreamDocumentCreateAction docCreateAction = new StreamDocumentCreateAction(document);
		docCreateAction.setDocumentFile(remoteFile);

		return docCreateAction;
	}

	private static SSLContext createSSLContext() {
		SSLContext sslContext;

		try {
			X509TrustManager trustManager = new OwnTrustManager("keystore.jks",
					"mypassword");

			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new TrustManager[] { trustManager }, null);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return sslContext;
	}



	private class WebSocketListener implements Listener {

		/** Accumulating message buffer. */
		private StringBuffer buffer = new StringBuffer();


		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			logException(error, "WebSocket error");
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data,
				boolean last) {
			logTraceMessage("WebSocket <-: {0}", data);

			webSocket.request(1);

			buffer.append(data);

			if (last) {
				try {
					Object message = null;
					String jsonData = buffer.toString();

					JsonReader jsonReader = Json.createReader(new StringReader(jsonData));
					JsonObject jsonObject = jsonReader.readObject();
					String typeStr = jsonObject.getString("type");

					if (typeStr.startsWith("Speech")) {
						message = jsonb.fromJson(jsonData, SpeechBaseMessage.class);
					}
					else if (typeStr.startsWith("CourseParticipant")) {
						message = jsonb.fromJson(jsonData, CourseParticipantMessage.class);
					}

					if (nonNull(message)) {
						eventBus.post(message);
					}
				}
				catch (Exception e) {
					logException(e, "Process message failed");
				}

				buffer = new StringBuffer();
			}

			return null;
		}
	}
}
