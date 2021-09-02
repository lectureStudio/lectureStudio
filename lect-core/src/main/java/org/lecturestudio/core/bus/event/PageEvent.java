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

public class PageEvent extends BusEvent {

	/** Enum with the {@link PageEvent} types. */
	public enum Type { CREATED, REMOVED, SELECTED };

	/** the old {@link Page}. */
	private final Page oldPage;

	/** the {@link Page}. */
	private final Page page;

	/** the {@link Type} of the {@link PageEvent}. */
	private final Type type;

	/**
	 * Create the {@link PageEvent} with specified page and type ({@link #oldPage} will be {@code null}).
	 *
	 * @param page The page.
	 * @param type The type of the {@link PageEvent}.
	 */
	public PageEvent(Page page, Type type) {
		this(page, null, type);
	}

	/**
	 * Create the {@link PageEvent} with specified page, old page and type.
	 *
	 * @param page The page.
	 * @param oldPage The old page.
	 * @param type The type of the {@link PageEvent}.
	 */
	public PageEvent(Page page, Page oldPage, Type type) {
		this.page = page;
		this.oldPage = oldPage;
		this.type = type;
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
	 * Get the old page.
	 *
	 * @return The old page.
	 */
	public Page getOldPage() {
		return oldPage;
	}

	/**
	 * Get the type of the {@link PageEvent}.
	 *
	 * @return The type of the {@link PageEvent}.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Indicates whether the {@link PageEvent} is created.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.CREATED}, otherwise {@code false}.
	 */
	public boolean isCreated() {
		return type == Type.CREATED;
	}

	/**
	 * Indicates whether the {@link PageEvent} is removed.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.REMOVED}, otherwise {@code false}.
	 */
	public boolean isRemoved() {
		return type == Type.REMOVED;
	}

	/**
	 * Indicates whether the {@link PageEvent} is selected.
	 *
	 * @return {@code true} if the {@link #type} equals {@code Type.SELECTED}, otherwise {@code false}.
	 */
	public boolean isSelected() {
		return type == Type.SELECTED;
	}
	
}
