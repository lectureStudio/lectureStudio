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

import java.math.BigInteger;

/**
 * Success message returned by the Janus WebRTC Server. This message is returned
 * upon a successful session creation or an attachment to a plugin.
 *
 * @author Alex Andres
 */
public class JanusSessionSuccessMessage extends JanusMessage {

	private final BigInteger id;


	/**
	 * Create a new {@code JanusSessionSuccessMessage} with the specified
	 * parameters.
	 *
	 * @param id The unique integer session or plugin handle ID.
	 */
	public JanusSessionSuccessMessage(BigInteger id) {
		setEventType(JanusMessageType.SUCCESS);

		this.id = id;
	}

	/**
	 * Get the unique session or plugin handle ID.
	 *
	 * @return The unique integer session or plugin handle ID.
	 */
	public BigInteger getId() {
		return id;
	}
}
