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

package org.lecturestudio.presenter.api.model;

import org.lecturestudio.core.model.Page;

/**
 * Represents a bookmark within a presentation.
 *
 * @param shortcut The keyboard shortcut or identifier for the bookmark
 * @param page     The page that is bookmarked
 *
 * @author Alex Andres
 */
public record Bookmark(String shortcut, Page page) {

	/**
	 * Creates a bookmark with the given shortcut and no associated page.
	 *
	 * @param shortcut The keyboard shortcut or identifier for the bookmark.
	 */
	public Bookmark(String shortcut) {
		this(shortcut, null);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		final Bookmark other = (Bookmark) obj;

		return shortcut != null && shortcut.equalsIgnoreCase(other.shortcut());
	}

	@Override
	public String toString() {
		return "Bookmark{" +
				"shortcut='" + shortcut + '\'' +
				", page=" + page +
				'}';
	}
}
