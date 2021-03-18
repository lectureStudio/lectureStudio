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

import java.util.Objects;

public class Bookmark {

	private final String shortcut;

	private final Page page;


    public Bookmark(String shortcut) {
        this(shortcut, null);
    }

	public Bookmark(String shortcut, Page page) {
		this.shortcut = shortcut;
		this.page = page;
	}

	public String getShortcut() {
		return shortcut;
	}

	public Page getPage() {
		return page;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		final Bookmark other = (Bookmark) obj;

		if (shortcut == null || !shortcut.equalsIgnoreCase(other.getShortcut())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(shortcut, page);
	}

}
