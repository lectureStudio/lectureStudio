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

public class InfoState implements JanusState {

	private JanusMessage infoMessage;


	@Override
	public void initialize(JanusHandler handler) {
		infoMessage = new JanusMessage();
		infoMessage.setEventType(JanusMessageType.INFO);
		infoMessage.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(infoMessage);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		checkTransaction(infoMessage, message);

		if (message instanceof JanusInfoMessage) {
			JanusInfoMessage info = (JanusInfoMessage) message;

			handler.setInfo(info.getJanusInfo());
			handler.setState(new CreateSessionState());
		}
	}

}
