/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.net.protocol.bcast.message;

/**
 * Enumeration of message codes which are used to interact with the broadcaster.
 * Each message has it's own message code that identifies the message.
 * 
 * @author Alex Andres
 */
public enum BcastMessageCode {

	/** Request to start a new Session. */
	SESSION_REQUEST(100),

	/** Acknowledge the last Session request. Resources have been reserved. */
	SESSION_REQUEST_ACK(101),

	/** Authentication request. */
	AUTH_REQUEST(102);


	/** Unique number that represents the message type. */
	private final int type;


	/**
	 * Creates a new {@link BcastMessageCode} with the specified message type.
	 * 
	 * @param type The message type.
	 */
	BcastMessageCode(int type) {
		this.type = type;
	}

	/**
	 * Returns the associated number that represents the message type.
	 * 
	 * @return The message type.
	 */
	public int getId() {
		return type;
	}

}
