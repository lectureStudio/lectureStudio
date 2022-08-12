package org.lecturestudio.web.api.stream.model;

import java.util.Objects;

public class CourseParticipant {

	private CourseParticipantType participantType;

	private CoursePresenceType presenceType;

	private String id;

	private String firstName;

	private String familyName;


	public CourseParticipant(String id, String firstName, String familyName,
			CoursePresenceType presenceType, CourseParticipantType type) {
		this.id = id;
		this.firstName = firstName;
		this.familyName = familyName;
		this.presenceType = presenceType;
		this.participantType = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "[CourseParticipant] " + id;
	}
}
