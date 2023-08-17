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
 * Event message received when an publisher has left the room.
 *
 * @author Alex Andres
 */
public class JanusRoomPublisherLeftMessage extends JanusRoomMessage {

	private final BigInteger roomId;

	private final BigInteger publisherId;


	/**
	 * Create a new {@code JanusRoomPublisherLeftMessage}.
	 *
	 * @param sessionId   The unique integer session ID.
	 * @param roomId      The unique numeric room ID.
	 * @param publisherId The unique publisher ID who left the room.
	 */
	public JanusRoomPublisherLeftMessage(BigInteger sessionId,
			BigInteger roomId, BigInteger publisherId) {
		super(sessionId);

		this.roomId = roomId;
		this.publisherId = publisherId;
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
	 * Get the unique publisher ID who left the room.
	 *
	 * @return The publisher ID.
	 */
	public BigInteger getPublisherId() {
		return publisherId;
	}
}
