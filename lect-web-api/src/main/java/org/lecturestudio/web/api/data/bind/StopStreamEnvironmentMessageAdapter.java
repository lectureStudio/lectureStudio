package org.lecturestudio.web.api.data.bind;

import static java.util.Objects.nonNull;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.StopStreamEnvironmentMessage;

import java.time.ZonedDateTime;

public class StopStreamEnvironmentMessageAdapter implements JsonbAdapter<StopStreamEnvironmentMessage, JsonObject> {

    @Override
    public JsonObject adaptToJson(StopStreamEnvironmentMessage stopStreamEnvironmentMessage) throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("type", stopStreamEnvironmentMessage.getClass().getSimpleName());

        if (nonNull(stopStreamEnvironmentMessage.getMessageId())) {
            builder.add("messageId", stopStreamEnvironmentMessage.getMessageId());
        }

        return builder.build();
    }

    @Override
    public org.lecturestudio.web.api.message.StopStreamEnvironmentMessage adaptFromJson(JsonObject jsonObject) throws Exception {
        String typeStr = jsonObject.getString("type");
        String className = StopStreamEnvironmentMessage.class.getPackageName() + "." + typeStr;
        Class<?> cls = Class.forName(className);

        StopStreamEnvironmentMessage message = (StopStreamEnvironmentMessage) cls.getConstructor().newInstance();

        message.setMessageId(jsonObject.getString("messageId"));
        message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
        message.setUserId(jsonObject.getString("userId"));
        message.setFirstName(jsonObject.getString("firstName"));
        message.setFamilyName(jsonObject.getString("familyName"));

        return message;
    }
}
