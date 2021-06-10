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

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.tool.Stroke;

/**
 * A shape representing a line.
 * 
 * @author Alex Andres
 */
public class LineShape extends FormShape {

	/**
	 * Creates a new {@link LineShape} with the specified stroke.
	 * (Calls {@link FormShape#FormShape(Stroke)} with the stroke.)
	 *
	 * @param stroke The stroke.
	 */
    public LineShape(Stroke stroke) {
    	super(stroke);
    }

	/**
	 * Creates a new {@link LineShape} with the specified input byte array containing the data for a stroke.
	 *
	 * @param input The input byte array.
	 */
	public LineShape(byte[] input) throws IOException {
		super(null);

		parseFrom(input);
	}

	/**
	 * Get the width of the line.
	 *
	 * @return The width of the stroke (times two if the line is bold).
	 */
	public double getLineWidth() {
		return isBold() ? getStroke().getWidth() * 2 : getStroke().getWidth();
	}

	/**
	 * Return true if Alt is pressed (and line should be bold).
	 *
	 * @return {@code true} if Shift is pressed, otherwise {@code false}.
	 */
	public boolean isBold() {
		KeyEvent keyEvent = getKeyEvent();
		return nonNull(keyEvent) && keyEvent.isAltDown();
	}

	@Override
	public boolean contains(Point2D p) {
		double delta = getStroke().getWidth();
		List<PenPoint2D> points = getPoints();

		// Handle simple cases.
		if (points.isEmpty()) {
			return false;
		}

		PenPoint2D p1 = getStartPoint();
		PenPoint2D p2 = getEndPoint();

		return intersects(delta, p1.getX(), p1.getY(), p2.getX(), p2.getY(), p);
	}

	@Override
	public boolean intersects(Rectangle2D rect) {
		PenPoint2D p1 = getStartPoint();
		PenPoint2D p2 = getEndPoint();

		Line2D line = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());

		return line.intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	private boolean intersects(double delta, double x1, double y1, double x2, double y2, Point2D p) {
		double x3 = p.getX() + delta;
		double y3 = p.getY() + delta;
		double x4 = p.getX() - delta;
		double y4 = p.getY() - delta;

		if (Line2D.Double.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
			return true;
		}

		x3 = p.getX() - delta;
		y3 = p.getY() + delta;
		x4 = p.getX() + delta;
		y4 = p.getY() - delta;

		return Line2D.Double.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	@Override
	public LineShape clone() {
		LineShape shape = new LineShape(getStroke().clone());
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

}
