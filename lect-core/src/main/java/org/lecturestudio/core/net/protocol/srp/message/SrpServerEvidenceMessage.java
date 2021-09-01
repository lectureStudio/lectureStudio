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
 * The {@link SrpServerEvidenceMessage} is used to provide the evidence that the
 * shared secret key of the server matches the clients shared secret key.
 * This message carries the evidence of the server.
 * 
 * @author Alex Andres
 */
public class SrpServerEvidenceMessage extends SrpEvidenceMessage {

	/**
	 * Creates a new {@link SrpServerEvidenceMessage}.
	 */
	public SrpServerEvidenceMessage() {
		this(null);
	}

	/**
	 * Creates a new {@link SrpServerEvidenceMessage} that contains the evidence of the server.
	 *
	 * @param M The evidence.
	 */
	public SrpServerEvidenceMessage(BigInteger M) {
		super(M);
		setMessageCode(SrpMessageCode.SERVER_EVIDENCE);
	}

}
