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

package org.lecturestudio.core.net.protocol.ptip;

/**
 * Enumeration of PTIP message codes. Each message has it's own message code that identifies the message.
 * 
 * @author Alex Andres
 * 
 */
public enum PTIPMessageCode {

	SESSION_DESCRIPTION(1),
	FILE_CHUNK(2),
	CHANGE_DOCUMENT(3),
	AUTH_REQUEST(4),
	PAGE(5);


	/**
	 * Unique number that represents the message type.
	 */
	private final int type;


	/**
	 * Creates a new {@link PTIPMessageCode} with the specified message type.
	 * 
	 * @param type The message type.
	 */
	PTIPMessageCode(int type) {
		this.type = type;
	}

	/**
	 * Returns the associated number that represents the message type.
	 * 
	 * @return The message type.
	 */
	public int getID() {
		return type;
	}

}
