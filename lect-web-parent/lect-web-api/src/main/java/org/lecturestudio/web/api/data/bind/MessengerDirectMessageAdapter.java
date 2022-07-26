package org.lecturestudio.web.api.data.bind;

import static java.util.Objects.nonNull;

import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.model.Message;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import java.time.ZonedDateTime;

public class MessengerDirectMessageAdapter implements JsonbAdapter<MessengerDirectMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(MessengerDirectMessage directMessage) throws Exception {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", directMessage.getClass().getSimpleName());

		if (nonNull(directMessage.getRecipient())) {
			builder.add("recipient", directMessage.getRecipient());
		}
		if (nonNull(directMessage.getMessage())) {
			builder.add("message", directMessage.getMessage().getText());
		}
		if (nonNull(directMessage.getFirstName())) {
			builder.add("firstName", directMessage.getFirstName());
		}
		if (nonNull(directMessage.getFamilyName())) {
			builder.add("familyName", directMessage.getFamilyName());
		}
		if (nonNull(directMessage.getUserId())) {
			builder.add("userId", directMessage.getUserId());
		}
		if (nonNull(directMessage.getDate())) {
			builder.add("date", directMessage.getDate().toString());
		}
		if (nonNull(directMessage.getMessageId())) {
			builder.add("messageId", directMessage.getMessageId().toString());
		}
		if (nonNull(directMessage.isReply())) {
			builder.add("reply", directMessage.isReply());
		}

		return builder.build();
	}

	@Override
	public MessengerDirectMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		String recipient = null;
		if (jsonObject.get("recipient").getValueType() != JsonValue.ValueType.NULL) {
			recipient = jsonObject.getString("recipient");
		}
		MessengerDirectMessage directMessage = new MessengerDirectMessage(recipient);

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
			directMessage.setUserId(jsonObject.getString("username"));
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