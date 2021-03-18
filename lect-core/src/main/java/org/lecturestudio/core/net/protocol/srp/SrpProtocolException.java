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

/**
 * The class {@code SRPProtocolException} is to be used during the
 * authentication process. This exception is thrown if inconsistencies occur
 * between the client and server while authenticating.
 * 
 * @author Alex Andres
 */
public class SrpProtocolException extends Exception {

	private static final long serialVersionUID = -9113059457478171856L;

	/**
	 * Constructs a new protocol exception with {@code null} as its detail
	 * message.
	 */
	public SrpProtocolException() {
		super();
	}

	/**
	 * Constructs a new protocol exception with the specified detail message.
	 *
	 * @param message the detail message which is received by the {@link
	 *                #getMessage()} method.
	 */
	public SrpProtocolException(String message) {
		super(message);
	}

}
