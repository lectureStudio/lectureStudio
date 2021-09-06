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
 * Room request to start subscribing media from a room.
 *
 * @author Alex Andres
 */
public class JanusRoomSubscribeRequest extends JanusRoomRequest {

	protected BigInteger room;


	/**
	 * Create a new {@code JanusRoomSubscribeRequest}.
	 */
	public JanusRoomSubscribeRequest() {
		setRequestType(JanusRoomRequestType.START);
	}

	/**
	 * Get the unique numeric room ID, optional, chosen by plugin if missing.
	 *
	 * @return The unique numeric room ID.
	 */
	public BigInteger getRoom() {
		return room;
	}

	/**
	 * Set the unique numeric room ID. Optional, chosen by plugin if missing.
	 *
	 * @param room the unique room ID.
	 */
	public void setRoom(BigInteger room) {
		this.room = room;
	}
}
