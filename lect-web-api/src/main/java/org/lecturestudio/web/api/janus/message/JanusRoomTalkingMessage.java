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
 * This message is sent to a video room notifying participants that a certain
 * participant is talking or stopped talking.
 *
 * @author Alex Andres
 */
public class JanusRoomTalkingMessage extends JanusRoomMessage {

	private final BigInteger roomId;

	private final BigInteger peerId;


	/**
	 * Create a new {@code JanusRoomStateMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param roomId    The unique numeric room ID.
	 * @param peerId    The ID of the peer who is talking or stopped talking.
	 */
	public JanusRoomTalkingMessage(BigInteger sessionId, BigInteger roomId,
			BigInteger peerId) {
		super(sessionId);

		this.roomId = roomId;
		this.peerId = peerId;

		setRoomEventType(JanusRoomEventType.TALKING);
	}

	/**
	 * Get the unique numeric room ID, optional, chosen by plugin if missing.
	 *
	 * @return The unique numeric room ID.
	 */
	public BigInteger getRoomId() {
		return roomId;
	}

	/**
	 * Get the unique numeric participant ID who triggered this event message.
	 *
	 * @return The ID of the peer who is talking or stopped talking.
	 */
	public BigInteger getPeerId() {
		return peerId;
	}
}
