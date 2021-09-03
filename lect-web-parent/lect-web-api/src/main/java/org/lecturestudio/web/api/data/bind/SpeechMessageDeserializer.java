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

import java.lang.reflect.Type;
import java.time.ZonedDateTime;

import javax.json.JsonObject;
import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import org.lecturestudio.web.api.message.SpeechBaseMessage;

public class SpeechMessageDeserializer implements JsonbDeserializer<SpeechBaseMessage> {

	@Override
	public SpeechBaseMessage deserialize(JsonParser parser,
			DeserializationContext context, Type type) {
		JsonObject jsonObj = parser.getObject();
		String typeStr = jsonObj.getString("type");

		String className = SpeechBaseMessage.class.getPackageName() + "." + typeStr;

		try {
			Class<?> cls = Class.forName(className);

			SpeechBaseMessage message = (SpeechBaseMessage) cls.getConstructor().newInstance();
			message.setRequestId(jsonObj.getJsonNumber("requestId").longValue());
			message.setDate(ZonedDateTime.parse(jsonObj.getString("time")));
			message.setFirstName(jsonObj.getString("firstName"));
			message.setFamilyName(jsonObj.getString("familyName"));

			return message;
		}
		catch (Exception e) {
			throw new JsonbException("Create message (" + className + ") failed");
		}
	}

}
