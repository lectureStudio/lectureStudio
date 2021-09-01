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

package org.lecturestudio.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contributor {

	/** The name of the contributor. */
	public String name;

	/** The firm of the contributor. */
	public String firm;

	/** a list of contributions. */
	public List<String> contributions = new ArrayList<>();


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Contributor other = (Contributor) o;

		return Objects.equals(name, other.name) && Objects
				.equals(firm, other.firm) && Objects
				.equals(contributions, other.contributions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, firm, contributions);
	}

	@Override
	public String toString() {
		return "Contributor{" + "name='" + name + '\'' + ", firm='" + firm
				+ '\'' + ", contributions=" + contributions + '}';
	}
}
