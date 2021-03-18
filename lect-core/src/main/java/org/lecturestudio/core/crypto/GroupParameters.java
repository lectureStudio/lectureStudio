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

package org.lecturestudio.core.crypto;

import java.math.BigInteger;

/**
 * The class {@code GroupParameters} consists of the fields safe prime (N) and
 * the corresponding generator (g). {@code N} is a prime of the form N=2q+1,
 * where q is also prime. The generator is a member of the group (1 to N-1)
 * under multiplication % N.
 *
 * @author Alex Andres
 */
public class GroupParameters {

	/** The safe prime 'N'. */
	private final BigInteger N;

	/** The corresponding generator 'g'. */
	private final BigInteger g;


	/**
	 * Creates new instance of {@link GroupParameters} with provided prime and
	 * the corresponding generator.
	 *
	 * @param N The safe prime.
	 * @param g The corresponding generator.
	 */
	public GroupParameters(BigInteger N, BigInteger g) {
		if (N == null) {
			throw new NullPointerException("The safe prime 'N' must not be null.");
		}

		if (g == null) {
			throw new NullPointerException("The generator 'g' must not be null.");
		}

		this.N = N;
		this.g = g;
	}

	/**
	 * Returns the safe prime 'N'.
	 *
	 * @return 'N'
	 */
	public BigInteger getPrime() {
		return N;
	}

	/**
	 * Returns the generator 'g'.
	 *
	 * @return 'g'
	 */
	public BigInteger getGenerator() {
		return g;
	}

}
