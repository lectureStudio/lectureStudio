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

package org.lecturestudio.core.input;

public interface ScrollHandler {

	/**
	 * An event dispatched when the mouse wheel was rotated in a component.
	 *
	 * @param x      The horizontal x coordinate with respect to the scene/window component.
	 * @param y      The vertical y coordinate with respect to the scene/window component.
	 * @param deltaX The horizontal scroll amount.
	 * @param deltaY The vertical scroll amount.
	 */
	record ScrollEvent(double x, double y, double deltaX, double deltaY) {

	}


	/**
	 * Invoked when scrolling has occurred.
	 *
	 * @param e The event to be processed.
	 *
	 * @see ScrollEvent
	 */
	void onScrollEvent(ScrollEvent e);

}
