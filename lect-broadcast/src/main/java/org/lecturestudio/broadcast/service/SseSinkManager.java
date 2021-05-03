package org.lecturestudio.broadcast.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.sse.SseEventSink;

@ApplicationScoped
public class SseSinkManager {

	private final Map<String, SseEventSink> sseSinks;


	public SseSinkManager() {
		sseSinks = new ConcurrentHashMap<>();
	}

	public SseEventSink getSseSink(String serviceId) {
		return sseSinks.get(serviceId);
	}

	public void registerSseSink(SseEventSink eventSink, String serviceId) {
		if (!sseSinks.containsKey(serviceId)) {
			sseSinks.put(serviceId, eventSink);
		}
	}
}
