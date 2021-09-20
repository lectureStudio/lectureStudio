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

package org.lecturestudio.web.api.message;

/**
 * Message used to notify the arrival or departure of participants during a
 * streaming session.
 *
 * @author Alex Andres
 */
public class CourseParticipantMessage {

	private boolean connected;


	/**
	 * Get whether the participant connected or disconnected to/from the current
	 * streaming session.
	 *
	 * @return True if connected.
	 */
	public boolean getConnected() {
		return connected;
	}

	/**
	 * Set whether the participant connected or disconnected to/from the current
	 * streaming session.
	 *
	 * @param connected True if connected.
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
