package org.lecturestudio.web.api.data.bind;

import org.lecturestudio.web.api.message.CourseFeatureMessengerParticipantMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import java.time.ZonedDateTime;

import static java.util.Objects.nonNull;

public class CourseFeatureMessengerParticipantMessageAdapter implements JsonbAdapter<CourseFeatureMessengerParticipantMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(CourseFeatureMessengerParticipantMessage message) {
		JsonObjectBuilder builder = Json.createObjectBuilder();

		builder.add("_type", message.getClass().getSimpleName());

		if (nonNull(message.getFirstName())) {
			builder.add("firstName", message.getFirstName());
		}
		if (nonNull(message.getFamilyName())) {
			builder.add("familyName", message.getFamilyName());
		}
		if (nonNull(message.getRemoteAddress())) {
			builder.add("remoteAddress", message.getRemoteAddress());
		}
		if (nonNull(message.getDate())) {
			builder.add("date", message.getDate().toString());
		}

		if (nonNull(message.getMessageId())) {
			builder.add("messageId", message.getMessageId().toString());
		}

		if (nonNull(message.getConnected())) {
			builder.add("connected", message.getConnected());
		}

		return builder.build();
	}

	@Override
	public CourseFeatureMessengerParticipantMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		String typeStr = jsonObject.getString("_type");
		String className = CourseFeatureMessengerParticipantMessage.class.getPackageName() + "." + typeStr;
		Class<?> cls = Class.forName(className);

		CourseFeatureMessengerParticipantMessage message = (CourseFeatureMessengerParticipantMessage) cls.getConstructor().newInstance();
		if (jsonObject.get("time").getValueType() != JsonValue.ValueType.NULL) {
			message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
		}
		if (jsonObject.get("firstName").getValueType() != JsonValue.ValueType.NULL) {
			message.setFirstName(jsonObject.getString("firstName"));
		}
		if (jsonObject.get("familyName").getValueType() != JsonValue.ValueType.NULL) {
			message.setFamilyName(jsonObject.getString("familyName"));
		}
		if (jsonObject.get("remoteAddress").getValueType() != JsonValue.ValueType.NULL) {
			message.setRemoteAddress(jsonObject.getString("remoteAddress"));
		}
		if (jsonObject.get("messageId").getValueType() != JsonValue.ValueType.NULL) {
			message.setMessageId(jsonObject.getString("messageId"));
		}
		if (jsonObject.get("connected").getValueType() != JsonValue.ValueType.NULL) {
			message.setConnected(jsonObject.getBoolean("connected"));
		}
		return message;
	}
}