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

package org.lecturestudio.web.api.model;

import java.util.Objects;

public class ScreenSource {

	private final String title;

	private final long id;

	private final boolean isWindow;


	public ScreenSource(String title, long id, boolean isWindow) {
		this.title = title;
		this.id = id;
		this.isWindow = isWindow;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public boolean isWindow() {
		return isWindow;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ScreenSource that = (ScreenSource) o;

		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}