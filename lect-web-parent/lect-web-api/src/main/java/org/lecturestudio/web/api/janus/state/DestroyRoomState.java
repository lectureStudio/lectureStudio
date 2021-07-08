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

import java.util.UUID;

import org.lecturestudio.web.api.janus.JanusHandler;
import org.lecturestudio.web.api.janus.message.JanusDestroyRoomMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionMessage;

/**
 * This state destroys a previously created video-room on the Janus WebRTC
 * server.
 *
 * @author Alex Andres
 */
public class DestroyRoomState implements JanusState {

	private JanusPluginDataMessage destroyRoomRequest;


	@Override
	public void initialize(JanusHandler handler) {
		logDebug("Destroying room");

		JanusDestroyRoomMessage request = new JanusDestroyRoomMessage();
		request.setRoom(handler.getRoomId());
		request.setSecret(handler.getRoomSecret());

		destroyRoomRequest = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		destroyRoomRequest.setTransaction(UUID.randomUUID().toString());
		destroyRoomRequest.setBody(request);

		handler.sendMessage(destroyRoomRequest);

		handler.getPeerConnection().close();
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
//		// Destroy the session as well. Though, it will timeout anyway.
//		JanusSessionMessage destroySessionMessage = new JanusSessionMessage(
//				handler.getSessionId());
//		destroySessionMessage.setTransaction(UUID.randomUUID().toString());
//		destroySessionMessage.setEventType(JanusMessageType.DESTROY);
//
//		handler.sendMessage(destroySessionMessage);
	}
}
