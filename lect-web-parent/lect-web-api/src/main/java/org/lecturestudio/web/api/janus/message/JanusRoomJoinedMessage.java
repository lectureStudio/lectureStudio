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
import java.util.List;

import org.lecturestudio.web.api.janus.JanusPublisher;

/**
 * Response message received when a video room has been successfully joined.
 *
 * @author Alex Andres
 */
public class JanusRoomJoinedMessage extends JanusRoomMessage {

	private final BigInteger roomId;

	private final BigInteger id;

	private final BigInteger privateId;

	private final String description;

	private final List<JanusPublisher> publishers;


	/**
	 * Create a new {@code JanusRoomJoinedMessage}.
	 *
	 * @param sessionId   The unique integer session ID.
	 * @param roomId      The unique numeric room ID.
	 * @param id          The unique ID of the participant.
	 * @param privateId   The unique private ID of the participant.
	 * @param description The the description of the room.
	 * @param publishers  The list of all active publishers.
	 */
	public JanusRoomJoinedMessage(BigInteger sessionId, BigInteger roomId,
			BigInteger id, BigInteger privateId, String description,
			List<JanusPublisher> publishers) {
		super(sessionId);

		this.roomId = roomId;
		this.id = id;
		this.privateId = privateId;
		this.description = description;
		this.publishers = publishers;
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
	 * Get the unique ID of the participant.
	 *
	 * @return The unique ID of the participant.
	 */
	public BigInteger getId() {
		return id;
	}

	/**
	 * Get the unique private ID associated to the participant.
	 *
	 * @return The unique private ID.
	 */
	public BigInteger getPrivateId() {
		return privateId;
	}

	/**
	 * Get the description of the room, if available.
	 *
	 * @return The description of the room.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get a list of all active publishers.
	 *
	 * @return All active publishers.
	 */
	public List<JanusPublisher> getPublishers() {
		return publishers;
	}
}
