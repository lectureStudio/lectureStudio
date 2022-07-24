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
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.CoursePresenceMessage;
import org.lecturestudio.web.api.stream.model.CoursePresence;

public class CoursePresenceMessageAdapter implements JsonbAdapter<CoursePresenceMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(CoursePresenceMessage message) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", message.getClass().getSimpleName());
		builder.add("date", message.getDate().toString());
		builder.add("firstName", message.getFirstName());
		builder.add("familyName", message.getFamilyName());
		builder.add("userId", message.getUserId());
		builder.add("messageId", message.getMessageId());
		builder.add("presence", message.getCoursePresence().toString());

		return builder.build();
	}

	@Override
	public CoursePresenceMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		String typeStr = jsonObject.getString("type");
		String className = CoursePresenceMessage.class.getPackageName() + "." + typeStr;
		Class<?> cls = Class.forName(className);

		CoursePresenceMessage message = (CoursePresenceMessage) cls.getConstructor().newInstance();
		message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
		message.setFirstName(jsonObject.getString("firstName"));
		message.setFamilyName(jsonObject.getString("familyName"));
		message.setUserId(jsonObject.getString("userId"));
		message.setMessageId(jsonObject.getString("messageId"));
		message.setCoursePresence(CoursePresence.valueOf(jsonObject.getString("presence")));

		return message;
	}
}
