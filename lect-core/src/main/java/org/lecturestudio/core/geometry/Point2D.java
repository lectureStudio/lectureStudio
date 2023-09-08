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
 * A geometric point that represents the x, y coordinates.
 *
 * @author Alex Andres
 */
public class Point2D implements Cloneable, Serializable {

	private static final long serialVersionUID = 3737399850497337585L;

	/** The x coordinate. */
	private double x;

	/** The y coordinate. */
	private double y;


	/**
	 * Create a new instance of {@link Point2D} with origin coordinates.
	 */
	public Point2D() {
		this(0, 0);
	}

	/**
	 * Create a new instance of {@link Point2D} with coordinates from provided point.
	 */
	public Point2D(Point2D point) {
		this(point.getX(), point.getY());
	}

	/**
	 * Creates a new instance of {@link Point2D} with specified coordinates.
	 *
	 * @param x The x coordinate of the point.
	 * @param y The y coordinate of the point.
	 */
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * The x coordinate of the point.
	 *
	 * @return The x coordinate.
	 */
	public double getX() {
		return x;
	}

	/**
	 * The y coordinate of the point.
	 *
	 * @return The y coordinate.
	 */
	public double getY() {
		return y;
	}

	/**
	 * Set new coordinates of this point.
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public <T extends Point2D> T set(double x, double y) {
		this.x = x;
		this.y = y;

		return (T) this;
	}

	/**
	 * Set the coordinates to the values from the specified point.
	 *
	 * @param p The point from which to copy the coordinates.
	 *
	 * @return This point.
	 */
	public <T extends Point2D> T set(Point2D p) {
		set(p.x, p.y);

		return (T) this;
	}

	/**
	 * Returns the distance from this point to a specified point.
	 *
	 * @param p The point to which the distance should be measured.
	 *
	 * @return The distance to the given point.
	 */
	public double distance(Point2D p) {
		double dx = p.x - x;
		double dy = p.y - y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Compute the dot product of the vector represented by this instance and
	 * the specified vector.
	 *
	 * @param p The other vector.
	 *
	 * @return The dot product of the two vectors
	 */
	public double dot(Point2D p) {
		return x * p.x + y * p.y;
	}

	/**
	 * Add coordinates of the specified point to the coordinates of this point.
	 *
	 * @param p The point whose coordinates are to be added.
	 *
	 * @return This point instance with added coordinates.
	 */
	public <T extends Point2D> T add(Point2D p) {
		x += p.x;
		y += p.y;

		return (T) this;
	}

	/**
	 * Subtract coordinates of the specified point from the coordinates of this point.
	 *
	 * @param p The point whose coordinates are to be subtracted.
	 *
	 * @return This point instance with subtracted coordinates.
	 */
	public <T extends Point2D> T subtract(Point2D p) {
		x -= p.x;
		y -= p.y;

		return (T) this;
	}

	/**
	 * Interpolate a point between the specified point and this point with the defined scalar.
	 *
	 * @param v The other point that will define a line segment with this point.
	 * @param f The scalar.
	 *
	 * @return An interpolated point.
	 */
	public Point2D interpolate(Point2D v, double f) {
		return new Point2D(this.x + (v.x - this.x) * f, this.y + (v.y - this.y) * f);
	}

	/**
	 * Multiply the coordinates of this point with the specified factor.
	 *
	 * @param factor The multiplying factor.
	 *
	 * @return This point instance with multiplied coordinates.
	 */
	public <T extends Point2D> T multiply(double factor) {
		x *= factor;
		y *= factor;

		return (T) this;
	}

	/**
	 * Normalize the relative magnitude vector represented by this point.
	 *
	 * @return This point instance as the normalized vector.
	 */
	public <T extends Point2D> T normalize() {
		double length = Math.sqrt(x * x + y * y);

		x /= length;
		y /= length;

		return (T) this;
	}

	/**
	 * Normalize this point to the specified length.
	 *
	 * @param length The length to normalize to.
	 *
	 * @return This normalized point instance.
	 */
	public <T extends Point2D> T normalize(double length) {
		double mag = Math.sqrt(x * x + y * y);

		if (mag > 0) {
			mag = length / mag;
			x *= mag;
			y *= mag;
		}

		return (T) this;
	}

	/**
	 * Compute a point on the perpendicular of this point.
	 *
	 * @return A new point on the perpendicular of this point.
	 */
	public <T extends Point2D> T perpendicular() {
		double t = x;
		this.x = -y;
		this.y = t;

		return (T) this;
	}

	@Override
	public String toString() {
		return getClass().getName() + " (" + x + ", " + y + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		Point2D other = (Point2D) obj;

		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}

		return Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
	}

	@Override
	public Point2D clone() {
		return new Point2D(x, y);
	}

	/**
	 * Inverts the coordinates
	 *
	 * @return itself with the changed coordinates
	 */
	public <T extends Point2D> T invert() {
		x = -x;
		y = -y;

		return (T) this;
	}
}
