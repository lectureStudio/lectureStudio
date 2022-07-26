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

import static java.util.Objects.nonNull;

import java.time.ZonedDateTime;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.Message;

public class MessengerMessageAdapter implements JsonbAdapter<MessengerMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(MessengerMessage messengerMessage) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", messengerMessage.getClass().getSimpleName());

		if (nonNull(messengerMessage.getMessage())) {
			builder.add("message", messengerMessage.getMessage().getText());
		}
		if (nonNull(messengerMessage.getFirstName())) {
			builder.add("firstName", messengerMessage.getFirstName());
		}
		if (nonNull(messengerMessage.getFamilyName())) {
			builder.add("familyName", messengerMessage.getFamilyName());
		}
		if (nonNull(messengerMessage.getUserId())) {
			builder.add("userId", messengerMessage.getUserId());
		}
		if (nonNull(messengerMessage.getDate())) {
			builder.add("date", messengerMessage.getDate().toString());
		}
		if (nonNull(messengerMessage.getMessageId())) {
			builder.add("messageId", messengerMessage.getMessageId().toString());
		}
		if (nonNull(messengerMessage.getReply())) {
			builder.add("reply", messengerMessage.getReply());
		}

		return builder.build();
	}

	@Override
	public MessengerMessage adaptFromJson(JsonObject jsonObject) {
		MessengerMessage message = new MessengerMessage();

		if (jsonObject.get("text").getValueType() != JsonValue.ValueType.NULL) {
			message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
			message.setMessage(new Message(jsonObject.getString("text")));
			message.setFirstName(jsonObject.getString("firstName"));
		}
		message.setFamilyName(jsonObject.getString("familyName"));
		if (jsonObject.get("time").getValueType() != JsonValue.ValueType.NULL) {

			message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
		}
		if (jsonObject.get("firstName").getValueType() != JsonValue.ValueType.NULL) {
			message.setFirstName(jsonObject.getString("firstName"));
		}
		if (jsonObject.get("familyName").getValueType() != JsonValue.ValueType.NULL) {
			message.setFamilyName(jsonObject.getString("familyName"));
		}
		if (jsonObject.get("userId").getValueType() != JsonValue.ValueType.NULL) {
			message.setUserId(jsonObject.getString("userId"));
		}
		if (jsonObject.get("messageId").getValueType() != JsonValue.ValueType.NULL) {
			message.setMessageId(jsonObject.getString("messageId"));
		}
		if (jsonObject.get("reply").getValueType() != JsonValue.ValueType.NULL) {
			message.setReply(jsonObject.getBoolean("reply"));
		}

		return message;
	}
}
