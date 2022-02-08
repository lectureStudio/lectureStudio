package org.lecturestudio.web.api.data.bind;

import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.model.Message;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
        String messageDestinationUsername = jsonObject.getString("messageDestinationUsername");
        MessengerDirectMessage directMessage = new MessengerDirectMessage(messageDestinationUsername);

        directMessage.setMessage(new Message(jsonObject.getString("text")));
        directMessage.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
        directMessage.setFirstName(jsonObject.getString("firstName"));
        directMessage.setFamilyName(jsonObject.getString("familyName"));
        directMessage.setRemoteAddress(jsonObject.getString("remoteAddress"));
        directMessage.setMessageId(jsonObject.getString("messageId"));
        directMessage.setReply(jsonObject.getBoolean("reply"));

        return directMessage;
    }
}
