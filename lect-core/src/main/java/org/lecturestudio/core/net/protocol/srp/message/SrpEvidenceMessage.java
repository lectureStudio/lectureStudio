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
 * Abstract message class that carries an SRP evidence.
 * 
 * @author Alex Andres
 */
public abstract class SrpEvidenceMessage extends SrpMessage {

	/** The evidence to proof the shared key. */
	private final BigInteger evidence;


	/**
	 * Creates a new {@link SrpEvidenceMessage} that contains the evidence.
	 *
	 * @param M The evidence.
	 */
	public SrpEvidenceMessage(BigInteger M) {
		this.evidence = M;
	}

	/**
	 * Returns the evidence.
	 */
	public BigInteger getEvidence() {
		return evidence;
	}

}
