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

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.UUID;

import org.lecturestudio.web.api.janus.JanusHandler;
import org.lecturestudio.web.api.janus.message.JanusCreateRoomMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomCreatedMessage;
import org.lecturestudio.web.api.stream.model.Lecture;

/**
 * This state creates a new video-room on the Janus WebRTC server.
 *
 * @author Alex Andres
 */
public class CreateRoomState implements JanusState {

	private JanusPluginDataMessage createRequest;

	private BigInteger roomId;


	@Override
	public void initialize(JanusHandler handler) {
		Lecture lecture = handler.getWebRtcConfig().getLecture();

		requireNonNull(lecture);

		logDebug("Creating Janus room for: %s", lecture.getTitle());

		handler.setRoomSecret(UUID.randomUUID().toString());

		roomId = BigInteger.valueOf(lecture.getRoomId());

		JanusCreateRoomMessage request = new JanusCreateRoomMessage();
		request.setRoom(roomId);
		request.setPublishers(1);
		request.setSecret(handler.getRoomSecret());

		createRequest = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		createRequest.setTransaction(UUID.randomUUID().toString());
		createRequest.setBody(request);

		handler.sendMessage(createRequest);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(createRequest, message);

		if (message instanceof JanusRoomCreatedMessage) {
			JanusRoomCreatedMessage success = (JanusRoomCreatedMessage) message;

			logDebug("Janus room created: %d", success.getRoomId());

			if (!success.getRoomId().equals(roomId)) {
				throw new IllegalStateException("Room IDs do not match");
			}

			handler.setRoomId(success.getRoomId());
			handler.setState(new JoinRoomState());
		}
	}
}
