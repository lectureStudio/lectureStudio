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

import static java.util.Objects.nonNull;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.Iterator;

import org.lecturestudio.core.geometry.PathFactory;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.tool.ArrowTool;
import org.lecturestudio.core.tool.Stroke;

/**
 * A shape representing an arrow while using the {@link ArrowTool}.
 *
 * @author Alex Andres
 */
public class ArrowShape extends LineShape {

	public ArrowShape(Stroke stroke) {
		super(stroke);
	}

	public ArrowShape(byte[] input) throws IOException {
		super(input);
	}

	public boolean isTwoSided() {
		KeyEvent keyEvent = getKeyEvent();
		return nonNull(keyEvent) && keyEvent.isShiftDown();
	}

	@Override
	public boolean contains(Point2D p) {
		return getShape().contains(p.getX(), p.getY());
	}

	@Override
	public boolean intersects(Rectangle2D rect) {
		return getShape().intersects(rect.getX(), rect.getY(), rect.getWidth(),
				rect.getHeight());
	}

	@Override
	public ArrowShape clone() {
		ArrowShape shape = new ArrowShape(getStroke().clone());
		shape.setHandle(getHandle());
		shape.setKeyEvent(getKeyEvent());

		Iterator<PenPoint2D> points = getPoints().iterator();

		if (points.hasNext()) {
			shape.setStartPoint(points.next().clone());
		}

		while (points.hasNext()) {
			shape.setEndPoint(points.next().clone());
		}

		return shape;
	}

	private java.awt.Shape getShape() {
		KeyEvent keyEvent = getKeyEvent();
		double width = getLineWidth();

		AffineTransform tx = new AffineTransform();
		Path2D path = PathFactory.createArrowPath(tx, keyEvent,
				getStartPoint(), getEndPoint(), width);

		return tx.createTransformedShape(path);
	}

}
