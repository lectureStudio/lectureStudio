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

package org.lecturestudio.web.api.data.bind;

import java.time.ZonedDateTime;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.Message;

public class MessengerMessageAdapter implements JsonbAdapter<MessengerMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(MessengerMessage messengerMessage) {
		return null;
	}

	@Override
	public MessengerMessage adaptFromJson(JsonObject jsonObject) {
		MessengerMessage message = new MessengerMessage();
		message.setMessage(new Message(jsonObject.getString("text")));
		message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));

		return message;
	}
}
