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

package org.lecturestudio.core.net.protocol.srp;

import org.lecturestudio.core.net.protocol.srp.message.SrpClientEvidenceMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpClientIdentityMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpServerChallengeMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpServerEvidenceMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpMessage;
import org.lecturestudio.core.net.protocol.srp.message.SrpMessageCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SrpJsonParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();


	/**
	 * Convert a JSON string to {@link SrpMessage}.
	 * 
	 * @param content The JSON string.
	 * 
	 * @return A {@link SrpMessage}.
	 * 
	 * @throws Exception on JSON parse error.
	 */
	public static SrpMessage getMessage(String content) throws Exception {
		JsonNode rootNode = MAPPER.readTree(content);
		JsonNode codeNode = rootNode.path("messageCode");

		SrpMessageCode code = MAPPER.readValue(codeNode.toString(), SrpMessageCode.class);
		Class<? extends SrpMessage> valueType = null;

		switch (code) {
			case CLIENT_EVIDENCE:
				valueType = SrpClientEvidenceMessage.class;
				break;

			case CLIENT_IDENTITY:
				valueType = SrpClientIdentityMessage.class;
				break;

			case SERVER_CHALLENGE:
				valueType = SrpServerChallengeMessage.class;
				break;

			case SERVER_EVIDENCE:
				valueType = SrpServerEvidenceMessage.class;
				break;

			case ERROR:
				break;
		}

		return MAPPER.readValue(content, valueType);
	}

	/**
	 * Convert the specified {@link SrpMessage} to a byte array.
	 *
	 * @param message The {@link SrpMessage}
	 *
	 * @return The resulting byte array.
	 */
	public static byte[] getByteArray(SrpMessage message) throws JsonProcessingException {
		return MAPPER.writeValueAsBytes(message);
	}

}
