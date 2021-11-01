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

package org.lecturestudio.web.api.janus;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.message.JanusPluginMessage;
import org.lecturestudio.web.api.janus.state.AttachPluginState;
import org.lecturestudio.web.api.janus.state.SubscriberJoinRoomState;

public class JanusSubscriberHandler extends JanusStateHandler {

	private final JanusPublisher publisher;


	public JanusSubscriberHandler(JanusPublisher publisher,
			JanusPeerConnectionFactory factory,
			JanusMessageTransmitter transmitter) {
		super(factory, transmitter);

		this.publisher = publisher;
	}

	public JanusPublisher getPublisher() {
		return publisher;
	}

	@Override
	public <T extends JanusMessage> void handleMessage(T message) throws Exception {
		if (message instanceof JanusPluginMessage) {
			JanusPluginMessage pluginMessage = (JanusPluginMessage) message;

			// Accept only messages that are addressed to this handler.
			if (!pluginMessage.getHandleId().equals(getPluginId())) {
				return;
			}
		}

		super.handleMessage(message);
	}

	@Override
	public JanusPeerConnection createPeerConnection() {
		JanusPeerConnection peerConnection = super.createPeerConnection();

		peerConnection.setOnIceConnectionState(state -> {
			switch (state) {
				case CONNECTED:
					setConnected();
					break;

				case DISCONNECTED:
				case CLOSED:
					setDisconnected();
					break;
			}
		});

		return peerConnection;
	}

	@Override
	protected void initInternal() throws ExecutableException {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		setState(new AttachPluginState(new SubscriberJoinRoomState(publisher)));
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (nonNull(peerConnection)) {
			peerConnection.close();
			peerConnection = null;
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}
}
