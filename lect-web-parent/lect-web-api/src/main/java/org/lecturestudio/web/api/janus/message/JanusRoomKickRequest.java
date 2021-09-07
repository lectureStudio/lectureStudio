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

import java.math.BigInteger;

/**
 * Kick participant request.
 *
 * @author Alex Andres
 */
public class JanusRoomKickRequest extends JanusRoomRequest {

	private BigInteger room;

	private BigInteger id;

	private String secret;


	/**
	 * Create a new {@code JanusRoomKickRequest}.
	 */
	public JanusRoomKickRequest() {
		setRequestType(JanusRoomRequestType.KICK);
	}

	/**
	 * Set the unique ID of the room from which to kick the participant.
	 *
	 * @param roomId The unique room ID.
	 */
	public void setRoomId(BigInteger roomId) {
		room = roomId;
	}

	/**
	 * Set the unique ID of the participant to kick.
	 *
	 * @param id The unique participant ID.
	 */
	public void setParticipantId(BigInteger id) {
		this.id = id;
	}

	/**
	 * Get the secret required to destroy the room.
	 *
	 * @return The required secret to destroy the room.
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Set the secret required to destroy the room. This setting is mandatory if
	 * configured.
	 *
	 * @param secret The required secret to destroy the room.
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
}
