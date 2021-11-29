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

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.web.api.janus.message.JanusMessage;
import org.lecturestudio.web.api.janus.state.JanusState;
import org.lecturestudio.web.api.stream.StreamContext;

public abstract class JanusStateHandler extends ExecutableBase {

	private final List<JanusStateHandlerListener> listeners;

	protected final JanusPeerConnectionFactory peerConnectionFactory;

	protected final JanusMessageTransmitter transmitter;

	protected JanusState state;

	protected JanusInfo info;

	protected JanusPeerConnection peerConnection;

	protected BigInteger sessionId;

	protected BigInteger pluginId;

	protected BigInteger roomId;

	protected String roomSecret;


	public JanusStateHandler(JanusPeerConnectionFactory factory,
			JanusMessageTransmitter transmitter) {
		this.listeners = new ArrayList<>();
		this.peerConnectionFactory = factory;
		this.transmitter = transmitter;
	}

	public void addJanusStateHandlerListener(JanusStateHandlerListener listener) {
		listeners.add(listener);
	}

	public void setState(JanusState state) {
		requireNonNull(state);

		this.state = state;

		try {
			state.initialize(this);
		}
		catch (Exception e) {
			logException(e, "Initialize state failed");
		}
	}

	public BigInteger getSessionId() {
		return sessionId;
	}

	public void setSessionId(BigInteger id) {
		requireNonNull(id);

		sessionId = id;
	}

	public BigInteger getPluginId() {
		return pluginId;
	}

	public void setPluginId(BigInteger id) {
		requireNonNull(id);

		pluginId = id;
	}

	public BigInteger getRoomId() {
		return roomId;
	}

	public void setRoomId(BigInteger id) {
		requireNonNull(id);

		roomId = id;
	}

	public String getRoomSecret() {
		return roomSecret;
	}

	public void setRoomSecret(String secret) {
		requireNonNull(secret);

		roomSecret = secret;
	}

	public void setInfo(JanusInfo info) {
		requireNonNull(info);

		this.info = info;
	}

	public <T extends JanusMessage> void handleMessage(T message) throws Exception {
		requireNonNull(state);

		state.handleMessage(this, message);
	}

	public void sendMessage(JanusMessage message) {
		transmitter.sendMessage(message);
	}

	public JanusPeerConnection createPeerConnection() {
		peerConnection = new JanusPeerConnection(peerConnectionFactory);

		return peerConnection;
	}

	public JanusPeerConnection getPeerConnection() {
		return peerConnection;
	}

	public StreamContext getStreamContext() {
		return peerConnectionFactory.getStreamContext();
	}

	protected void setConnected() {
		for (JanusStateHandlerListener listener : listeners) {
			listener.connected();
		}
	}

	protected void setDisconnected() {
		for (JanusStateHandlerListener listener : listeners) {
			listener.disconnected();
		}
	}

	protected void setError(Throwable throwable) {
		for (JanusStateHandlerListener listener : listeners) {
			listener.error(throwable);
		}
	}
}
