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

package org.lecturestudio.core.net.protocol.srp;

import org.lecturestudio.core.net.protocol.srp.message.SrpMessage;
import org.lecturestudio.core.crypto.srp.SrpAgreementException;

/**
 * The interface {@code SRPMessageHandler} defines methods that should be
 * implemented by a server and client to enable communication between them.
 * 
 * @author Alex Andres
 */
public interface SrpMessageHandler {

	/**
	 * Processes a {@code SrpMessage} that is used for the password-authenticated
	 * key agreement.
	 * 
	 * @param message The SRP-Message to process.
	 * 
	 * @throws SrpAgreementException on errors during the authentication.
	 */
	void processSrpMessage(SrpMessage message) throws SrpAgreementException;

	/**
	 * Returns the next {@code SrpMessage} in the stack that should be transmitted
	 * to the counterpart.
	 * 
	 * @return the SrpMessage to transmit.
	 */
	SrpMessage getNextOutgoingMessage();

	/**
	 * Checks whether the authentication is completed.
	 * 
	 * @return true if authenticated, false otherwise.
	 */
	boolean isAuthenticated();

}
