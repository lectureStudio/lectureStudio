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

package org.lecturestudio.web.api.janus.state;

import java.util.Optional;
import java.util.UUID;

import org.lecturestudio.web.api.janus.JanusRoom;
import org.lecturestudio.web.api.janus.JanusStateHandler;
import org.lecturestudio.web.api.janus.message.JanusCreateRoomMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomEventType;
import org.lecturestudio.web.api.janus.message.JanusRoomStateMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomListMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomRequest;
import org.lecturestudio.web.api.janus.message.JanusRoomRequestType;
import org.lecturestudio.web.api.stream.StreamContext;
import org.lecturestudio.web.api.stream.model.Course;

/**
 * This state creates a new video-room on the Janus WebRTC server.
 *
 * @author Alex Andres
 */
public class CreateRoomState implements JanusState {

	private JanusPluginDataMessage requestMessage;


	@Override
	public void initialize(JanusStateHandler handler) {
		Course course = handler.getStreamContext().getCourse();

		logDebug("Creating Janus room for: %s", course.getTitle());

		JanusRoomRequest request = new JanusRoomRequest();
		request.setRequestType(JanusRoomRequestType.LIST);

		requestMessage = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		requestMessage.setTransaction(UUID.randomUUID().toString());
		requestMessage.setBody(request);

		handler.sendMessage(requestMessage);
	}

	@Override
	public void handleMessage(JanusStateHandler handler, JanusMessage message) {
		checkTransaction(requestMessage, message);

		if (message instanceof JanusRoomListMessage) {
			JanusRoomListMessage roomsMessage = (JanusRoomListMessage) message;

			Optional<JanusRoom> roomOpt = roomsMessage.getRooms().stream()
					.filter(room -> room.getRoomId().equals(handler.getRoomId()))
					.findFirst();

			if (roomOpt.isEmpty()) {
				createRoom(handler);
			}
			else {
				joinRoom(handler);
			}
		}
		else if (message instanceof JanusRoomStateMessage) {
			JanusRoomStateMessage stateMessage = (JanusRoomStateMessage) message;

			if (stateMessage.getRoomEventType() == JanusRoomEventType.CREATED) {
				logDebug("Janus room created: %d", stateMessage.getRoomId());

				if (!stateMessage.getRoomId().equals(handler.getRoomId())) {
					throw new IllegalStateException("Room IDs do not match");
				}

				joinRoom(handler);
			}
		}
	}

	private void joinRoom(JanusStateHandler handler) {
		handler.setState(new JoinRoomState());
	}

	private void createRoom(JanusStateHandler handler) {
		StreamContext context = handler.getStreamContext();
		Course course = context.getCourse();

		JanusCreateRoomMessage request = new JanusCreateRoomMessage();
		request.setRoomId(handler.getRoomId());
		request.setDescription(course.getTitle());
		request.setPublishers(1);
		request.setBitrate(context.getVideoContext().getBitrate() * 1000);
		request.setNotifyJoining(true);
		//request.setSecret(handler.getRoomSecret());

		requestMessage = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		requestMessage.setTransaction(UUID.randomUUID().toString());
		requestMessage.setBody(request);

		handler.sendMessage(requestMessage);
	}
}
