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
import java.security.MessageDigest;
import java.security.SecureRandom;

import org.lecturestudio.core.crypto.GroupParameters;

/**
 * {@link SrpFactory} class that creates all necessary SRP parameters and classes.
 * 
 * @author Alex Andres
 */
public abstract class SrpFactory {

	/** Strong random number generator. */
	public static final SecureRandom random = new SecureRandom();


	/**
	 * Creates a new triple {identity, verifier, salt}. The salt is generated
	 * randomly and the verifier is computed as follows: <li>x = H(s, p)</li>
	 * <li>v = g^x</li> <br>
	 * <br>
	 *
	 * @param digest   The secure one-way hash function.
	 * @param params   The group parameters N and g.
	 * @param identity The user's identity 'I', e.g. user name, e-mail address etc.
	 * @param password The user's password.
	 *
	 * @return a new triple {identity, verifier, salt} represented by {@link SrpUserEntry}.
	 */
	public static SrpUserEntry createUserEntry(MessageDigest digest, GroupParameters params, String identity, String password) {
		BigInteger s = generateRandomNumber();

		digest.update(s.toByteArray());
		digest.update(password.getBytes());

		BigInteger x = new BigInteger(1, digest.digest());
		BigInteger v = params.getGenerator().modPow(x, params.getPrime());

		return new SrpUserEntry(identity, s, v);
	}

	/**
	 * Generates a random number with specified byte length.
	 *
	 * @param bytes The count of bytes the number must have.
	 *
	 * @return The random number as {@link BigInteger}.
	 */
	public static BigInteger generateRandomNumber(int bytes) {
		return new BigInteger(bytes * 8, random);
	}

	/**
	 * Generates a random 16-byte number.
	 *
	 * @return The random number as {@link BigInteger}.
	 */
	public static BigInteger generateRandomNumber() {
		return generateRandomNumber(16);
	}

}
