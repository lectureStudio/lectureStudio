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
import org.lecturestudio.web.api.janus.message.JanusInfoMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;

/**
 * Usually this is the initial state of the session establishment with the Janus
 * WebRTC server. This state gathers the server information, e.g. session
 * relevant settings like the session timeout. This information can then be used
 * by other states and the {@link JanusHandler}.
 *
 * @author Alex Andres
 */
public class InfoState implements JanusState {

	private JanusMessage infoRequest;


	@Override
	public void initialize(JanusHandler handler) {
		infoRequest = new JanusMessage();
		infoRequest.setEventType(JanusMessageType.INFO);
		infoRequest.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(infoRequest);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(infoRequest, message);

		if (message instanceof JanusInfoMessage) {
			JanusInfoMessage infoMessage = (JanusInfoMessage) message;

			// We're done here, move forward.
			handler.setInfo(infoMessage.getJanusInfo());
			handler.setState(new CreateSessionState());
		}
	}
}
