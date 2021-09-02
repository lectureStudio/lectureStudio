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

import org.lecturestudio.core.crypto.GroupParameters;

/**
 * The {@link SrpServerContext} handles server-side computing and storing of SRP-6a values.
 *
 * @author Alex Andres
 */
public class SrpServerContext extends SrpContext {

	/** Password verifier. */
	private BigInteger v;

	/** Secret ephemeral value. */
	private BigInteger b;


	/**
	 * Creates a new {@link SrpServerContext} for specified user and negotiated group parameters.
	 *
	 * @param entry  The triple {identity, verifier, salt} as {@link SrpUserEntry}.
	 * @param params The {@link GroupParameters}, N and g.
	 *
	 * @throws Exception If the hash function is not supported.
	 */
	public SrpServerContext(SrpUserEntry entry, GroupParameters params) throws Exception {
		super(entry.getIdentity(), params);

		this.v = entry.getVerifier();
		this.s = entry.getSalt();
	}

	/**
	 * Computes and returns the public value B = kv + g^b.
	 *
	 * @return The public value.
	 */
	public BigInteger computePublicValue() {
		BigInteger k = getMultiplier();

		this.b = generatePrivateValue();
		this.B = params.getGenerator().modPow(b, params.getPrime())
				.add(k.multiply(v)).mod(params.getPrime());

		return B;
	}

	/**
	 * Computes the shared, strong session key K = H(S). This method invokes the
	 * computation of several required parameters such as:
	 * <li>u = H(A, B)</li>
	 * <li>S = (Av^u) ^ b</li>
	 * <br><br>
	 *
	 * @param A The public value of the client.
	 */
	public void computeSessionKey(BigInteger A) {
		BigInteger u = computeU(A, B);
		BigInteger S = v.modPow(u, params.getPrime()).multiply(A).modPow(b, params.getPrime());

		this.A = A;
		this.K = computeK(S);
		this.M = computeEvidenceMessage();
	}

	/**
	 * Checks if the session key verifier of the client matches own verifier.
	 *
	 * @param M The session key verifier of the client.
	 *
	 * @return {@code true} if own session key verifier matches the provided one, otherwise {@code false}.
	 */
	public boolean verifySessionKey(BigInteger M) {
		BigInteger ownEvidence = computeEvidenceMessage();

		return ownEvidence.equals(M);
	}

}
