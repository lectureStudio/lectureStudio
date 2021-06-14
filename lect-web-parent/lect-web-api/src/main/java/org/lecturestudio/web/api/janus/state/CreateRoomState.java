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
import org.lecturestudio.web.api.janus.message.JanusCreateRoomMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginDataMessage;
import org.lecturestudio.web.api.janus.message.JanusRoomCreatedMessage;

public class CreateRoomState implements JanusState {

	private JanusPluginDataMessage requestMessage;


	@Override
	public void initialize(JanusHandler handler) {
		JanusCreateRoomMessage request = new JanusCreateRoomMessage();
		request.setIsPrivate(false);
		request.setPermanent(false);

		requestMessage = new JanusPluginDataMessage(handler.getSessionId(),
				handler.getPluginId());
		requestMessage.setTransaction(UUID.randomUUID().toString());
		requestMessage.setBody(request);

		handler.sendMessage(requestMessage);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(requestMessage, message);

		if (message instanceof JanusRoomCreatedMessage) {
			JanusRoomCreatedMessage success = (JanusRoomCreatedMessage) message;

			logDebug("Janus room created: %d", success.getRoomId());

			handler.setRoomId(success.getRoomId());
			handler.setState(new JoinRoomState());
		}
	}
}
