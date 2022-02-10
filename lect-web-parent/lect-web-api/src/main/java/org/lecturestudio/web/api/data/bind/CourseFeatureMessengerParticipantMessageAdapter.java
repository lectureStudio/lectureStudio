package org.lecturestudio.web.api.data.bind;

import org.lecturestudio.web.api.message.CourseFeatureMessengerParticipantMessage;
import org.lecturestudio.web.api.message.CourseParticipantMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.adapter.JsonbAdapter;

public class CourseFeatureMessengerParticipantMessageAdapter implements JsonbAdapter<CourseFeatureMessengerParticipantMessage, JsonObject> {

    @Override
    public JsonObject adaptToJson(CourseFeatureMessengerParticipantMessage message) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", message.getClass().getSimpleName());
        builder.add("connected", message.getConnected());
        builder.add("firstName", message.getFirstName());
        builder.add("familyName", message.getFamilyName());
        builder.add("username", message.getUsername());

        return builder.build();
    }

    @Override
    public CourseFeatureMessengerParticipantMessage adaptFromJson(JsonObject jsonObject) throws Exception {
        String typeStr = jsonObject.getString("type");
        String className = CourseFeatureMessengerParticipantMessage.class.getPackageName() + "." + typeStr;
        Class<?> cls = Class.forName(className);

        CourseFeatureMessengerParticipantMessage message = (CourseFeatureMessengerParticipantMessage) cls.getConstructor().newInstance();
        message.setConnected(jsonObject.getBoolean("connected"));
        message.setFirstName(jsonObject.getString("firstName"));
        message.setFamilyName(jsonObject.getString("familyName"));
        message.setUsername(jsonObject.getString("username"));

        return message;
    }
}
