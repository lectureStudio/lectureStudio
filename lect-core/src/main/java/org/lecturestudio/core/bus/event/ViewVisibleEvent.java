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

import org.lecturestudio.core.view.View;

public class ViewVisibleEvent {

	/** The view class. */
	private final Class<? extends View> viewClass;

	/** The visible truth value. */
	private final boolean visible;


	/**
	 * Create the {@link ViewVisibleEvent} with specified view class and visible truth value.
	 *
	 * @param viewClass The view class.
	 * @param visible The visible truth value.
	 */
	public ViewVisibleEvent(Class<? extends View> viewClass, boolean visible) {
		this.viewClass = viewClass;
		this.visible = visible;
	}

	/**
	 * Get the view class.
	 *
	 * @return The view class.
	 */
	public Class<? extends View> getViewClass() {
		return viewClass;
	}

	/**
	 * Get the visible truth value.
	 *
	 * @return The visible truth value.
	 */
	public boolean isVisible() {
		return visible;
	}
}
