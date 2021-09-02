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

package org.lecturestudio.core.net.protocol;

/**
 * The class {@link DecodeMessageException} should be used by packet decoders to report errors while decoding messages.
 * 
 * @author Alex Andres
 * 
 */
public class DecodeMessageException extends Exception {

	private static final long serialVersionUID = -3240524528408571103L;


	/**
	 * Creates a new {@link DecodeMessageException} with {@code null} as its detail message.
	 */
	public DecodeMessageException() {
		super();
	}

	/**
	 * Creates a new {@link DecodeMessageException} with the specified detail message.
	 * 
	 * @param message The detail message.
	 */
	public DecodeMessageException(String message) {
		super(message);
	}

}
