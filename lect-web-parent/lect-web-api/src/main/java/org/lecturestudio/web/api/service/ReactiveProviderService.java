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

package org.lecturestudio.web.api.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.json.bind.Jsonb;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.web.api.client.ApiKeyFilter;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.data.bind.JsonConfig;
import org.lecturestudio.web.api.message.MessageTransport;
import org.lecturestudio.web.api.message.WebMessage;

public abstract class ReactiveProviderService extends ProviderService {

	private static final Logger LOG = LogManager.getLogger(ReactiveProviderService.class);

	private final ServiceParameters parameters;

	private final TokenProvider tokenProvider;

	private final MessageTransport messageTransport;

	private final Map<Client, SseEventSource> eventSources;

	private final Jsonb jsonb;


	public ReactiveProviderService(ServiceParameters parameters) {
		this(parameters, null, null);
	}

	public ReactiveProviderService(ServiceParameters parameters,
			TokenProvider tokenProvider, MessageTransport messageTransport) {
		this.parameters = parameters;
		this.tokenProvider = tokenProvider;
		this.messageTransport = messageTransport;
		this.eventSources = new ConcurrentHashMap<>();
		this.jsonb = new JsonConfig().getContext(null);
	}

	public <T extends WebMessage> void addMessageListener(Class<T> cls,
			Consumer<T> onEvent) {
		messageTransport.addListener(cls, onEvent);
	}

	public <T extends WebMessage> void removeMessageListener(Class<T> cls,
			Consumer<T> onEvent) {
		messageTransport.removeListener(cls, onEvent);
	}

	protected <T extends WebMessage> void subscribeSse(String path, Class<T> cls,
			Consumer<T> onEvent, Consumer<Throwable> onError) {
		WebClientBuilder builder = new WebClientBuilder();
		builder.setTls(true);
		builder.setTokenProvider(tokenProvider);
		builder.setComponentClasses(ApiKeyFilter.class);

		Client client = builder.build();
		WebTarget target = client.target(parameters.getUrl() + path);

		SseEventSource eventSource = SseEventSource.target(target).build();
		eventSource.register(sseEvent -> {
			if (sseEvent.isEmpty()) {
				return;
			}

			T message = jsonb.fromJson(sseEvent.readData(), cls);

			try {
				onEvent.accept(message);
			}
			catch (Exception e) {
				LOG.error("Consume event message failed", e);
			}
		}, onError);
		eventSource.open();

		if (eventSource.isOpen()) {
			eventSources.put(client, eventSource);
		}
	}

	public void close() {
		for (Entry<Client, SseEventSource> entry : eventSources.entrySet()) {
			entry.getValue().close();
			entry.getKey().close();
		}
	}
}
