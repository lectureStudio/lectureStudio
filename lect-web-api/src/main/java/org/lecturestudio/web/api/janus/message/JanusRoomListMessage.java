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

import org.lecturestudio.web.api.janus.JanusRoom;

/**
 * Response message contains all the available rooms.
 *
 * @author Alex Andres
 */
public class JanusRoomListMessage extends JanusRoomMessage {

	private final List<JanusRoom> rooms;


	/**
	 * Create a new {@code JanusRoomListMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param rooms     The list of all available rooms.
	 */
	public JanusRoomListMessage(BigInteger sessionId, List<JanusRoom> rooms) {
		super(sessionId);

		this.rooms = rooms;
	}

	/**
	 * Get a list of all available rooms.
	 *
	 * @return All available rooms.
	 */
	public List<JanusRoom> getRooms() {
		return rooms;
	}
}
