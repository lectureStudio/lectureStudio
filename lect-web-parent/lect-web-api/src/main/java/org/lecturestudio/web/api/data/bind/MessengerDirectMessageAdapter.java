package org.lecturestudio.web.api.data.bind;

import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.model.Message;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import java.time.ZonedDateTime;

import static java.util.Objects.nonNull;

public class MessengerDirectMessageAdapter implements JsonbAdapter<MessengerDirectMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(MessengerDirectMessage messengerDirectMessage) throws Exception {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", messengerDirectMessage.getClass().getSimpleName());

		if (nonNull(messengerDirectMessage.getMessageDestinationUsername())) {
			builder.add("messageDestinationUsername", messengerDirectMessage.getMessageDestinationUsername());
		}

		if (nonNull(messengerDirectMessage.getMessage())) {
			builder.add("message", messengerDirectMessage.getMessage().getText());
		}
		if (nonNull(messengerDirectMessage.getFirstName())) {
			builder.add("firstName", messengerDirectMessage.getFirstName());
		}
		if (nonNull(messengerDirectMessage.getFamilyName())) {
			builder.add("familyName", messengerDirectMessage.getFamilyName());
		}
		if (nonNull(messengerDirectMessage.getRemoteAddress())) {
			builder.add("remoteAddress", messengerDirectMessage.getRemoteAddress());
		}
		if (nonNull(messengerDirectMessage.getDate())) {
			builder.add("date", messengerDirectMessage.getDate().toString());
		}

		if (nonNull(messengerDirectMessage.getMessageId())) {
			builder.add("messageId", messengerDirectMessage.getMessageId().toString());
		}

		if (nonNull(messengerDirectMessage.isReply())) {
			builder.add("reply", messengerDirectMessage.isReply());
		}

		return builder.build();
	}

	@Override
	public MessengerDirectMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		String messageDestinationUsername = null;
		if (jsonObject.get("messageDestinationUsername").getValueType() != JsonValue.ValueType.NULL) {
			messageDestinationUsername = jsonObject.getString("messageDestinationUsername");
		}
		MessengerDirectMessage directMessage = new MessengerDirectMessage(messageDestinationUsername);

		if (jsonObject.get("text").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setMessage(new Message(jsonObject.getString("text")));
		}
		if (jsonObject.get("time").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
		}
		if (jsonObject.get("firstName").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setFirstName(jsonObject.getString("firstName"));
		}
		if (jsonObject.get("familyName").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setFamilyName(jsonObject.getString("familyName"));
		}
		if (jsonObject.get("username").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setRemoteAddress(jsonObject.getString("username"));
		}
		if (jsonObject.get("messageId").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setMessageId(jsonObject.getString("messageId"));
		}
		if (jsonObject.get("reply").getValueType() != JsonValue.ValueType.NULL) {
			directMessage.setReply(jsonObject.getBoolean("reply"));
		}

		return directMessage;
	}
}