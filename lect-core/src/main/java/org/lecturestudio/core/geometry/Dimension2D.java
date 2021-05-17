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

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * The {@link Dimension2D} represents the horizontal and vertical extent of an object in 2D space.
 *
 * @author Alex Andres
 */
public class Dimension2D implements Cloneable, Serializable {

	private static final long serialVersionUID = 221175148276014654L;

	/** The width. */
	private double width;

	/** The height. */
	private double height;


	/**
	 * Creates a new instance of {@link Dimension2D} with zero size.
	 */
	public Dimension2D() {
		this(0, 0);
	}

	/**
	 * Creates a new instance of {@link Dimension2D} with the specified size.
	 *
	 * @param width  The width.
	 * @param height The height.
	 */
	public Dimension2D(double width, double height) {
		setSize(width, height);
	}

	/**
	 * The width of this dimension.
	 *
	 * @return The width.
	 */
	public double getWidth() {
		return width;
	}
	
	/**
	 * The height of this dimension.
	 * 
	 * @return The height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Set the new width and height of this dimension.
	 *
	 * @param width  The new width to set.
	 * @param height The new height to set.
	 */
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(width, height);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Dimension2D other = (Dimension2D) obj;

		return Objects.equal(getWidth(), other.getWidth()) && Objects
				.equal(getHeight(), other.getHeight());
	}

	@Override
	public String toString() {
		return getClass().getName() + " (" + width + ", " + height + ")";
	}

	@Override
	public Dimension2D clone() {
		try {
			return (Dimension2D) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

}
