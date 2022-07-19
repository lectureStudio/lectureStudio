package org.lecturestudio.web.api.data.bind;

import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.MessengerReplyMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.adapter.JsonbAdapter;

import java.time.ZonedDateTime;

import static java.util.Objects.nonNull;

public class MessengerReplyMessageAdapter implements JsonbAdapter<MessengerReplyMessage, JsonObject>  {

	@Override
	public JsonObject adaptToJson(MessengerReplyMessage messengerReplyMessage) throws Exception {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", messengerReplyMessage.getClass().getSimpleName());

		if (nonNull(messengerReplyMessage.getFirstName())) {
			builder.add("firstName", messengerReplyMessage.getFirstName());
		}
		if (nonNull(messengerReplyMessage.getFamilyName())) {
			builder.add("familyName", messengerReplyMessage.getFamilyName());
		}
		if (nonNull(messengerReplyMessage.getRemoteAddress())) {
			builder.add("remoteAddress", messengerReplyMessage.getRemoteAddress());
		}
		if (nonNull(messengerReplyMessage.getDate())) {
			builder.add("date", messengerReplyMessage.getDate().toString());
		}

		if (nonNull(messengerReplyMessage.getMessageId())) {
			builder.add("messageId", messengerReplyMessage.getMessageId().toString());
		}

		if (nonNull(messengerReplyMessage.getRepliedMessageId())) {
			builder.add("repliedMessageId", messengerReplyMessage.getRepliedMessageId());
		}

		return builder.build();
	}

	@Override
	public MessengerReplyMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		MessengerMessage messageToReply = new MessengerMessage();
		messageToReply.setMessageId(jsonObject.getString("repliedMessageId"));

		MessengerReplyMessage replyMessage = new MessengerReplyMessage(messageToReply);
		replyMessage.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
		replyMessage.setFirstName(jsonObject.getString("firstName"));
		replyMessage.setFamilyName(jsonObject.getString("familyName"));
		replyMessage.setRemoteAddress(jsonObject.getString("remoteAddress"));
		replyMessage.setMessageId(jsonObject.getString("messageId"));

		return replyMessage;
	}
}