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

import org.lecturestudio.core.crypto.Authority;
import org.lecturestudio.core.crypto.GroupParameters;

/**
 * The {@link SrpClientContext} handles client-side computing and storing of SRP-6a values.
 *
 * @author Alex Andres
 */
public class SrpClientContext extends SrpContext {

	/** The Authority. */
	private final Authority authority;

	/** Secret ephemeral value. */
	private BigInteger a;


	/**
	 * Creates a new {@link SrpClientContext} for specified identity and negotiated group parameters.
	 *
	 * @param authority The authority.
	 * @param params    The {@link GroupParameters}, N and g.
	 *
	 * @throws Exception On initialization errors.
	 */
	public SrpClientContext(Authority authority, GroupParameters params) throws Exception {
		super(authority.getIdentity(), params);

		this.authority = authority;
	}

	/**
	 * Get client's Authority.
	 */
	public Authority getAuthority() {
		return authority;
	}

	/**
	 * Computes and returns the public value A = g^a.
	 *
	 * @return the public value.
	 */
	public BigInteger computePublicValue() {
		this.a = generatePrivateValue();
		this.A = params.getGenerator().modPow(a, params.getPrime());

		return A;
	}

	/**
	 * Computes the shared, strong session key K = H(S). This method invokes the
	 * computation of several required parameters such as:
	 * <li>u = H(A, B)</li>
	 * <li>x = H(s, p)</li>
	 * <li>S = (B - kg^x) ^ (a + ux)</li> <br>
	 * <br>
	 * If u = 0, then a {@link SrpAgreementException} is thrown. <br>
	 * <br>
	 *
	 * @param B The public value of the server.
	 * @param s The user's salt.
	 *
	 * @throws SrpAgreementException If the random scrambling parameter is
	 *                               zero.
	 */
	public void computeSessionKey(BigInteger B, BigInteger s) throws SrpAgreementException {
		BigInteger u = computeU(A, B);

		if (u.equals(BigInteger.ZERO)) {
			throw new SrpAgreementException("Agreement violation: u == 0.");
		}

		BigInteger x = computePrivateKey(s, authority.getPassword());
		BigInteger k = getMultiplier();

		BigInteger exp = u.multiply(x).add(a);
		BigInteger tmp = params.getGenerator().modPow(x, params.getPrime())
				.multiply(k).mod(params.getPrime());

		BigInteger S = B.subtract(tmp).mod(params.getPrime()).modPow(exp, params.getPrime());

		this.B = B;
		this.s = s;
		this.K = computeK(S);
		this.M = computeEvidenceMessage();
	}

	/**
	 * Checks if the session key verifier of the server matches own verifier.
	 *
	 * @param hAMK The session key verifier of the server.
	 *
	 * @return True if own session key verifier matches the provided one, false otherwise.
	 */
	public boolean verifySessionKey(BigInteger hAMK) {
		BigInteger ownHAMK = computeSessionKeyVerifier();

		return ownHAMK.equals(hAMK);
	}

	/**
	 * Computes the secret private key x = H(s, p).
	 *
	 * @param s        The user's salt.
	 * @param password The user's password.
	 *
	 * @return The secret private key.
	 */
	private BigInteger computePrivateKey(BigInteger s, String password) {
		digest.update(s.toByteArray());
		digest.update(password.getBytes());

		return new BigInteger(1, digest.digest());
	}

}
