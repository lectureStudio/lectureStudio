/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.message;

import static java.util.Objects.requireNonNullElse;

import java.time.ZonedDateTime;

import org.lecturestudio.web.api.model.Message;

public class MessengerMessage extends UserMessage {

	private String receiverId;

	private String receiverFirstName;

	private String receiverFamilyName;

	private Message message;


	public MessengerMessage() {
		this(null, null, null);
	}

	public MessengerMessage(Message message, String userId,
			ZonedDateTime date) {
		setMessage(message);
		setUserId(userId);
		setDate(date);
	}

	public MessengerMessage(Message message, String remoteAddress,
			ZonedDateTime date, String messageId) {
		this(message, remoteAddress, date);

		setMessageId(messageId);
	}

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Message message) {
		this.message = message;
	}

	public String getReceiverFirstName() {
		return requireNonNullElse(receiverFirstName, "");
	}

	public void setReceiverFirstName(String firstName) {
		receiverFirstName = firstName;
	}

	public String getReceiverFamilyName() {
		return requireNonNullElse(receiverFamilyName, "");
	}

	public void setReceiverFamilyName(String familyName) {
		receiverFamilyName = familyName;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String id) {
		receiverId = id;
	}
}
