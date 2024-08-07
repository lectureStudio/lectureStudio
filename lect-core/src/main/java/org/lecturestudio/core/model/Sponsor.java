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

import java.util.Objects;

public class Sponsor {

	/** The name of the organization. */
	public String organization;

	/** The path of the logo. */
	public String logo;

	/** The link for the organization. */
	public Link link;


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Sponsor sponsor = (Sponsor) o;

		return Objects.equals(organization, sponsor.organization)
				&& Objects.equals(logo, sponsor.logo)
				&& Objects.equals(link, sponsor.link);
	}

	@Override
	public int hashCode() {
		return Objects.hash(organization, logo, link);
	}



	public static class Link {

		public String name;

		public String url;

	}
}
