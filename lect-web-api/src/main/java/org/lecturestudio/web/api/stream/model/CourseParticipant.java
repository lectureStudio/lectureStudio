/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.model;

import java.util.Objects;

public class CourseParticipant {

	private CourseParticipantType participantType;

	private CoursePresenceType presenceType;

	private String userId;

	private String firstName;

	private String familyName;


	public CourseParticipant() {

	}

	public CourseParticipant(String userId, String firstName, String familyName,
			CoursePresenceType presenceType, CourseParticipantType type) {
		this.userId = userId;
		this.firstName = firstName;
		this.familyName = familyName;
		this.presenceType = presenceType;
		this.participantType = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public CourseParticipantType getParticipantType() {
		return participantType;
	}

	public void setParticipantType(CourseParticipantType type) {
		this.participantType = type;
	}

	public CoursePresenceType getPresenceType() {
		return presenceType;
	}

	public void setPresenceType(CoursePresenceType type) {
		this.presenceType = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CourseParticipant that = (CourseParticipant) o;

		return Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	@Override
	public String toString() {
		return "CourseParticipant{"
				+ "participantType=" + participantType
				+ ", presenceType=" + presenceType
				+ ", userId='" + userId + '\''
				+ ", firstName='" + firstName + '\''
				+ ", familyName='" + familyName + '\'' + '}';
	}
}
