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

/**
 * Event types used to classify messages when communicating with the Janus
 * WebRTC Server.
 *
 * @author Alex Andres
 */
public enum JanusEventType {

	/**
	 * To create a new session with the server.
	 */
	CREATE,

	/**
	 * If the request is successful.
	 */
	SUCCESS,

	/**
	 * To create a new handle to attach to a plugin.
	 */
	ATTACH,

	/**
	 * To detach from a plugin and destroy the plugin handle.
	 */
	DETACH,

	/**
	 * To destroy the current session.
	 */
	DESTROY,

	/**
	 * If the server failed to process the request.
	 */
	ERROR,

	/**
	 * For everything that is related to the communication with a plugin.
	 */
	MESSAGE,

	/**
	 * To keep a session alive.
	 */
	KEEP_ALIVE,


	// WebRTC-related events.

	/**
	 * ICE and DTLS succeeded. A PeerConnection has been correctly established
	 * with the user/application.
	 */
	WEBRTC_UP,

	/**
	 * Whether Janus is receiving audio/video on the PeerConnection.
	 */
	MEDIA,

	/**
	 * Whether Janus is reporting trouble sending/receiving media on the
	 * PeerConnection.
	 */
	SLOW_LINK,

	/**
	 * The PeerConnection was closed, either by Janus or by the
	 * user/application, and as such cannot be used anymore.
	 */
	HANGUP,

}
