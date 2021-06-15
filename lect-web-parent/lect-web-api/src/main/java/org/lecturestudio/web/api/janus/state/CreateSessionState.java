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
import org.lecturestudio.web.api.janus.message.JanusMessageType;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionSuccessMessage;

/**
 * This state creates a Janus session. This is the first mandatory step for the
 * session establishment with the Janus WebRTC server.
 *
 * @author Alex Andres
 */
public class CreateSessionState implements JanusState {

	private JanusMessage createRequest;


	@Override
	public void initialize(JanusHandler handler) {
		createRequest = new JanusMessage();
		createRequest.setEventType(JanusMessageType.CREATE);
		createRequest.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(createRequest);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(createRequest, message);

		if (message instanceof JanusSessionSuccessMessage) {
			JanusSessionSuccessMessage success = (JanusSessionSuccessMessage) message;

			logDebug("Janus session created: %d", success.getSessionId());

			handler.setSessionId(success.getSessionId());
			handler.setState(new AttachPluginState());
		}
	}

}
