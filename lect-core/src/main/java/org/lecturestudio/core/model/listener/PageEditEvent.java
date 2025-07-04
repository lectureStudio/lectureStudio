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

package org.lecturestudio.core.model.listener;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;

public class PageEditEvent {

	/** The possible types of page edit events. */
	public enum Type { SHAPES_ADDED, SHAPE_ADDED, SHAPE_REMOVED, SHAPE_CHANGE, CLEAR }

	private final Rectangle2D dirtyArea;
	
	private final Page page;
	
	private final Shape shape;
	
	private final Type type;


	/**
	 * Create a {@link PageEditEvent} with the specified page, shape, dirty area and type.
	 *
	 * @param page The page.
	 * @param shape The shape.
	 * @param dirtyArea The dirty area.
	 * @param type The type.
	 */
	public PageEditEvent(Page page, Shape shape, Rectangle2D dirtyArea, Type type) {
		this.page = page;
		this.shape = shape;
		this.dirtyArea = dirtyArea;
		this.type = type;
	}

	/**
	 * Get the dirty area.
	 *
	 * @return The dirty area.
	 */
	public Rectangle2D getDirtyArea() {
		return dirtyArea;
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
	 * Get the shape.
	 *
	 * @return The shape.
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Get the type.
	 *
	 * @return The type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Specifies whether the shape changed.
	 *
	 * @return {@code true} if {@link #type} equals {@code Type.SHAPE_CHANGE}, otherwise {@code false}.
	 */
	public boolean shapeChanged() {
		return type == Type.SHAPE_CHANGE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((page == null) ? 0 : page.hashCode());
		result = prime * result + ((shape == null) ? 0 : shape.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		PageEditEvent other = (PageEditEvent) obj;
		
		if (page == null) {
			if (other.page != null) {
				return false;
			}
		}
		else if (!page.equals(other.page)) {
			return false;
		}
		if (shape == null) {
			if (other.shape != null) {
				return false;
			}
		}
		else if (!shape.equals(other.shape)) {
			return false;
		}

		return type == other.type;
	}
	
}
