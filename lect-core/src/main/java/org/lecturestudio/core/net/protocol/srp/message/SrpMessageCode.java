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

package org.lecturestudio.core.net.protocol.srp.message;

/**
 * Enumeration of message codes which are used during SRP authentication.
 * Each message has it's own message code that identifies the message and it's content.
 * 
 * @author Alex Andres
 */
public enum SrpMessageCode {

	/** Identity e.g. user name or e-mail. */
	CLIENT_IDENTITY(200),

	/** Servers public value 'B' and user's random salt 's'. */
	SERVER_CHALLENGE(201),

	/** Clients evidence to proof the equality of the shared key. */
	CLIENT_EVIDENCE(202),

	/** Servers evidence to proof the equality of the shared key. */
	SERVER_EVIDENCE(203),

	/** Clients or Servers error message. */
	ERROR(204);


	/** Unique number that represents the message type. */
	private final int type;


	/**
	 * Creates a new {@link SrpMessageCode} with the specified message type.
	 *
	 * @param type The message type.
	 */
	SrpMessageCode(int type) {
		this.type = type;
	}

	/**
	 * Get the associated number that represents the message type.
	 *
	 * @return The message type.
	 */
	public int getId() {
		return type;
	}

}
