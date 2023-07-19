/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class WebSocketClientHandler extends TimerTask implements Listener {

	private static final Logger LOG = LogManager.getLogger(WebSocketClientHandler.class);

	/** Accumulating message buffer. */
	private StringBuffer buffer = new StringBuffer();

	private WebSocket webSocket;

	private Timer heartBeatTimer;


	abstract protected void onText(WebSocket webSocket, String text);


	@Override
	public void onOpen(WebSocket webSocket) {
		webSocket.request(1);

		startHeartBeat(webSocket);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode,
			String reason) {
		stopHeartBeat();
		return null;
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		stopHeartBeat();

		LOG.error("WebSocket error: " + error.getMessage());
	}

	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
		return Listener.super.onPing(webSocket, message);
	}

	@Override
	public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
		return Listener.super.onPong(webSocket, message);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data,
			boolean last) {
		LOG.trace("WebSocket <-: " + data);

		webSocket.request(1);

		buffer.append(data);

		if (last) {
			onText(webSocket, buffer.toString());

			buffer = new StringBuffer();
		}

		return null;
	}

	@Override
	public void run() {
		try {
			webSocket.sendPing(ByteBuffer.allocate(0));
		}
		catch (Throwable e) {
			LOG.error("WebSocket send ping failed", e);
		}
	}

	private void startHeartBeat(WebSocket socket) {
		webSocket = socket;

		heartBeatTimer = new Timer("WS Heart Beat Timer");
		heartBeatTimer.scheduleAtFixedRate(this, 30000, 30000);
	}

	private void stopHeartBeat() {
		webSocket = null;

		heartBeatTimer.cancel();
		heartBeatTimer.purge();
	}
}
