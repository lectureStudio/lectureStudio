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

import java.time.ZonedDateTime;
import java.util.Objects;

import org.lecturestudio.web.api.model.Message;

public class MessengerMessage extends WebMessage {

	private Message message;


	public MessengerMessage() {
		this(null, null, null);
	}

	public MessengerMessage(Message message, String remoteAddress, ZonedDateTime date) {
		setMessage(message);
		setRemoteAddress(remoteAddress);
		setDate(date);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MessengerMessage other = (MessengerMessage) o;

		return Objects.equals(message, other.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName());
		buffer.append(": ");
		buffer.append(getMessage());
		buffer.append(", ");
		buffer.append(getDate());
		buffer.append(", ");
		buffer.append("RemoteAddress: ");
		buffer.append(getRemoteAddress());

		return buffer.toString();
	}

}
