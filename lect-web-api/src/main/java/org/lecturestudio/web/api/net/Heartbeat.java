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

package org.lecturestudio.web.api.net;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;

import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.web.api.event.HeartbeatEvent;

/**
 * Sends HTTP GET requests to a specified URL at regular intervals to check the availability of a server or service.
 *
 * @author Alex Andres
 */
public class Heartbeat {

	private final EventBus eventBus;
	private final HttpClient httpClient;
	private final String url;
	private final long interval;
	private final Duration timeout;
	private boolean running;
	private ScheduledExecutorService scheduler;


	/**
	 * Constructs a new Heartbeat instance with the specified interval, URL, and timeout.
	 *
	 * @param eventBus The event bus to publish occurred events.
	 * @param interval The interval in milliseconds at which heartbeat requests should be sent.
	 * @param url      The URL to which the heartbeat request will be sent.
	 * @param timeout  The timeout for the HTTP connection.
	 */
	public Heartbeat(EventBus eventBus, String url, long interval, Duration timeout) {
		this.eventBus = eventBus;
		this.interval = interval;
		this.url = url;
		this.timeout = timeout;
		this.running = false;

		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(timeout)
				.build();
	}

	/**
	 * Starts the heartbeat task asynchronously. This method can only be called once. Repeated calls will not start
	 * additional tasks.
	 */
	public synchronized void start() {
		if (running) {
			return;
		}

		running = true;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(() -> {
			if (!isRunning()) {
				return;
			}

			sendHeartbeat();
		}, interval, interval, TimeUnit.MILLISECONDS);
	}

	/**
	 * Stops the heartbeat task.
	 */
	public synchronized void stop() {
		running = false;

		if (scheduler != null) {
			scheduler.shutdownNow();
		}
	}

	private synchronized boolean isRunning() {
		return running;
	}

	private void sendHeartbeat() {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(timeout)
				.GET()
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				eventBus.post(new HeartbeatEvent(HeartbeatEvent.Type.FAILURE));
			}
		}
		catch (Exception e) {
			eventBus.post(new HeartbeatEvent(HeartbeatEvent.Type.FAILURE));
		}
	}
}
