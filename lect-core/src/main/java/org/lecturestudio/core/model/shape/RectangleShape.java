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

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Iterator;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.tool.Stroke;

/**
 * A shape representing a rectangle.
 *
 * @author Alex Andres
 */
public class RectangleShape extends FormShape {

	/**
	 * Creates a new {@link RectangleShape} with the specified stroke.
	 * (Calls {@link FormShape#FormShape(Stroke)} with the stroke.)
	 *
	 * @param stroke The stroke.
	 */
	public RectangleShape(Stroke stroke) {
		super(stroke);
	}

	/**
	 * Creates a new {@link RectangleShape} with the specified input byte array containing the data for a stroke.
	 *
	 * @param input The input byte array.
	 */
	public RectangleShape(byte[] input) throws IOException {
		super(null);

		parseFrom(input);
	}

	@Override
	public boolean contains(Point2D p) {
		double delta = getStroke().getWidth();

		Rectangle2D bounds = getBounds();

		if (fill()) {
			return bounds.contains(p);
		}

		double x1 = bounds.getX();
		double y1 = bounds.getY();
		double x2 = bounds.getX() + bounds.getWidth();
		double y2 = bounds.getY();

		if (intersects(delta, x1, y1, x2, y2, p)) {
			return true;
		}

		x1 = bounds.getX() + bounds.getWidth();
		y1 = bounds.getY();
		x2 = bounds.getX() + bounds.getWidth();
		y2 = bounds.getY() + bounds.getHeight();

		if (intersects(delta, x1, y1, x2, y2, p)) {
			return true;
		}

		x1 = bounds.getX() + bounds.getWidth();
		y1 = bounds.getY() + bounds.getHeight();
		x2 = bounds.getX();
		y2 = bounds.getY() + bounds.getHeight();

		if (intersects(delta, x1, y1, x2, y2, p)) {
			return true;
		}

		x1 = bounds.getX();
		y1 = bounds.getY();
		x2 = bounds.getX();
		y2 = bounds.getY() + bounds.getHeight();

		return intersects(delta, x1, y1, x2, y2, p);
	}

	@Override
	public boolean intersects(Rectangle2D rect) {
		Rectangle2D bounds = getBounds();
		java.awt.geom.Rectangle2D r = new java.awt.geom.Rectangle2D.Double(
				bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

		return r.intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
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
	public RectangleShape clone() {
		RectangleShape shape = new RectangleShape(getStroke().clone());
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
