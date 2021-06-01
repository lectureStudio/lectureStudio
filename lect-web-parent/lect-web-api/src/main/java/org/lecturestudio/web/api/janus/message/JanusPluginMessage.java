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

package org.lecturestudio.web.api.janus.message;

import java.math.BigInteger;

/**
 * Plugin message to perform plugin related requests with the Janus WebRTC
 * Server. The plugin message builds up on the {@link JanusSessionMessage}. The
 * plugin message contains fields, such as the plugin handle ID, to be used with
 * push mechanisms made available by transport protocols like WebSockets,
 * RabbitMQ, MQTT, etc. For a plain HTTP REST communication this message type
 * may not be used.
 *
 * @author Alex Andres
 */
public class JanusPluginMessage extends JanusSessionMessage {

	private final BigInteger handleId;


	/**
	 * Create a new {@code JanusPluginMessage} with the specified parameters.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param handleId  The unique integer plugin handle ID.
	 */
	public JanusPluginMessage(BigInteger sessionId, BigInteger handleId) {
		super(sessionId);

		this.handleId = handleId;
	}

	/**
	 * Get the unique plugin handle ID.
	 *
	 * @return The unique integer plugin handle ID.
	 */
	public BigInteger getHandleId() {
		return handleId;
	}
}
