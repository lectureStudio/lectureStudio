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

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTypeAdapter;

import org.lecturestudio.web.api.janus.json.MessageTypeAdapter;

/**
 * Basic message implementation to communicate with endpoints of the Janus
 * WebRTC Server. The {@code JanusMessage} is meant to be extended by specific
 * message classes as it contains fields that all messages require. With this
 * basic message you can only communicate with the server root, meaning only to
 * create a Janus session.
 *
 * @author Alex Andres
 */
public class JanusMessage {

	@JsonbProperty("janus")
	@JsonbTypeAdapter(MessageTypeAdapter.class)
	private JanusMessageType eventType;

	private String transaction;


	/**
	 * Get the event type this message describes.
	 *
	 * @return The message event type.
	 */
	public JanusMessageType getEventType() {
		return eventType;
	}

	/**
	 * Set the event type this message describes.
	 *
	 * @param eventType The message event type.
	 */
	public void setEventType(JanusMessageType eventType) {
		this.eventType = eventType;
	}

	/**
	 * Get the random alphanumeric string to match incoming messages.
	 *
	 * @return The random alphanumeric string.
	 */
	public String getTransaction() {
		return transaction;
	}

	/**
	 * Set the random alphanumeric string to match incoming messages.
	 *
	 * @param transaction The new random alphanumeric string.
	 */
	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}
}
