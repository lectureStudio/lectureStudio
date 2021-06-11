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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.ws.rs.NotSupportedException;

import org.lecturestudio.web.api.janus.JanusInfo;
import org.lecturestudio.web.api.janus.JanusPublisher;
import org.lecturestudio.web.api.janus.JanusRoom;
import org.lecturestudio.web.api.janus.message.JanusErrorMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusInfoMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomCreatedMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomEventType;
import org.lecturestudio.web.api.janus.message.JanusRoomJoinedMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomListMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherJoinedMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherLeftMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomPublisherUnpublishedMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionSuccessMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionTimeoutMessage;

/**
 * Factory that creates various {@link JanusMessage}s out of the JSON formatted
 * messages received over a signaling channel from the Janus server.
 *
 * @author Alex Andres
 */
public class JanusMessageFactory {

	/**
	 * Create a {@code JanusMessage} from the provided JSON formatted input.
	 *
	 * @param body The message body in JSON format.
	 * @param type The message type that classifies the received message.
	 *
	 * @return A {@code JanusMessage} with its specific information.
	 */
	public static JanusMessage createMessage(Jsonb jsonb, JsonObject body, JanusMessageType type) {
		JsonObject pluginData = body.getJsonObject("plugindata");

		if (isNull(pluginData)) {
			// Common Janus signaling messages.
			switch (type) {
				case ACK:
					return createAcknowledgedMessage(body);

				case ERROR:
					return createErrorMessage(body);

				case SERVER_INFO:
					return createInfoMessage(body);

				case SUCCESS:
					return createSessionSuccessMessage(body);

				case TIMEOUT:
					return createSessionTimeoutMessage(body);
			}
		}
		else {
			// Janus plugin related signaling messages.
			JsonObject data = pluginData.getJsonObject("data");

			if (nonNull(data)) {
				String responseStr = data.getString("videoroom");
				var responseType = JanusRoomEventType
						.fromString(responseStr);

				switch (responseType) {
					case CREATED:
						return createRoomCreatedMessage(body, data, type);

					case EVENT:
						return createRoomEventMessage(body, data, type, jsonb);

					case JOINED:
						return createRoomJoinedMessage(body, data, type, jsonb);

					case SUCCESS:
						return createRoomSuccessMessage(body, data, type, jsonb);

					default:
						throw new NotSupportedException(
								"Event type not supported: " + responseStr);
				}
			}
		}

		throw new NotSupportedException("Message type not supported: " + type);
	}

