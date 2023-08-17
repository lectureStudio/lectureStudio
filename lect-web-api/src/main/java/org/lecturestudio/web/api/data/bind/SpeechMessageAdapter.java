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

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.bind.adapter.JsonbAdapter;

import org.lecturestudio.web.api.message.SpeechBaseMessage;

public class SpeechMessageAdapter implements JsonbAdapter<SpeechBaseMessage, JsonObject> {

	@Override
	public JsonObject adaptToJson(SpeechBaseMessage message) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", message.getClass().getSimpleName());

		if (nonNull(message.getUserId())) {
			builder.add("userId", message.getUserId());
		}
		if (nonNull(message.getRequestId())) {
			builder.add("requestId", message.getRequestId().toString());
		}
		if (nonNull(message.getDate())) {
			builder.add("time", message.getDate().toString());
		}
		if (nonNull(message.getFirstName())) {
			builder.add("firstName", message.getFirstName());
		}
		if (nonNull(message.getFamilyName())) {
			builder.add("familyName", message.getFamilyName());
		}

		return builder.build();
	}

	@Override
	public SpeechBaseMessage adaptFromJson(JsonObject jsonObject) throws Exception {
		String typeStr = jsonObject.getString("type");
		String className = SpeechBaseMessage.class.getPackageName() + "." + typeStr;
		Class<?> cls = Class.forName(className);

		SpeechBaseMessage message = (SpeechBaseMessage) cls.getConstructor().newInstance();
		message.setUserId(jsonObject.getString("userId"));
		message.setRequestId(UUID.fromString(jsonObject.getString("requestId")));
		message.setDate(ZonedDateTime.parse(jsonObject.getString("time")));
		message.setFirstName(jsonObject.isNull("firstName") ? null : jsonObject.getString("firstName"));
		message.setFamilyName(jsonObject.isNull("familyName") ? null : jsonObject.getString("familyName"));

		return message;
	}
}
