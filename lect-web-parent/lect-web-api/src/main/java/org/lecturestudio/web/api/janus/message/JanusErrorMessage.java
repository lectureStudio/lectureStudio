/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.janus.message;

/**
 * Error message returned by the Janus WebRTC Server.
 *
 * @author Alex Andres
 */
public class JanusErrorMessage extends JanusMessage {

	private final int code;

	private final String reason;


	/**
	 * Create a new {@code JanusErrorMessage} with the specified parameters.
	 *
	 * @param code   The numeric error code.
	 * @param reason The string describing the cause of the failure.
	 */
	public JanusErrorMessage(int code, String reason) {
		setEventType(JanusEventType.ERROR);

		this.code = code;
		this.reason = reason;
	}

	/**
	 * Get the numeric error code.
	 *
	 * @return The numeric error code.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get the verbose string describing the cause of the failure.
	 *
	 * @return The string describing the cause of the failure.
	 */
	public String getReason() {
		return reason;
	}
}
