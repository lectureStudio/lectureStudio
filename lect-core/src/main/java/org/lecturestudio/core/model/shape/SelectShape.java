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

package org.lecturestudio.core.model.shape;

import java.io.IOException;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.tool.Stroke;

/**
 * A shape representing the "select rectangle" while using a select tool. It is
 * removed after the select operation has ended.
 *
 * @author Alex Andres
 */
public class SelectShape extends RectangleShape {

    public SelectShape(Stroke stroke) {
    	super(stroke);
    }

    public SelectShape(byte[] input) throws IOException {
		super(input);
    }

	@Override
	public SelectShape clone() {
		SelectShape shape = new SelectShape(getStroke().clone());
		shape.setHandle(getHandle());
		shape.setKeyEvent(getKeyEvent());

		for (PenPoint2D point : getPoints()) {
			shape.addPoint(point.clone());
		}

		return shape;
	}

}
