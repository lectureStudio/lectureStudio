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

import org.lecturestudio.web.api.janus.JanusStateHandler;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginAttachMessage;
import org.lecturestudio.web.api.janus.message.JanusSessionSuccessMessage;

/**
 * This state binds a Janus server plugin to a Janus session. This is the second
 * mandatory step for the session establishment with the Janus WebRTC server. By
 * default, this state attaches to the Janus video-room plugin.
 *
 * @author Alex Andres
 */
public class AttachPluginState implements JanusState {

	private final static String PLUGIN = "janus.plugin.videoroom";

	private final JanusState nextState;

	private JanusMessage attachRequest;


	public AttachPluginState(JanusState nextState) {
		this.nextState = nextState;
	}

	@Override
	public void initialize(JanusStateHandler handler) {
		attachRequest = new JanusPluginAttachMessage(handler.getSessionId(), PLUGIN);
		attachRequest.setTransaction(UUID.randomUUID().toString());

		handler.sendMessage(attachRequest);
	}

	@Override
	public void handleMessage(JanusStateHandler handler, JanusMessage message) {
//		checkTransaction(attachRequest, message);

		if (message instanceof JanusSessionSuccessMessage) {
			JanusSessionSuccessMessage success = (JanusSessionSuccessMessage) message;

			logDebug("Janus plugin handle created: %d", success.getSessionId());

			handler.setPluginId(success.getSessionId());
			handler.setState(nextState);
		}
	}
}
