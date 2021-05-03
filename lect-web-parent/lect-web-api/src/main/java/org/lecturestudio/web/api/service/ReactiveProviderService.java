package org.lecturestudio.web.api.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.json.bind.Jsonb;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.lecturestudio.web.api.data.bind.JsonConfig;
import org.lecturestudio.web.api.message.WebMessage;

public abstract class ReactiveProviderService extends ProviderService {

	private final Map<Client, SseEventSource> eventSources;

	private final Jsonb jsonb;


	public ReactiveProviderService() {
		eventSources = new ConcurrentHashMap<>();
		jsonb = new JsonConfig().getContext(null);
	}

	protected <T extends WebMessage> void subscribeSse(String path, Class<T> cls,
			Consumer<T> onEvent, Consumer<Throwable> onError) {
		WebClientBuilder builder = new WebClientBuilder();
		builder.setTls(true);
		builder.setComponentClasses(JwtRequestFilter.class);

		Client client = builder.build();
		WebTarget target = client.target(parameters.getUrl() + path);

		SseEventSource eventSource = SseEventSource.target(target).build();
		eventSource.register(sseEvent -> {
			T message = jsonb.fromJson(sseEvent.readData(), cls);

			onEvent.accept(message);
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
