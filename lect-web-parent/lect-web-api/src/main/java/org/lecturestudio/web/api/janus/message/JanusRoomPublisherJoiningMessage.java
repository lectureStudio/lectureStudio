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

import org.lecturestudio.web.api.janus.JanusPublisher;

/**
 * Event message received when a new publisher is joining the video room. A
 * joining publisher is a non-active publisher who does not send media to the
 * video room.
 *
 * @author Alex Andres
 */
public class JanusRoomPublisherJoiningMessage extends JanusRoomPublisherJoinMessage {

	/**
	 * Create a new {@code JanusRoomPublisherJoiningMessage}.
	 *
	 * @param sessionId The unique integer session ID.
	 * @param roomId    The unique numeric room ID.
	 * @param publisher The new publisher who joined the room.
	 */
	public JanusRoomPublisherJoiningMessage(BigInteger sessionId,
			BigInteger roomId, JanusPublisher publisher) {
		super(sessionId, roomId, publisher);
	}
}
