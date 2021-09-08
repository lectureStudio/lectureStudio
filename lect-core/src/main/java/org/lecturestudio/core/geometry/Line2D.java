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

import java.io.Serializable;

/**
 * Line2D represents a line segment in 2D space with a start point and an end
 * point.
 *
 * @author Alex Andres
 */
public class Line2D implements Cloneable, Serializable {

	private static final long serialVersionUID = -9153963597515138133L;

	/** The x coordinate of the start point. */
	private double x1;

	/** The y coordinate of the start point. */
	private double y1;

	/** The x coordinate of the end point. */
	private double x2;

	/** The y coordinate of the end point. */
	private double y2;


	/**
	 * Creates a new instance of {@link Line2D} with zero length.
	 */
	public Line2D() {
		set(0, 0, 0, 0);
	}

	/**
	 * Creates a new instance of {@link Line2D} with specified start and end
	 * point.
	 *
	 * @param start The start point of the line.
	 * @param end   The end point of the line.
	 */
	public Line2D(Point2D start, Point2D end) {
		set(start, end);
	}

	/**
	 * Creates a new instance of {@link Line2D} with specified coordinates.
	 *
	 * @param x1 The x coordinate of the start point.
	 * @param y1 The y coordinate of the start point.
	 * @param x2 The x coordinate of the end point.
	 * @param y2 The y coordinate of the end point.
	 */
	public Line2D(double x1, double y1, double x2, double y2) {
		set(x1, y1, x2, y2);
	}

	/**
	 * Sets new start and end coordinates.
	 *
	 * @param start The start point of the line.
	 * @param end   The end point of the line.
	 */
	public void set(Point2D start, Point2D end) {
		set(start.getX(), start.getY(), end.getX(), end.getY());
	}

	/**
	 * Sets new start and end coordinates.
	 *
	 * @param x1 The x coordinate of the start point.
	 * @param y1 The y coordinate of the start point.
	 * @param x2 The x coordinate of the end point.
	 * @param y2 The y coordinate of the end point.
	 */
	public void set(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	/**
	 * The start point of the {@link Line2D} represented by {@link Point2D}.
	 *
	 * @return The start point.
	 */
	public Point2D getStartPoint() {
		return new Point2D(x1, y1);
	}

	/**
	 * The end point of the {@link Line2D} represented by {@link Point2D}.
	 *
	 * @return The end point.
	 */
	public Point2D getEndPoint() {
		return new Point2D(x2, y2);
	}

	/**
	 * Returns the shortest distance from a point to the {@link Line2D}.
	 *
	 * @param p The point to which to compute the distance to.
	 *
	 * @return the shortest distance from a point to this {@link Line2D}.
	 */
	public double distance(Point2D p) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double length = Math.sqrt(dx * dx + dy * dy);

		return Math.abs(((y1 - y2) * p.getX() + dx * p.getY() + (x1 * y2 - x2 * y1)) / length);
	}

	/**
	 * Check if the specified {@link Line2D} intersects this line.
	 *
	 * @param line The line to check the intersection with.
	 *
	 * @return {@code true} if the lines intersect each other, otherwise {@code
	 * true}.
	 */
	public boolean intersects(Line2D line) {
		double x3 = line.x1;
		double x4 = line.x2;
		double y3 = line.y1;
		double y4 = line.y2;

		double d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

		// Are the lines parallel.
		if (d == 0) {
			return false;
		}

		// Calculate the intermediate fractional point.
		double a = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		double b = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);
		double ua = a / d;
		double ub = b / d;

		// Check for intersection along the segments.
		return ua >= 0d && ua <= 1d && ub >= 0d && ub <= 1d;
	}

	/**
	 * Find the intersection point of two lines. If the lines are parallel, the
	 * return value is {@code null}.
	 *
	 * @param line The other line of which the intersection point should be
	 *             found.
	 *
	 * @return The intersection point, or {@code null}, if the lines are
	 * parallel.
	 */
	public Point2D getIntersectionPoint(Line2D line) {
		double x3 = line.x1;
		double x4 = line.x2;
		double y3 = line.y1;
		double y4 = line.y2;

		double d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

		// Are the lines parallel.
		if (Math.abs(d) < 0.01) {
			return null;
		}

		// Calculate the intermediate fractional point.
		double a = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		double b = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);
		double ua = a / d;
		double ub = b / d;

		// Check for intersection along the the segments.
		if (ua >= 0d && ua <= 1d && ub >= 0d && ub <= 1d) {
			double A1 = y2 - y1;
			double B1 = x1 - x2;
			double C1 = A1 * x1 + B1 * y1;
			double A2 = y4 - y3;
			double B2 = x3 - x4;
			double C2 = A2 * x3 + B2 * y3;

			return new Point2D((B2 * C1 - B1 * C2) / d, (A1 * C2 - A2 * C1) / d);
		}

		return null;
	}

	@Override
	public String toString() {
		return getClass().getName() + " (" + x1 + ", " + y1 + ") -> " + "(" + x2 + ", " + y2 + ")";
	}

	@Override
	public Line2D clone() {
		try {
			return (Line2D) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

}
