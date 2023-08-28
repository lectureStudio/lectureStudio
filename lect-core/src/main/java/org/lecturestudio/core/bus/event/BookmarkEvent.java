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
package org.lecturestudio.core.bus.event;

import org.lecturestudio.core.model.Page;

public class BookmarkEvent extends BusEvent {

	/** Enum with the {@link BookmarkEvent} types. */
	public enum Type { CREATED, REMOVED};

	/** the {@link Type} of the {@link BookmarkEvent}. */
	private final Type type;

	private final Page page;

	/**
	 * Create the {@link BookmarkEvent} with specified page and type.
	 *
	 * @param page The page.
	 * @param type The type of the {@link BookmarkEvent}.
	 */
	public BookmarkEvent(Page page, BookmarkEvent.Type type) {
		this.page = page;
		this.type = type;
	}

	/**
	 * Get the type of the {@link BookmarkEvent}.
	 *
	 * @return The type of the {@link BookmarkEvent}.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Get the page.
	 *
	 * @return The page.
	 */

	public Page getPage() {
		return page;
	}
	/**
	 * Indicates whether the {@link BookmarkEvent} is created.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.CREATED}, otherwise {@code false}.
	 */
	public boolean isCreated() {
		return type == Type.CREATED;
	}

	/**
	 * Indicates whether the {@link BookmarkEvent} is removed.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.REMOVED}, otherwise {@code false}.
	 */
	public boolean isRemoved() {
		return type == Type.REMOVED;
	}

}
