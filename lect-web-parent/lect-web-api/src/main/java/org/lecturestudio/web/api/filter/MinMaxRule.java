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

package org.lecturestudio.web.api.filter;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;

//@Entity
public class MinMaxRule implements InputFieldRule<String>, Serializable {

	private static final long serialVersionUID = 827184938853275192L;

	private final int min;

	private final int max;

	/** Bind input field to this rule. */
	private final int fieldId;


	public MinMaxRule(int min, int max, int fieldId) {
		this.min = min;
		this.max = max;
		this.fieldId = fieldId;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getFieldId() {
		return fieldId;
	}

	@Override
	public boolean isAllowed(String value) {
		int number;

		try {
			number = Integer.parseInt(value);
		}
		catch (Exception e) {
			return false;
		}

		return (number >= min && number <= max);
	}

	@Override
	public FilterRule<String> clone() {
		return new MinMaxRule(min, max, fieldId);
	}

	@Override
	public boolean isAllowed(String value, int fieldId) {
		// skip and "allow" for wrong fields
		if (this.fieldId != fieldId)
			return true;

		int number;

		try {
			number = Integer.parseInt(value);
		}
		catch (Exception e) {
			return false;
		}

		return (number >= min && number <= max);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldId, min, max);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		MinMaxRule other = (MinMaxRule) obj;

		boolean a = Objects.equals(fieldId, other.fieldId);
		boolean b = Objects.equals(min, other.min);
		boolean c = Objects.equals(max, other.max);

		return a && b && c;
	}

	@Override
	public String toString() {
		return fieldId + ": [" + min + "," + max + "]";
	}

}
