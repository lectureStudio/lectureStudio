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

import java.math.BigInteger;

/**
 * The {@code ServerChallengeMessage} is used in response on the identity lookup
 * request of the client. This message carries the public value of the server
 * and the user's salt.
 * 
 * @author Alex Andres
 */
public class SrpServerChallengeMessage extends SrpMessage {

	/** The public value of the server. */
	private final BigInteger publicValue;

	/** User's salt. */
	private final BigInteger salt;


	/**
	 * Creates a new SrpServerChallengeMessage.
	 */
	public SrpServerChallengeMessage() {
		this(null, null);
	}

	/**
	 * Creates a new SrpServerChallengeMessage that contains the public value of
	 * the server and the user's salt.
	 *
	 * @param B The public value.
	 * @param s User's salt.
	 */
	public SrpServerChallengeMessage(BigInteger B, BigInteger s) {
		this.publicValue = B;
		this.salt = s;

		setMessageCode(SrpMessageCode.SERVER_CHALLENGE);
	}

	/**
	 * Returns the public value of the server.
	 */
	public BigInteger getPublicValue() {
		return publicValue;
	}

	/**
	 * Returns the user's salt.
	 */
	public BigInteger getSalt() {
		return salt;
	}

}
