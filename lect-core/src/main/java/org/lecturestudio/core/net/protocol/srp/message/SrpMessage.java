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

import org.lecturestudio.core.crypto.srp.SrpContext;

/**
 * SrpMessage is a base class to provide common methods for a SRP message.
 * A SrpMessage carries SRP parameters over packet-swiched networks.
 * 
 * @author Alex Andres
 */
public abstract class SrpMessage {

	/** The message identifier. */
	private SrpMessageCode messageCode;


	/**
	 * Get the SRP version string.
	 */
	public String getVersion() {
		return SrpContext.VERSION;
	}

	/**
	 * Returns one of the message codes that are defined in {@link
	 * SrpMessageCode}. Each SrpMessage has it's own message code.
	 *
	 * @return the SrpMessageCode.
	 */
	public SrpMessageCode getMessageCode() {
		return messageCode;
	}

	/**
	 * Set the new SrpMessageCode.
	 *
	 * @param messageCode The new SrpMessageCode.
	 */
	public void setMessageCode(SrpMessageCode messageCode) {
		this.messageCode = messageCode;
	}

}
