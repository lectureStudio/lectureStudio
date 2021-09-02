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

import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;

/**
 * Listens for PageEdited events.
 *
 * @author Alex Andres
 */
public interface ShapeListener {

	/**
	 * Fired when shape has been painted.
	 *
	 * @param event The shape paint event.
	 */
	void shapePainted(ShapePaintEvent event);

	/**
	 * Fired when shape has been modified.
	 *
	 * @param event The shape modify event.
	 */
	void shapeModified(ShapeModifyEvent event);

}
