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

import org.lecturestudio.core.model.ParticipantType;
import org.lecturestudio.web.api.stream.model.CoursePresence;

/**
 * Message used to notify the arrival or departure of participants during a
 * streaming session.
 *
 * @author Alex Andres
 */
public class CoursePresenceMessage extends WebMessage {

	private CoursePresence presence;

	private ParticipantType participantType;


	/**
	 * Get whether the participant connected or disconnected to/from the current
	 * streaming session.
	 *
	 * @return True if connected.
	 */
	public CoursePresence getCoursePresence() {
		return presence;
	}

	/**
	 * Set whether the participant connected or disconnected to/from the current
	 * streaming session.
	 *
	 * @param presence True if connected.
	 */
	public void setCoursePresence(CoursePresence presence) {
		this.presence = presence;
	}

	/**
	 * Get the type of the participant who has issued this presence event.
	 *
	 * @return The participant type.
	 */
	public ParticipantType getCourseParticipantType() {
		return participantType;
	}

	/**
	 * Set the type of the participant who has issued this presence event.
	 *
	 * @param type The participant type.
	 */
	public void setCourseParticipantType(ParticipantType type) {
		this.participantType = type;
	}
}
