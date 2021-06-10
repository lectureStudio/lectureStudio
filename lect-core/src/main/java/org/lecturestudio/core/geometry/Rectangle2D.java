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
 * A {@link Rectangle2D} represents a rectangle in 2D space defined by a location (x, y) and dimension (w x h).
 *
 * @author Alex Andres
 */
public class Rectangle2D implements Cloneable, Serializable {

	private static final long serialVersionUID = -3647938113618642582L;

	/** The x coordinate. */
	private double x;

	/** The y coordinate. */
	private double y;

	/** The width. */
	private double width;

	/** The height. */
	private double height;


	/**
	 * Creates a new instance of {@link Rectangle2D} with origin coordinates and zero size.
	 */
	public Rectangle2D() {
		this(0, 0, 0, 0);
	}

	/**
	 * Creates a new instance of {@link Rectangle2D} with specified location coordinates and size.
	 *
	 * @param x      The x coordinate of the rectangle.
	 * @param y      The y coordinate of the rectangle.
	 * @param width  The width of the rectangle.
	 * @param height The height of the rectangle.
	 */
	public Rectangle2D(double x, double y, double width, double height) {
		setRect(x, y, width, height);
	}

	/**
	 * Creates a new instance of {@link Rectangle2D} with specified {@link Rectangle2D} to copy.
	 *
	 * @param rect The rectangle to copy.
	 */
	public Rectangle2D(Rectangle2D rect) {
		setRect(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Sets the location of the <code>Rectangle2D</code> to the specified values.
	 *
	 * @param x The new x coordinate.
	 * @param y The new y coordinate.
	 */
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets the size of the {@link Rectangle2D}> to the specified values.
	 *
	 * @param width  The width.
	 * @param height The height.
	 */
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * The location of the {@link Rectangle2D} represented by {@link Point2D}.
	 *
	 * @return The location of this rectangle.
	 */
	public Point2D getLocation() {
		return new Point2D(x, y);
	}

	/**
	 * The x coordinate of the top left corner.
	 *
	 * @return The x coordinate.
	 */
	public double getX() {
		return x;
	}

	/**
	 * The y coordinate of the top left corner.
	 *
	 * @return The y coordinate.
	 */
	public double getY() {
		return y;
	}

	/**
	 * The width of the {@link Rectangle2D}.
	 *
	 * @return The width of this rectangle.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * The height of the {@link Rectangle2D}.
	 *
	 * @return The height of this rectangle.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Adds a point to the {@link Rectangle2D}. The resulting {@link Rectangle2D} is enlarged
	 * so it contains the specified point.
	 *
	 * @param x The x coordinate of the point.
	 * @param y The y coordinate of the point.
	 */
	public void add(double x, double y) {
		double x1 = Math.min(this.x, x);
		double x2 = Math.max(this.x + this.width, x);
		double y1 = Math.min(this.y, y);
		double y2 = Math.max(this.y + this.height, y);

		setRect(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Checks, if the specified {@link Point2D} is inside the boundary of the {@link Rectangle2D}.
	 *
	 * @param point The {@link Point2D} that represents a x and y coordinate pair.
	 *
	 * @return {@code true} if the specified {@link Point2D} is inside the boundary, otherwise {@code true}.
	 */
	public boolean contains(Point2D point) {
		double px = point.getX();
		double py = point.getY();

		return (px >= x && py >= y && px < x + getWidth() && py < y + getHeight());
	}

	/**
	 * Checks if the interior of this {@link Rectangle2D} entirely encloses the specified {@link Rectangle2D}.
	 *
	 * @param r The {@link Rectangle2D} to check if it is enclosed by this {@link Rectangle2D}.
	 *
	 * @return {@code true} if the interior of this {@link Rectangle2D} entirely contains the specified area,
	 * otherwise {@code false}.
	 */
	public boolean contains(Rectangle2D r) {
		double w = r.getWidth();
		double h = r.getHeight();

		if (isEmpty() || w <= 0 || h <= 0) {
			return false;
		}

		double x = r.getX();
		double y = r.getY();
		double x0 = getX();
		double y0 = getY();

		return (x >= x0 && y >= y0 && (x + w) <= x0 + getWidth()
				&& (y + h) <= y0 + getHeight());
	}

	/**
	 * Intersects the provided {@link Rectangle2D} with this one and puts the result into
	 * the returned {@link Rectangle2D} object.
	 *
	 * @param rect The {@link Rectangle2D} to be intersected with this one.
	 *
	 * @return The intersection rectangle, or {@code null} if the rectangles don't intersect each other.
	 */
	public Rectangle2D intersection(Rectangle2D rect) {
		double iX = Math.max(x, rect.x);
		double iY = Math.max(y, rect.y);
		double iW = Math.min(x + width, rect.x + rect.width) - iX;
		double iH = Math.min(y + height, rect.y + rect.height) - iY;

		long zero = Double.doubleToLongBits(0);

		if (Double.doubleToLongBits(iW) <= zero) {
			return null;
		}
		if (Double.doubleToLongBits(iH) <= zero) {
			return null;
		}

		return new Rectangle2D(iX, iY, iW, iH);
	}

	/**
	 * Determines if the {@link Rectangle2D} encloses some area.
	 *
	 * @return {@code true} if the {@link Rectangle2D} is empty, otherwise {@code false}.
	 */
	public boolean isEmpty() {
		return (width <= 0.0) || (height <= 0.0);
	}

	/**
	 * Set the location and size of the {@link Rectangle2D} to the specified values.
	 *
	 * @param x      The x coordinate.
	 * @param y      The y coordinate.
	 * @param width  The width.
	 * @param height The height.
	 */
	public void setRect(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Set the diagonal of this rectangle.
	 *
	 * @param x1 the x coordinate of the start point of the diagonal.
	 * @param y1 the y coordinate of the start point of the diagonal.
	 * @param x2 the x coordinate of the end point of the diagonal.
	 * @param y2 the y coordinate of the end point of the diagonal.
	 */
	public void setFromDiagonal(double x1, double y1, double x2, double y2) {
		if (x2 < x1) {
			double t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y2 < y1) {
			double t = y1;
			y1 = y2;
			y2 = t;
		}

		setLocation(x1, y1);
		setSize(x2 - x1, y2 - y1);
	}

	/**
	 * Unions the provided {@link Rectangle2D} with this one and puts the result into this {@link Rectangle2D} object.
	 *
	 * @param rect The {@link Rectangle2D} to be combined with this one.
	 */
	public void union(Rectangle2D rect) {
		double x1 = Math.min(getX(), rect.getX());
		double y1 = Math.min(getY(), rect.getY());
		double x2 = Math.max(getX() + getWidth(), rect.getX() + rect.getWidth());
		double y2 = Math.max(getY() + getHeight(), rect.getY() + rect.getHeight());

		setRect(x1, y1, x2 - x1, y2 - y1);
	}

	@Override
	public String toString() {
		return getClass().getName() + " (" + x + ", " + y + ", " + width + ", " + height + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;

		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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

		Rectangle2D other = (Rectangle2D) obj;

		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height)) {
			return false;
		}
		if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width)) {
			return false;
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}

		return Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
	}

	@Override
	public Rectangle2D clone() {
		return new Rectangle2D(x, y, width, height);
	}

}
