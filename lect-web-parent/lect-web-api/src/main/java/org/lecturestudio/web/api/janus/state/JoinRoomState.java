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

import java.math.BigInteger;
import java.util.UUID;

import org.lecturestudio.web.api.janus.JanusHandler;
import org.lecturestudio.web.api.janus.JanusMessageTransmitter;
import org.lecturestudio.web.api.janus.JanusParticipantType;
import org.lecturestudio.web.api.janus.message.JanusRoomJoinRequest;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomJoinedMessage;

public class JoinRoomState implements JanusState {

	private final BigInteger sessionId;

	private final BigInteger pluginId;

	private final BigInteger roomId;

	private JanusPluginDataMessage joinMessage;


	public JoinRoomState(BigInteger sessionId, BigInteger pluginId, BigInteger roomId) {
		this.sessionId = sessionId;
		this.pluginId = pluginId;
		this.roomId = roomId;
	}

	@Override
	public void initialize(JanusMessageTransmitter transmitter) {
		JanusRoomJoinRequest request = new JanusRoomJoinRequest();
		request.setParticipantType(JanusParticipantType.PUBLISHER);
		request.setRoomId(roomId);

		joinMessage = new JanusPluginDataMessage(sessionId, pluginId);
		joinMessage.setTransaction(UUID.randomUUID().toString());
		joinMessage.setBody(request);

		transmitter.sendMessage(joinMessage);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(joinMessage, message);

		if (message instanceof JanusRoomJoinedMessage) {
			handler.setState(new PublishToRoomState(handler.getSessionId(),
					handler.getPluginId()));
		}
	}
}
