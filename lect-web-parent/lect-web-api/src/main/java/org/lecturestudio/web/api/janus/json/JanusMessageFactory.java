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

package org.lecturestudio.web.api.janus.json;

import java.math.BigInteger;

import javax.json.JsonObject;
import javax.ws.rs.NotSupportedException;

import org.lecturestudio.web.api.janus.JanusInfo;
import org.lecturestudio.web.api.janus.message.JanusErrorMessage;
import org.lecturestudio.web.api.janus.message.JanusEventType;
import org.lecturestudio.web.api.janus.message.JanusInfoMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionSuccessMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionTimeoutMessage;

public class JanusMessageFactory {

	public static JanusMessage createMessage(JsonObject body, JanusEventType type) {
		switch (type) {
			case ERROR:
				return createErrorMessage(body);

			case SERVER_INFO:
				return createInfoMessage(body);

			case SUCCESS:
				return createSessionSuccessMessage(body);

			case TIMEOUT:
				return createSessionTimeoutMessage(body);
		}

		throw new NotSupportedException("Message type not supported: " + type);
	}

	private static JanusMessage createErrorMessage(JsonObject body) {
		JsonObject data = body.getJsonObject("error");
		int code = data.getJsonNumber("code").intValue();
		String reason = data.getString("reason");

		JanusErrorMessage message = new JanusErrorMessage(code, reason);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createInfoMessage(JsonObject body) {
		int sessionTimeout = body.getJsonNumber("session-timeout").intValue();
		boolean apiSecret = body.getBoolean("api_secret");
		boolean authToken = body.getBoolean("auth_token");

		JanusInfo info = new JanusInfo(apiSecret, authToken, sessionTimeout);

		JanusInfoMessage message = new JanusInfoMessage(info);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createSessionSuccessMessage(JsonObject body) {
		JsonObject data = body.getJsonObject("data");
		BigInteger id = data.getJsonNumber("id").bigIntegerValue();

		JanusSessionSuccessMessage message = new JanusSessionSuccessMessage(id);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createSessionTimeoutMessage(JsonObject body) {
		BigInteger id = body.getJsonNumber("session_id").bigIntegerValue();

		return new JanusSessionTimeoutMessage(id);
	}
}
