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
 * Response message used when a new video room has been successfully created.
 *
 * @author Alex Andres
 */
public class JanusRoomCreatedMessage extends JanusRoomMessage {

	private final BigInteger roomId;

	private final Boolean permanent;


	/**
	 * Create a new {@code JanusRoomCreatedMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param roomId    The unique numeric room ID.
	 * @param permanent True if saved to config file, false if not.
	 */
	public JanusRoomCreatedMessage(BigInteger sessionId, BigInteger roomId, Boolean permanent) {
		super(sessionId);

		this.roomId = roomId;
		this.permanent = permanent;

		setRoomEventType(JanusRoomEventType.CREATED);
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
	 * Checks if the created room has been permanently saved to the config file
	 * on the server side.
	 *
	 * @return True if saved to config file, false if not.
	 */
	public Boolean getPermanent() {
		return permanent;
	}
}
