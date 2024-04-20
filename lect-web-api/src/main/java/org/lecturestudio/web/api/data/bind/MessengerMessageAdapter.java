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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.message.MessengerDirectMessageAsReply;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.MessengerMessageAsReply;
import org.lecturestudio.web.api.model.Message;

public class MessengerMessageAdapter implements JsonbAdapter<MessengerMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(MessengerMessage message) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", message.getClass().getSimpleName());
		builder.add("message", message.getMessage().getText());
		builder.add("userId", message.getUserId());
		builder.add("firstName", message.getFirstName());
		builder.add("familyName", message.getFamilyName());
		builder.add("date", message.getDate().toString());
		builder.add("messageId", message.getMessageId());

		if(message instanceof MessengerMessageAsReply messageAsReply) {
			builder.add("msgIdToReplyTo", messageAsReply.getMsgIdToReplyTo());
		}

		if (message instanceof MessengerDirectMessage directMessage) {
			builder.add("recipientId", directMessage.getRecipientId());
			builder.add("recipientFirstName", directMessage.getRecipientFirstName());
			builder.add("recipientFamilyName", directMessage.getRecipientFamilyName());
		}

		if(message instanceof MessengerDirectMessageAsReply directMessageAsReply) {
			builder.add("msgIdToReplyTo", directMessageAsReply.getMsgIdToReplyTo());
		}

		return builder.build();
	}

	@Override
	public MessengerMessage adaptFromJson(JsonObject jsonObject) {
		String type = jsonObject.getString("_type");

		MessengerMessage message;

		switch (type) {
			case "MessengerDirectMessage" -> {
				MessengerDirectMessage directMessage = new MessengerDirectMessage();

				directMessage.setRecipientId(jsonObject.getString("recipientId"));
				if (!jsonObject.isNull("recipientFirstName")) {
					directMessage.setRecipientFirstName(jsonObject.getString("recipientFirstName"));
				}
				if (!jsonObject.isNull("recipientFamilyName")) {
					directMessage.setRecipientFamilyName(jsonObject.getString("recipientFamilyName"));
				}
				message = directMessage;
			}
			case "MessengerDirectMessageAsReply" -> {
				MessengerDirectMessageAsReply directMessageAsReply = new MessengerDirectMessageAsReply();

				directMessageAsReply.setRecipientId(jsonObject.getString("recipientId"));
				if (!jsonObject.isNull("recipientFirstName")) {
					directMessageAsReply.setRecipientFirstName(jsonObject.getString("recipientFirstName"));
				}
				if (!jsonObject.isNull("recipientFamilyName")) {
					directMessageAsReply.setRecipientFamilyName(jsonObject.getString("recipientFamilyName"));
				}
				if (!jsonObject.isNull("messageIdToReplyTo")) {
					directMessageAsReply.setMsgIdToReplyTo(jsonObject.getString("msgIdToReplyTo"));
				}

				message = directMessageAsReply;
			}
			case "MessengerMessageAsReply" -> {
				MessengerMessageAsReply messageAsReply = new MessengerMessageAsReply();

				if (!jsonObject.isNull("msgIdToReplyTo")) {
					messageAsReply.setMsgIdToReplyTo(jsonObject.getString("msgIdToReplyTo"));
				}

				message = messageAsReply;
			}
			default -> message = new MessengerMessage();
		}

		if (jsonObject.get("text").getValueType() != JsonValue.ValueType.NULL) {
			message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
			message.setMessage(new Message(jsonObject.getString("text")));
			message.setFirstName(jsonObject.getString("firstName"));
		}

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
		if (jsonObject.get("deleted").getValueType() != JsonValue.ValueType.NULL) {
			message.setDeleted(jsonObject.getBoolean("deleted"));
		}
		if (jsonObject.get("edited").getValueType() != JsonValue.ValueType.NULL) {
			message.setEdited(jsonObject.getBoolean("edited"));
		}

		return message;
	}
}
