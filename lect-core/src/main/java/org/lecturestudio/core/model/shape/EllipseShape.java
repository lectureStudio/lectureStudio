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

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.tool.Stroke;

/**
 * A shape representing an ellipse.
 * 
 * @author Alex Andres
 */
public class EllipseShape extends FormShape {

	/**
	 * Creates a new {@link EllipseShape} with the specified stroke.
	 * (Calls {@link FormShape#FormShape(Stroke)} with the stroke.)
	 *
	 * @param stroke The stroke.
	 */
    public EllipseShape(Stroke stroke) {
    	super(stroke);
    }

	/**
	 * Creates a new {@link EllipseShape} with the specified input byte array containing the data for a stroke.
	 *
	 * @param input The input byte array.
	 */
    public EllipseShape(byte[] input) throws IOException {
    	super(null);

		parseFrom(input);
	}

    @Override
	public boolean contains(Point2D p) {
		double delta = getStroke().getWidth() / 2;
		List<PenPoint2D> points = getPoints();

		// Handle simple cases.
		if (points.isEmpty()) {
			return false;
		}

		Rectangle2D bounds = getBounds();
		Ellipse2D.Double ellipse = new Ellipse2D.Double(bounds.getX() - delta, bounds.getY() - delta, bounds.getWidth(), bounds.getHeight());

		if (fill()) {
			return ellipse.contains(p.getX(), p.getY());
		}

		PathIterator pi = ellipse.getPathIterator(null);
		CubicCurve2D.Double curve = new CubicCurve2D.Double();

		double[] coords = new double[6];
		double x1 = 0;
		double y1 = 0;

		double x = p.getX() - delta;
		double y = p.getY() - delta;
		double w = delta * 2;
		double h = delta * 2;

		while (!pi.isDone()) {
			int segment = pi.currentSegment(coords);

			switch (segment) {
				case PathIterator.SEG_MOVETO:
					x1 = coords[0];
					y1 = coords[1];
					break;

				case PathIterator.SEG_CUBICTO:
					curve.setCurve(x1, y1, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);

					x1 = coords[4];
					y1 = coords[5];

					// Subdivide 3 times for better precision.
					if (intersects(curve, 0, 3, x, y, w, h)) {
						return true;
					}
					break;

				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CLOSE:
					// Ignore
					break;
			}
			pi.next();
		}

		return false;
	}

    @Override
	public boolean intersects(Rectangle2D rect) {
    	double delta = getStroke().getWidth() / 2;
    	Rectangle2D bounds = getBounds();
		Ellipse2D.Double ellipse = new Ellipse2D.Double(bounds.getX() - delta, bounds.getY() - delta, bounds.getWidth(), bounds.getHeight());

		return ellipse.intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

	@Override
	public EllipseShape clone() {
		EllipseShape shape = new EllipseShape(getStroke().clone());
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

	private boolean intersects(CubicCurve2D curve, int index, int steps, double x, double y, double w, double h) {
		// Recursive anchor.
		if (index >= steps) {
			CubicCurve2D.Double left = new CubicCurve2D.Double();
			CubicCurve2D.Double right = new CubicCurve2D.Double();

			curve.subdivide(left, right);

			return left.intersects(x, y, w, h) || right.intersects(x, y, w, h);
		}

		for (int i = index; i < steps; i++) {
			CubicCurve2D.Double left = new CubicCurve2D.Double();
			CubicCurve2D.Double right = new CubicCurve2D.Double();

			curve.subdivide(left, right);

			if (intersects(left, i + 1, steps, x, y, w, h) || intersects(right, i + 1, steps, x, y, w, h)) {
				return true;
			}
		}
		return false;
	}

}
