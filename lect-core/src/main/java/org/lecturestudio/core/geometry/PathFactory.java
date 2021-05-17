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

package org.lecturestudio.core.geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.input.KeyEvent;

/**
 * The {@link PathFactory} creates renderable shapes represented by a {@link Path2D} from a list of points.
 *
 * @author Alex Andres
 */
public final class PathFactory {

	/**
	 * Create a variable width pen stroke from a list of points with the specified pen width.
	 *
	 * @param points   The list of points captured by a input device.
	 * @param penWidth The pen width.
	 *
	 * @return A variable width pen stroke {@link Path2D}.
	 */
	public static Path2D createPenPath(List<PenPoint2D> points, double penWidth) {
		PenStroker stroker = new PenStroker(penWidth);

		return stroker.createPath(points);
	}

	/**
	 * Create a highlighter stroke from a list of points with the specified pen width.
	 *
	 * @param points The list of points captured by a input device.
	 * @param width  The width of the highlighter stroke.
	 *
	 * @return A highlighter stroke as {@link Path2D}.
	 */
	public static Path2D createHighlighterPath(List<PenPoint2D> points, double width) {
		GeneralPath path = new GeneralPath();

		Iterator<PenPoint2D> it = points.iterator();
		Point2D p1;
		Point2D p2;
		Point2D p3;

		if (it.hasNext()) {
			p1 = it.next();
			path.moveTo(p1.getX(), p1.getY());
		}

		while (it.hasNext()) {
			p1 = it.next();

			if (it.hasNext()) {
				p2 = it.next();

				if (it.hasNext()) {
					p3 = it.next();
					path.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
				}
				else {
					path.quadTo(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				}
			}
			else {
				path.lineTo(p1.getX(), p1.getY());
			}
		}

		return path;
	}

	/**
	 * Create an arrow path with the specified transform from a start point to
	 * the end point. The end point will have an arrow pointed towards it. The
	 * key event modifies the thickness and the double arrow attributes.
	 *
	 * @param tx       The transform of the arrow, e.g. may be rotated.
	 * @param keyEvent The key event to modify the thickness and the double  arrow attributes.
	 * @param p1       The start point of the arrow.
	 * @param p2       The end point of the arrow.
	 * @param penWidth The width of the arrow.
	 *
	 * @return An arrow stroke as {@link Path2D}.
	 */
	public static Path2D createArrowPath(AffineTransform tx, KeyEvent keyEvent, PenPoint2D p1, PenPoint2D p2, double penWidth) {
		boolean bold = keyEvent != null && keyEvent.isAltDown();
		boolean twoSided = keyEvent != null && keyEvent.isShiftDown();

		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double w = bold ? penWidth * 2 : penWidth;
		double wd = w / 2;
		double dx = x2 - x1;
		double dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		double length = Math.sqrt(dx * dx + dy * dy);

		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		tx.concatenate(at);

		Path2D.Double path = new Path2D.Double();

		double arrowRatio = 0.5;
		double arrowLength = w * 5;
		double waisting = 0.35;
		double veeX = length - w * 0.5 / arrowRatio;

		double waistX = length - arrowLength * 0.5;
		double waistY = arrowRatio * arrowLength * 0.5 * waisting;
		double arrowWidth = arrowRatio * arrowLength;
		double x = twoSided ? w * 0.5 / arrowRatio + arrowLength * 0.75 : 0;

		path.moveTo(x, -wd);
		path.lineTo(veeX - arrowLength * 0.75, -wd);
		path.lineTo(veeX - arrowLength, -arrowWidth);
		path.quadTo(waistX, -waistY, length, 0);
		path.quadTo(waistX, waistY, veeX - arrowLength, arrowWidth);
		path.lineTo(veeX - arrowLength * 0.75, wd);
		path.lineTo(x, wd);

		if (twoSided) {
			waistX = x - arrowLength * 0.5;
			waistY = arrowRatio * arrowLength * 0.5 * waisting;
			
			path.lineTo(x + arrowLength * 0.25, arrowWidth);
			path.quadTo(waistX, waistY, 0, 0);
			path.quadTo(waistX, -waistY, x + arrowLength * 0.25, -arrowWidth);
		}

		path.closePath();

		return path;
	}

}
