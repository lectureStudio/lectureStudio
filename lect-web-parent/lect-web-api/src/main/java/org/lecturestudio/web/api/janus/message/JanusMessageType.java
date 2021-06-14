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

package org.lecturestudio.web.api.janus.message;

import java.util.NoSuchElementException;

/**
 * Event types used to classify messages when communicating with the Janus
 * WebRTC Server.
 *
 * @author Alex Andres
 */
public enum JanusMessageType {

	/**
	 * Event related to messages being sent from plugins.
	 */
	EVENT("event"),

	/**
	 * To get generic info from the server.
	 */
	INFO("info"),

	/**
	 * Info response from the server.
	 */
	SERVER_INFO("server_info"),

	/**
	 * To create a new session with the server.
	 */
	CREATE("create"),

	/**
	 * If the request is successful.
	 */
	SUCCESS("success"),

	/**
	 * If an asynchronous request has been accepted.
	 */
	ACK("ack"),

	/**
	 * To create a new handle to attach to a plugin.
	 */
	ATTACH("attach"),

	/**
	 * To detach from a plugin and destroy the plugin handle.
	 */
	DETACH("detach"),

	/**
	 * Occurs when e.g. a session was destroyed due to inactivity.
	 */
	TIMEOUT("timeout"),

	/**
	 * To destroy the current session.
	 */
	DESTROY("destroy"),

	/**
	 * If the server failed to process the request.
	 */
	ERROR("error"),

	/**
	 * For everything that is related to the communication with a plugin.
	 */
	MESSAGE("message"),

	/**
	 * To keep a session alive.
	 */
	KEEP_ALIVE("keepalive"),


	// WebRTC-related events.

	/**
	 * To trickle ICE candidates.
	 */
	TRICKLE("trickle"),

	/**
	 * ICE and DTLS succeeded. A PeerConnection has been correctly established
	 * with the user/application.
	 */
	WEBRTC_UP("webrtcup"),

	/**
	 * Whether Janus is receiving audio/video on the PeerConnection.
	 */
	MEDIA("media"),

	/**
	 * Whether Janus is reporting trouble sending/receiving media on the
	 * PeerConnection.
	 */
	SLOW_LINK("slowlink"),

	/**
	 * The PeerConnection was closed, either by Janus or by the
	 * user/application, and as such cannot be used anymore.
	 */
	HANGUP("hangup");


	private final String type;


	JanusMessageType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static JanusMessageType fromString(String typeStr) {
		for (var value : JanusMessageType.values()) {
			if (value.getType().equals(typeStr)) {
				return value;
			}
		}

		throw new NoSuchElementException();
	}
}
