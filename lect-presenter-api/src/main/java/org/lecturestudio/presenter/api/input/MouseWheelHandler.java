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

package org.lecturestudio.presenter.api.input;

public interface MouseWheelHandler {

	/**
	 * An event dispatched when the mouse wheel was rotated in a component.
	 *
	 * @param x             The horizontal x coordinate for the mouse location.
	 * @param y             The vertical y coordinate for the mouse location.
	 * @param wheelRotation The integer number of "clicks" by which the mouse wheel was rotated.
	 */
	record MouseWheelEvent(int x, int y, int wheelRotation) {

	}


	/**
	 * Invoked when the mouse wheel is rotated.
	 *
	 * @param e the event to be processed.
	 *
	 * @see MouseWheelEvent
	 */
	void mouseWheelMoved(MouseWheelEvent e);

}
