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
 * Message to send request related data to a plugin.
 *
 * @author Alex Andres
 */
public class JanusPluginDataMessage extends JanusPluginMessage {

	private Object body;


	/**
	 * Create a new {@code JanusPluginDataMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param handleId  The unique integer plugin handle ID.
	 */
	public JanusPluginDataMessage(BigInteger sessionId, BigInteger handleId) {
		super(sessionId, handleId);

		setEventType(JanusMessageType.MESSAGE);
	}

	/**
	 * Get data body of this message.
	 *
	 * @return The data body.
	 */
	public Object getBody() {
		return body;
	}

	/**
	 * Set data body of this message. The body contains specific plugin
	 * configuration data.
	 *
	 * @param body The data body.
	 */
	public void setBody(Object body) {
		this.body = body;
	}
}
