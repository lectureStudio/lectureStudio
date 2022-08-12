package org.lecturestudio.core.model;

import java.util.Objects;

//import lombok.AllArgsConstructor;
//import lombok.Data;

//@Data
//@AllArgsConstructor
public class Participant {

	private String id;

	private String firstName;

	private String familyName;


	public Participant(String id, String firstName, String familyName) {
		this.id = id;
		this.firstName = firstName;
		this.familyName = familyName;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Participant that = (Participant) o;

		return Objects.equals(id, that.id)
				&& Objects.equals(firstName, that.firstName)
				&& Objects.equals(familyName, that.familyName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, firstName, familyName);
	}
}
