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

package org.lecturestudio.web.api.stream.model;

import java.util.Objects;
import java.util.StringJoiner;

public class Course {

	private Long id;

	private String roomId;

	private String title;

	private String description;


	public Long getId() {
		return id;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Course)) {
			return false;
		}

		Course course = (Course) o;

		return Objects.equals(id, course.id)
				&& Objects.equals(roomId, course.roomId)
				&& Objects.equals(title, course.title)
				&& Objects.equals(description, course.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, roomId, title, description);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Course.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("title='" + title + "'")
				.add("description='" + description + "'")
				.toString();
	}
}
