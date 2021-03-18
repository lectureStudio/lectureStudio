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

package org.lecturestudio.core.net.protocol.bcast.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.lecturestudio.core.net.Session;

public class BcastJsonParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();


	/**
	 * Convert a JSON string to {@link BcastMessage}.
	 *
	 * @param content The JSON string.
	 *
	 * @return a BcastMessage.
	 *
	 * @throws Exception on JSON parse error.
	 */
	public static BcastMessage<?> getMessage(String content) throws Exception {
		JsonNode rootNode = MAPPER.readTree(content);
		JsonNode codeNode = rootNode.path("messageCode");

		BcastMessageCode code = MAPPER
				.readValue(codeNode.toString(), BcastMessageCode.class);
		TypeReference<?> type = null;

		switch (code) {
			case SESSION_REQUEST:
				type = new TypeReference<BcastMessage<Session>>() {};
				break;

			case SESSION_REQUEST_ACK:
				type = new TypeReference<BcastMessage<Integer>>() {};
				break;

			case AUTH_REQUEST:
				type = new TypeReference<BcastMessage<Void>>() {};
				break;
		}

		return (BcastMessage<?>) MAPPER.readValue(content, type);
	}

	public static byte[] getByteArray(BcastMessage<?> message)
			throws JsonProcessingException {
		return MAPPER.writeValueAsBytes(message);
	}

}
