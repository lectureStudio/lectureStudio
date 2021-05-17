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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.lecturestudio.core.crypto.GroupParameters;

/**
 * {@link SrpContext} is the base class for client and server-side Secure Remote
 * Password Protocol (SRP-6a) authentication.
 * 
 * @author Alex Andres
 */
public abstract class SrpContext {

	/** The version of SRP. */
	public static final String VERSION = "6a";

	/** Secure one-way hash function. */
	protected final MessageDigest digest;

	/** Strong random number generator. */
	protected final SecureRandom random;

	/** Group parameters N and g. */
	protected final GroupParameters params;

	/** User's identity 'I', e.g. user name, e-mail address etc. */
	protected String identity;

	/** User's salt. */
	protected BigInteger s;

	/** Clients public ephemeral value. */
	protected BigInteger A;

	/** Servers public ephemeral value. */
	protected BigInteger B;

	/** The shared, strong session key K. */
	protected BigInteger K;

	/** The evidence message. */
	protected BigInteger M;


	/**
	 * Computes and returns the public value of the implemented context.
	 *
	 * @return The public value.
	 */
	abstract public BigInteger computePublicValue();

	/**
	 * Check if the session key verifier of the counter part matches own verifier.
	 *
	 * @param M The session key verifier of the counter part.
	 *
	 * @return True if own session key verifier matches the provided one, false otherwise.
	 */
	abstract public boolean verifySessionKey(BigInteger M);


	/**
	 * Creates a new {@link SrpContext} with user's identity, desired group
	 * parameters and the default hash function SHA-1.
	 *
	 * @param identity The user's identity.
	 * @param params   The GroupParameters, N and g.
	 *
	 * @throws NoSuchAlgorithmException If the hash function is not supported.
	 */
	public SrpContext(String identity, GroupParameters params) throws NoSuchAlgorithmException {
		this(identity, params, "SHA-1");
	}

	/**
	 * Creates a new {@link SrpContext} with user's identity, desired group parameters and hash function.
	 *
	 * @param identity     The user's identity.
	 * @param params       The GroupParameters, N and g.
	 * @param hashFunction The desired hash function to be used during agreement.
	 *
	 * @throws NoSuchAlgorithmException If the hash function is not supported.
	 */
	public SrpContext(String identity, GroupParameters params, String hashFunction) throws NoSuchAlgorithmException {
		this.identity = identity;
		this.params = params;

		this.digest = MessageDigest.getInstance(hashFunction);
		this.random = new SecureRandom();
	}

	/**
	 * Returns user's identity, e.g. the user name.
	 * 
	 * @return User's identity.
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Get user's salt.
	 *
	 * @return The salt value.
	 */
	public BigInteger getSalt() {
		return s;
	}

	/**
	 * Returns the secret session key 'K'.
	 * 
	 * @return The secret session key.
	 */
	public BigInteger getSessionKey() {
		return K;
	}

	/**
	 * Checks if the public value % N is zero or not.
	 * 
	 * @param value The public value.
	 * 
	 * @return True if the public value % N != 0, false otherwise.
	 */
	public boolean verifyPublicValue(BigInteger value) {
		return !value.mod(params.getPrime()).equals(BigInteger.ZERO);
	}

	/**
	 * Computes the evidence message M = H(H(N) xor H(g), H(I), s, A, B, K)
	 * which is required to prove that the session keys of both parties match.
	 * 
	 * @return The evidence message.
	 */
	public BigInteger computeEvidenceMessage() {
		byte[] hashN = digest.digest(params.getPrime().toByteArray());
		byte[] hashG = digest.digest(params.getGenerator().toByteArray());
		byte[] hashI = digest.digest(identity.getBytes());

		BigInteger hN = new BigInteger(hashN);
		BigInteger hG = new BigInteger(hashG);

		digest.update(hN.xor(hG).toByteArray());
		digest.update(hashI);
		digest.update(s.toByteArray());
		digest.update(A.toByteArray());
		digest.update(B.toByteArray());
		digest.update(K.toByteArray());

		return new BigInteger(digest.digest());
	}

	/**
	 * Computes the session key verifier H(A, M, K) which is required to prove
	 * that the session keys of both parties match.
	 * 
	 * @return The session key verifier.
	 */
	public BigInteger computeSessionKeyVerifier() {
		digest.update(A.toByteArray());
		digest.update(M.toByteArray());
		digest.update(K.toByteArray());

		return new BigInteger(1, digest.digest());
	}

	/**
	 * Generates a random ephemeral private value with constraints 1 < value < N.
	 * 
	 * @return The ephemeral private value.
	 */
	BigInteger generatePrivateValue() {
		BigInteger value = SrpFactory.generateRandomNumber(32);
		BigInteger two = BigInteger.valueOf(2);
		BigInteger N = params.getPrime();

		if (value.compareTo(N) >= 0) {
			value = value.mod(N.subtract(BigInteger.ONE));
		}
		if (value.compareTo(two) < 0) {
			value = two;
		}

		return value;
	}

	/**
	 * Computes the random scrambling parameter u = H(A, B).
	 * 
	 * @param A The clients public value.
	 * @param B The servers public value.
	 */
	BigInteger computeU(BigInteger A, BigInteger B) {
		digest.update(A.toByteArray());
		digest.update(B.toByteArray());

		return new BigInteger(1, digest.digest());
	}

	/**
	 * Returns the multiplier parameter k = H(N, g).
	 * 
	 * @return The multiplier parameter.
	 */
	BigInteger getMultiplier() {
		digest.update(params.getPrime().toByteArray());
		digest.update(params.getGenerator().toByteArray());

		return new BigInteger(1, digest.digest());
	}

	/**
	 * Computes the session key K = H(S).
	 * 
	 * @param S The session key.
	 * 
	 * @return The hashed session key.
	 */
	BigInteger computeK(BigInteger S) {
		return new BigInteger(1, digest.digest(S.toByteArray()));
	}

}