	private static JanusMessage createAcknowledgedMessage(JsonObject body) {
		JanusMessage message = new JanusMessage();
		message.setEventType(JanusMessageType.ACK);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createErrorMessage(JsonObject body) {
		var data = body.getJsonObject("error");
		var code = data.getJsonNumber("code").intValue();
		var reason = data.getString("reason");

		JanusErrorMessage message = new JanusErrorMessage(code, reason);

		if (body.containsKey("transaction")) {
			// Not all error messages have a transaction ID, e.g. if we forgot
			// to set one in a request.
			message.setTransaction(body.getString("transaction"));
		}

		return message;
	}

	private static JanusMessage createInfoMessage(JsonObject body) {
		var sessionTimeout = body.getJsonNumber("session-timeout").intValue();
		var apiSecret = body.getBoolean("api_secret");
		var authToken = body.getBoolean("auth_token");

		var info = new JanusInfo(apiSecret, authToken, sessionTimeout);

		JanusInfoMessage message = new JanusInfoMessage(info);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createSessionSuccessMessage(JsonObject body) {
		var data = body.getJsonObject("data");
		var id = data.getJsonNumber("id").bigIntegerValue();

		JanusSessionSuccessMessage message = new JanusSessionSuccessMessage(id);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createSessionTimeoutMessage(JsonObject body) {
		var id = body.getJsonNumber("session_id").bigIntegerValue();

		return new JanusSessionTimeoutMessage(id);
	}

	private static JanusMessage createRoomCreatedMessage(JsonObject body,
			JsonObject data, JanusMessageType type) {
		var sessionId = body.getJsonNumber("session_id").bigIntegerValue();
		var roomId = data.getJsonNumber("room").bigIntegerValue();

		JanusRoomCreatedMessage message = new JanusRoomCreatedMessage(sessionId, roomId);
		message.setEventType(type);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createRoomJoinedMessage(JsonObject body,
			JsonObject data, JanusMessageType type, Jsonb jsonb) {
		var sessionId = body.getJsonNumber("session_id").bigIntegerValue();
		var roomId = data.getJsonNumber("room").bigIntegerValue();
		var id = data.getJsonNumber("id").bigIntegerValue();
		var privateId = data.getJsonNumber("private_id").bigIntegerValue();
		var description = data.getString("description");
		var publisherArray = data.getJsonArray("publishers");

		List<JanusPublisher> publishers = jsonb.fromJson(publisherArray.toString(),
				new ArrayList<JanusPublisher>(){}.getClass().getGenericSuperclass());

		JanusRoomJoinedMessage message = new JanusRoomJoinedMessage(sessionId,
				roomId, id, privateId, description, publishers);
		message.setEventType(type);
		message.setRoomEventType(JanusRoomEventType.JOINED);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createRoomPublisherJoinedMessage(JsonObject body,
			JsonObject data, JanusMessageType type, Jsonb jsonb) {
		var sessionId = body.getJsonNumber("session_id").bigIntegerValue();
		var roomId = data.getJsonNumber("room").bigIntegerValue();
		var publisherArray = data.getJsonArray("publishers");

		List<JanusPublisher> publishers = jsonb.fromJson(publisherArray.toString(),
				new ArrayList<JanusPublisher>(){}.getClass().getGenericSuperclass());

		JanusRoomPublisherJoinedMessage message = new JanusRoomPublisherJoinedMessage(
				sessionId, roomId, publishers.get(0));
		message.setEventType(type);
		message.setRoomEventType(JanusRoomEventType.EVENT);

		return message;
	}

	private static JanusMessage createRoomPublisherLeftMessage(JsonObject body,
			JsonObject data, JanusMessageType type) {
		var sessionId = body.getJsonNumber("session_id").bigIntegerValue();
		var roomId = data.getJsonNumber("room").bigIntegerValue();
		var publisherId = data.getJsonNumber("leaving").bigIntegerValue();

		JanusRoomPublisherLeftMessage message = new JanusRoomPublisherLeftMessage(
				sessionId, roomId, publisherId);
		message.setEventType(type);
		message.setRoomEventType(JanusRoomEventType.EVENT);

		return message;
	}

	private static JanusMessage createRoomPublisherUnpublishedMessage(JsonObject body,
			JsonObject data, JanusMessageType type) {
		var sessionId = body.getJsonNumber("session_id").bigIntegerValue();
		var roomId = data.getJsonNumber("room").bigIntegerValue();
		var publisherId = data.getJsonNumber("unpublished").bigIntegerValue();

		JanusRoomPublisherUnpublishedMessage message = new JanusRoomPublisherUnpublishedMessage(
				sessionId, roomId, publisherId);
		message.setEventType(type);
		message.setRoomEventType(JanusRoomEventType.EVENT);

		return message;
	}

	private static JanusMessage createRoomSuccessMessage(JsonObject body,
			JsonObject data, JanusMessageType type) {
		var id = body.getJsonNumber("session_id").bigIntegerValue();

		JanusSessionSuccessMessage message = new JanusSessionSuccessMessage(id);
		message.setEventType(type);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createRoomEventMessage(JsonObject body,
			JsonObject data, JanusMessageType type, Jsonb jsonb) {
		var eventType = JanusRoomEventType.fromString(data.getString("videoroom"));

		// There are multiple room events with individual payloads.
		if (data.containsKey("publishers")) {
			return createRoomPublisherJoinedMessage(body, data, type, jsonb);
		}
		else if (data.containsKey("unpublished")) {
			return createRoomPublisherUnpublishedMessage(body, data, type);
		}
		else if (data.containsKey("configured")) {
			return createRoomSuccessMessage(body, data, type);
		}
		else if (data.containsKey("leaving")) {
			return createRoomPublisherLeftMessage(body, data, type);
		}

		throw new NotSupportedException("Room event type not supported: " + eventType);
	}

	private static JanusMessage createRoomListMessage(JsonObject body,
			JsonObject data, JanusMessageType type, Jsonb jsonb) {
		var sessionId = body.getJsonNumber("session_id").bigIntegerValue();
		var eventType = JanusRoomEventType.fromString(data.getString("videoroom"));
		JsonArray list = data.getJsonArray("list");

		List<JanusRoom> rooms = jsonb.fromJson(list.toString(),
				new ArrayList<JanusRoom>(){}.getClass().getGenericSuperclass());

		JanusRoomListMessage message = new JanusRoomListMessage(sessionId, rooms);
		message.setEventType(type);
		message.setRoomEventType(eventType);
		message.setTransaction(body.getString("transaction"));

		return message;
	}

	private static JanusMessage createRoomSuccessMessage(JsonObject body,
			JsonObject data, JanusMessageType type, Jsonb jsonb) {
		var eventType = JanusRoomEventType.fromString(data.getString("videoroom"));

		// There are multiple room success messages with individual payloads.
		if (data.containsKey("list")) {
			return createRoomListMessage(body, data, type, jsonb);
		}

		throw new NotSupportedException("Room event type not supported: " + eventType);
	}
}
