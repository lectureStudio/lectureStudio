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
import org.lecturestudio.web.api.janus.JanusParticipantType;
import org.lecturestudio.web.api.janus.message.JanusRoomJoinRequest;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomJoinedMessage;

/**
 * This state joins a previously created video-room on the Janus WebRTC server.
 * By default this state joins the room as an publisher which makes you an
 * active send-only participant.
 *
 * @author Alex Andres
 */
public class JoinRoomState implements JanusState {

	private JanusPluginDataMessage joinRequest;


	@Override
	public void initialize(JanusHandler handler) {
		JanusRoomJoinRequest request = new JanusRoomJoinRequest();
		request.setParticipantType(JanusParticipantType.PUBLISHER);
		request.setRoomId(handler.getRoomId());

		joinRequest = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		joinRequest.setTransaction(UUID.randomUUID().toString());
		joinRequest.setBody(request);

		handler.sendMessage(joinRequest);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(joinRequest, message);

		if (message instanceof JanusRoomJoinedMessage) {
			JanusRoomJoinedMessage joinedMessage = (JanusRoomJoinedMessage) message;

			logDebug("Janus room joined: %d (%s)", joinedMessage.getRoomId(),
					joinedMessage.getDescription());

			handler.createPeerConnection();
			handler.setState(new PublishToRoomState());
		}
	}
}
