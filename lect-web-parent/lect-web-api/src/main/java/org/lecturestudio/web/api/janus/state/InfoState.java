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
import org.lecturestudio.web.api.janus.JanusMessageTransmitter;
import org.lecturestudio.web.api.janus.message.JanusEventType;
import org.lecturestudio.web.api.janus.message.JanusInfoMessage;
import org.lecturestudio.web.api.janus.message.JanusMessage;

public class InfoState implements JanusState {

	private JanusMessage infoMessage;


	@Override
	public void initialize(JanusMessageTransmitter transmitter) {
		infoMessage = new JanusMessage();
		infoMessage.setEventType(JanusEventType.INFO);
		infoMessage.setTransaction(UUID.randomUUID().toString());

		transmitter.sendMessage(infoMessage);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		if (!infoMessage.getTransaction().equals(message.getTransaction())) {
			throw new IllegalStateException("Transactions do not match");
		}

		if (message instanceof JanusInfoMessage) {
			JanusInfoMessage info = (JanusInfoMessage) message;

			handler.setInfo(info.getJanusInfo());
			handler.setState(new CreateSessionState());
		}
	}

}
