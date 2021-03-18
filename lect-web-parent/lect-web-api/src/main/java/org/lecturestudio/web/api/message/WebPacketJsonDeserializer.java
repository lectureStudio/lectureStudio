/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.message;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebPacketJsonDeserializer extends JsonDeserializer<WebPacket> {

	private final ObjectMapper mapper;


	public WebPacketJsonDeserializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public WebPacket deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.getCodec().readTree(p);
		String msgClassName = node.get("messageClass").asText();
		JsonNode msgNode = node.get("message");

		Class<?> msgClass;
		try {
			msgClass = Class.forName(msgClassName);
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}

		WebMessage message = (WebMessage) mapper.treeToValue(msgNode, msgClass);

		return new WebPacket(message);
	}

}
