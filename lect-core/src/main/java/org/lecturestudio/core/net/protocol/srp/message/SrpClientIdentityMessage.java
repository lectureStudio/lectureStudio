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
 * The {@code ClientIdentityMessage} is used to initiate the
 * password-authenticated key agreement. This message carries user's identity
 * and the public value of the client.
 * 
 * @author Alex Andres
 */
public class SrpClientIdentityMessage extends SrpMessage {

	/** User's identity, e.g. user name, e-mail address etc. */
	private final String identity;

	/** The public value of the client. */
	private final BigInteger publicValue;


	/**
	 * Creates a new SrpClientIdentityMessage.
	 */
	public SrpClientIdentityMessage() {
		this(null, null);
	}

	/**
	 * Creates a new SrpClientIdentityMessage that contains user's identity and
	 * the public value of the client.
	 *
	 * @param identity User's identity, e.g. user name, e-mail address etc.
	 * @param A        The public value.
	 */
	public SrpClientIdentityMessage(String identity, BigInteger A) {
		this.identity = identity;
		this.publicValue = A;

		setMessageCode(SrpMessageCode.CLIENT_IDENTITY);
	}

	/**
	 * Returns user's identity, e.g. user name, e-mail address etc.
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Returns the public value of the client.
	 */
	public BigInteger getPublicValue() {
		return publicValue;
	}

}
