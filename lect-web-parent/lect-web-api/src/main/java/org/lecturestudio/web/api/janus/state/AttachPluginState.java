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
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginAttachMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionSuccessMessage;

public class AttachPluginState implements JanusState {

	private final static String PLUGIN = "janus.plugin.videoroom";

	private final BigInteger sessionId;

	private JanusMessage attachMessage;


	public AttachPluginState(BigInteger sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void initialize(JanusMessageTransmitter transmitter) {
		attachMessage = new JanusPluginAttachMessage(sessionId, PLUGIN);
		attachMessage.setTransaction(UUID.randomUUID().toString());

		transmitter.sendMessage(attachMessage);
	}

	@Override
	public void handleMessage(JanusHandler handler, JanusMessage message) {
		if (!attachMessage.getTransaction().equals(message.getTransaction())) {
			throw new IllegalStateException("Transactions do not match");
		}

		if (message instanceof JanusSessionSuccessMessage) {
			JanusSessionSuccessMessage success = (JanusSessionSuccessMessage) message;

			handler.setPluginId(success.getId());
			handler.setState(new CreateRoomState(handler.getSessionId(), handler.getPluginId()));
		}
	}
}
