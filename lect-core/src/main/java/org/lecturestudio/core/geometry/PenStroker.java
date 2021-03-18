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

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The PenStroker generates variable width strokes from a list of points or
 * dynamically added points in sequence.
 *
 * @author Alex Andres
 */
public class PenStroker {

	/** Bottom line of the stroke. */
	private final List<PenPoint2D> A = new ArrayList<>();

	/** Top line of the stroke. */
	private final List<PenPoint2D> B = new ArrayList<>();

	/** Last two inserted points. */
	private final List<PenPoint2D> points = new ArrayList<>();

	/** The stroke width. */
	private final double strokeWidth;

	/** Temporary working space. */
	private final Point2D vector = new Point2D();
	private final Point2D normal = new Point2D();
	private final Point2D miter = new Point2D();
	private final Point2D offsetA = new Point2D();
	private final Point2D offsetB = new Point2D();
	private final Line2D lastLineTop = new Line2D();
	private final Line2D lastLineBottom = new Line2D();


	/**
	 * Create a PenStroker with the specified stroke width. The stroke width is
	 * multiplied with the pressure of the individual points.
	 *
	 * @param strokeWidth The width of the stroke.
	 */
	public PenStroker(double strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	/**
	 * Create a variable width stroke from the specified list of pen points.
	 *
	 * @param points The list of points captured by a input device.
	 *
	 * @return a variable width stroke as {@code Path2D}.
	 */
	public Path2D createPath(List<PenPoint2D> points) {
		if (points == null || points.isEmpty()) {
			return null;
		}
		
		for (PenPoint2D point : points) {
			addPoint(point);
		}
		
		return getStrokePath();
	}

	/**
	 * Get a list of points this PenStroker has generated for all captured
	 * points.
	 *
	 * @return a list of points representing the generated variable width path.
	 */
	public List<PenPoint2D> getStrokeList() {
		if (points.isEmpty()) {
			return null;
		}

		List<PenPoint2D> stroke = new ArrayList<>();

		// Special case with only one point.
		if (points.size() == 1) {
			capOnePoint(stroke, points.get(0), strokeWidth);

			return stroke;
		}

		// Get bottom line.
		stroke.addAll(A);

		// Cap last point.
		capEndpoint(stroke, points.get(1), points.get(0), false);

		// Get top line.
		stroke.addAll(B);

		return stroke;
	}

	/**
	 * Get the generated stoke path.
	 *
	 * @return the generated stoke path.
	 */
	public Path2D getStrokePath() {
		if (points.isEmpty()) {
			return null;
		}
		
		Path2D path = new Path2D.Double();
		Iterator<PenPoint2D> iter;
		
		// Special case with only one point.
		if (points.size() == 1) {
			List<PenPoint2D> stroke = new ArrayList<>();
			
			capOnePoint(stroke, points.get(0), strokeWidth);
			
			iter = stroke.iterator();
			
			PenPoint2D p0 = iter.next();
			path.moveTo(p0.getX(), p0.getY());
			
			advancePath(iter, path);

			return path;
		}

		List<PenPoint2D> temp = new ArrayList<>(A);
		
		iter = temp.iterator();
		PenPoint2D p0 = iter.next();
		
		path.moveTo(p0.getX(), p0.getY());
		
		advancePath(iter, path);
		
		List<PenPoint2D> caption = new ArrayList<>();
		capEndpoint(caption, points.get(1), points.get(0), false);
		
		iter = caption.iterator();
		advancePath(iter, path);
		
		temp = new ArrayList<>(B);
		
		iter = temp.iterator();
		advancePath(iter, path);
		
		return path;
	}

	/**
	 * Add a pen point and advance the variable width stroke.
	 *
	 * @param point The point to add.
	 */
	public void addPoint(PenPoint2D point) {
		points.add(point.clone());
		
		int pSize = points.size();
		
		if (pSize > 1) {
			if (pSize > 2) {
				// Keep track only of two last observed points.
				points.remove(0);
			}
			
			PenPoint2D p0 = points.get(0);
			
			if (A.isEmpty() || B.isEmpty()) {
				// Cap first point.
				capEndpoint(A, p0, point, false);
				beginPath(p0, point);
			}
			else {
				advancePath(p0, point);
			}
		}
	}

	/**
	 * Translate all points of the variable width stroke by the specified delta.
	 *
	 * @param delta The delta by which to translate the stroke.
	 */
	public void moveByDelta(PenPoint2D delta) {
		for (PenPoint2D point : A) {
			point.subtract(delta);
		}
		for (PenPoint2D point : B) {
			point.subtract(delta);
		}
		for (PenPoint2D point : points) {
			point.subtract(delta);
		}
	}
	
	private void advancePath(PenPoint2D p0, PenPoint2D p1) {
		advancePath(A, p0, p1, false);
		advancePath(B, p0, p1, true);
	}
	
	private void advancePath(List<PenPoint2D> target, PenPoint2D p0, PenPoint2D p1, boolean reverse) {
		vector.set(p1).subtract(p0).normalize();
		normal.set(-vector.getY(), vector.getX()).normalize();
		
		miter.set(normal).multiply(strokeWidth * toPressure(p0) / 2);
		offsetA.set(p0);
		
		offset(offsetA, miter, reverse);
		
		miter.set(normal).multiply(strokeWidth * toPressure(p1) / 2);
		offsetB.set(p1);
		
		offset(offsetB, miter, reverse);
		
		Line2D line = new Line2D(offsetA, offsetB);
		Line2D lastLine = reverse ? lastLineTop : lastLineBottom;
		
		Point2D inter = lastLine.getIntersectionPoint(line);

		if (inter != null) {
			intersect(target, inter, reverse);
		}
		else {
			Point2D a = lastLine.getEndPoint();
			Point2D b = line.getStartPoint();

			double s = toDegrees(a, p0);
			double e = toDegrees(b, p0);
			
			double d = Math.abs(s - e);
			
			if (d > 180) {
				s %= 360;
				e %= 360;
			}

			cap(target, p0, s, e, strokeWidth * toPressure(p0) / 2, 2, reverse);
		}
		
		lastLine.set(line.getStartPoint(), line.getEndPoint());
	}
	
	private void beginPath(PenPoint2D p0, PenPoint2D p1) {
		vector.set(p1).subtract(p0).normalize();
		normal.set(-vector.getY(), vector.getX()).normalize();
		
		// Bottom
		miter.set(normal).multiply(strokeWidth * toPressure(p0) / 2);
		offsetA.set(p0).add(miter);
		
		miter.set(normal).multiply(strokeWidth * toPressure(p1) / 2);
		offsetB.set(p1).add(miter);
		
		addPathPoint(A, offsetB.getX(), offsetB.getY(), false);
		
		lastLineBottom.set(offsetA, offsetB);
		
		// Top
		miter.set(normal).multiply(strokeWidth * toPressure(p0) / 2);
		offsetA.set(p0).subtract(miter);
		
		miter.set(normal).multiply(strokeWidth * toPressure(p1) / 2);
		offsetB.set(p1).subtract(miter);
		
		addPathPoint(B, offsetA.getX(), offsetA.getY(), true);
		addPathPoint(B, offsetB.getX(), offsetB.getY(), true);
		
		lastLineTop.set(offsetA, offsetB);
	}
	
	private void capEndpoint(List<PenPoint2D> target, PenPoint2D p0, PenPoint2D p1, boolean reverse) {
		vector.set(p1).subtract(p0).normalize();
		normal.set(-vector.getY(), vector.getX()).normalize();
		miter.set(normal).multiply(strokeWidth * toPressure(p0) / 2);
		
		offsetA.set(p0).add(miter);
		offsetB.set(p0).subtract(miter);
		
		// First/Last point offset.
		addPathPoint(target, offsetB.getX(), offsetB.getY(), reverse);

		// Cap point with 180 degrees.
		double s = toDegrees(offsetB, offsetA);
		double e = s - 180;

		cap(target, p0, s, e, strokeWidth * toPressure(p0) / 2, 2, reverse);
	}
	
	private static void cap(List<PenPoint2D> target, PenPoint2D center, double start, double end, double radius, int step, boolean reverse) {
		if (start > end) {
			for (double angle = start; angle >= end; angle -= step) {
				double rad = Math.PI * angle / 180;
				
				double x = center.getX() + radius * Math.cos(rad);
				double y = center.getY() + radius * Math.sin(rad);
				
				addPathPoint(target, x, y, reverse);
			}
		}
		else {
			for (double angle = start; angle <= end; angle += step) {
				double rad = Math.PI * angle / 180;
				
				double x = center.getX() + radius * Math.cos(rad);
				double y = center.getY() + radius * Math.sin(rad);
				
				addPathPoint(target, x, y, reverse);
			}
		}
	}
	
	private static void capOnePoint(List<PenPoint2D> target, PenPoint2D center, double strokeWidth) {
		addPathPoint(target, center.getX() + strokeWidth * toPressure(center) / 2, center.getY(), false);
		
		cap(target, center, 0, 360, strokeWidth * toPressure(center) / 2, 2, false);
	}
	
	private static void advancePath(Iterator<PenPoint2D> iter, Path2D path) {
		PenPoint2D point;
		
		while (iter.hasNext()) {
			point = iter.next();
			
			path.lineTo(point.getX(), point.getY());
		}
	}
	
	private static void addPathPoint(List<PenPoint2D> target, double x, double y, boolean reverse) {
		if (reverse) {
			target.add(0, new PenPoint2D(x, y));
		}
		else {
			target.add(new PenPoint2D(x, y));
		}
	}

	private static void offset(Point2D center, Point2D miter, boolean reverse) {
		if (reverse) {
			center.subtract(miter);
		}
		else {
			center.add(miter);
		}
	}
	
	private static void intersect(List<PenPoint2D> target, Point2D inter, boolean reverse) {
		if (reverse) {
			target.get(0).set(inter);
		}
		else {
			target.get(target.size() - 1).set(inter);
		}
	}
	
	private static double toPressure(PenPoint2D point) {
		return Math.min(1.0, Math.sqrt(point.getPressure() + 0.1));
	}
	
	private static double toDegrees(Point2D p1, Point2D p2) {
		double degrees = Math.atan2(p1.getY() - p2.getY(), p1.getX() - p2.getX());
		
		return (2 * Math.PI + degrees) * 180 / Math.PI;
	}
	
}
