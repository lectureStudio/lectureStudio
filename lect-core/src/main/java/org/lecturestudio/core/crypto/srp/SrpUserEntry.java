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

package org.lecturestudio.core.crypto.srp;

import java.math.BigInteger;

/**
 * The class {@code SRPUserEntry} represents the triple {identity, verifier,
 * salt} which can be stored in a database. The triples are only accessed by the
 * server that performs a lookup for a identity.
 *
 * @author Alex Andres
 */
public class SrpUserEntry {

	/** User's identity 'I', e.g. user name, e-mail address etc. */
	private final String identity;

	/** User's salt. */
	private final BigInteger salt;

	/** Password verifier. */
	private final BigInteger verifier;

	/**
	 * Creates a new {@link SrpUserEntry} with specified parameters.
	 *
	 * @param identity The user's identity 'I', e.g. user name, e-mail address
	 *                 etc.
	 * @param salt     The user's salt.
	 * @param verifier The corresponding password verifier.
	 */
	public SrpUserEntry(String identity, BigInteger salt, BigInteger verifier) {
		this.identity = identity;
		this.salt = salt;
		this.verifier = verifier;
	}

	/**
	 * Returns the identity of the user.
	 *
	 * @return user's identity.
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Returns user's salt.
	 *
	 * @return the salt.
	 */
	public BigInteger getSalt() {
		return salt;
	}

	/**
	 * Returns the password verifier.
	 *
	 * @return the password verifier.
	 */
	public BigInteger getVerifier() {
		return verifier;
	}

}
