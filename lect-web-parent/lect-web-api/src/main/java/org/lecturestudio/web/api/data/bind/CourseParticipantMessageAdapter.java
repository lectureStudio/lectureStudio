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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.CourseParticipantMessage;

public class CourseParticipantMessageAdapter implements JsonbAdapter<CourseParticipantMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(CourseParticipantMessage message) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", message.getClass().getSimpleName());
		builder.add("connected", message.getConnected());

		if (nonNull(message.getFirstName())) {
			builder.add("firstName", message.getFirstName());
		}
		if (nonNull(message.getFamilyName())) {
			builder.add("familyName", message.getFamilyName());
		}

		return builder.build();
	}

	@Override
	public CourseParticipantMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		String typeStr = jsonObject.getString("type");
		String className = CourseParticipantMessage.class.getPackageName() + "." + typeStr;
		Class<?> cls = Class.forName(className);

		CourseParticipantMessage message = (CourseParticipantMessage) cls.getConstructor().newInstance();
		message.setFirstName(jsonObject.getString("firstName"));
		message.setFamilyName(jsonObject.getString("familyName"));
		message.setConnected(jsonObject.getBoolean("connected"));

		return message;
	}
}
