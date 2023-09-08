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

package org.lecturestudio.web.api.websocket;

import java.net.http.WebSocket;

import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.web.socket.WebSocketHttpHeaders;

/**
 * Interface to set individual headers for STOMP WebSockets.
 *
 * @author Alex Andres
 */
public interface WebSocketStompHeaderProvider extends WebSocketHeaderProvider {

	/**
	 * Set WebSocket headers.
	 *
	 * @param builder The WebSocket builder.
	 */
	void setHeaders(WebSocket.Builder builder);

	/**
	 * Get all set HTTP headers for the WebSocket.
	 *
	 * @return The WebSocket HTTP headers.
	 */
	WebSocketHttpHeaders getHeaders();

	/**
	 * @param headers
	 *
	 * @return
	 */
	StompHeaders addHeaders(StompHeaders headers);

}
