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

package org.lecturestudio.core.tool;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.Shape;

public class ShapePaintEvent implements ToolEvent {

	private final ToolEventType type;

	private final Shape shape;

	private final Rectangle2D clipRect;


	public ShapePaintEvent(ToolEventType type, Shape shape, Rectangle2D clipRect) {
		this.type = type;
		this.shape = shape;
		this.clipRect = clipRect;
	}

	public ToolEventType getType() {
		return type;
	}

	public Shape getShape() {
		return shape;
	}

	public Rectangle2D getClipRect() {
		return clipRect;
	}
}
