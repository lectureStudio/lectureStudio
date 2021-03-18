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

import java.util.Objects;

/**
 * A 2D matrix implementation that currently only holds the 3x3 transformation
 * matrix double precision values.
 *
 * @author Alex Andres
 */
public class Matrix implements Cloneable {

	/** The X coordinate scaling element. */
	private double m00;

	/** The Y coordinate shearing element. */
	private double m10;

	/** The X coordinate shearing element. */
	private double m01;

	/** The Y coordinate scaling element. */
	private double m11;

	/** The X coordinate of the translation element. */
	private double m02;

	/** The Y coordinate of the translation element. */
	private double m12;


	/**
	 * Creates a new {@code Matrix} representing the Identity matrix.
	 */
	public Matrix() {
		m00 = m11 = 1.0;
	}

	/**
	 * Creates a new {@code Matrix} from 6 double precision values representing
	 * the 6 specifiable entries of the 3x3 transformation matrix.
	 *
	 * @param m00 The X coordinate scaling element of the matrix.
	 * @param m10 The Y coordinate shearing element of the matrix.
	 * @param m01 The X coordinate shearing element of the matrix.
	 * @param m11 The Y coordinate scaling element of the matrix.
	 * @param m02 The X coordinate translation element of the matrix.
	 * @param m12 The Y coordinate translation element of the matrix.
	 */
	public Matrix(double m00, double m10, double m01, double m11, double m02, double m12) {
		this.m00 = m00;
		this.m10 = m10;
		this.m01 = m01;
		this.m11 = m11;
		this.m02 = m02;
		this.m12 = m12;
	}

	/**
	 * Returns the {@code m00} scaling element of the 3x3 transformation
	 * matrix.
	 *
	 * @return The {@code m00} element of the matrix.
	 */
	public double getScaleX() {
		return m00;
	}

	/**
	 * Returns the {@code m11} scaling element of the 3x3 transformation
	 * matrix.
	 *
	 * @return The {@code m11} element of the matrix.
	 */
	public double getScaleY() {
		return m11;
	}

	/**
	 * Returns the {@code m01} shearing element of the 3x3 transformation
	 * matrix.
	 *
	 * @return The {@code m01} element of the matrix.
	 */
	public double getShearX() {
		return m01;
	}

	/**
	 * Returns the {@code m10} shearing element of the 3x3 transformation
	 * matrix.
	 *
	 * @return The {@code m10} element of the matrix.
	 */
	public double getShearY() {
		return m10;
	}

	/**
	 * Returns the {@code m02} translation element of the 3x3 transformation
	 * matrix.
	 *
	 * @return The {@code m02} element of the matrix.
	 */
	public double getTranslateX() {
		return m02;
	}

	/**
	 * Returns the {@code m12} translation element of the 3x3 transformation
	 * matrix.
	 *
	 * @return The {@code m12} element of the matrix.
	 */
	public double getTranslateY() {
		return m12;
	}

	/**
	 * Sets this matrix to the matrix specified by the 6 double precision
	 * values.
	 *
	 * @param m00 The X coordinate scaling element of the matrix.
	 * @param m10 The Y coordinate shearing element of the matrix.
	 * @param m01 The X coordinate shearing element of the matrix.
	 * @param m11 The Y coordinate scaling element of the matrix.
	 * @param m02 The X coordinate translation element of the matrix.
	 * @param m12 The Y coordinate translation element of the matrix.
	 */
	public void setTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
		this.m00 = m00;
		this.m10 = m10;
		this.m01 = m01;
		this.m11 = m11;
		this.m02 = m02;
		this.m12 = m12;
	}

	/**
	 * Sets this matrix to the Identity matrix.
	 */
	public void setToIdentity() {
		m00 = m11 = 1.0;
		m10 = m01 = m02 = m12 = 0.0;
	}

	/**
	 * Sets this matrix to a copy of the specified matrix.
	 *
	 * @param m The matrix object from which to copy the element values.
	 */
	public void setMatrix(Matrix m) {
		this.m00 = m.m00;
		this.m10 = m.m10;
		this.m01 = m.m01;
		this.m11 = m.m11;
		this.m02 = m.m02;
		this.m12 = m.m12;
	}

	/**
	 * Returns a deep copy of this matrix.
	 *
	 * @return a copy of this matrix.
	 */
	@Override
	public Matrix clone() {
		try {
			return (Matrix) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		Matrix matrix = (Matrix) other;

		return Double.compare(matrix.m00, m00) == 0
				&& Double.compare(matrix.m10, m10) == 0
				&& Double.compare(matrix.m01, m01) == 0
				&& Double.compare(matrix.m11, m11) == 0
				&& Double.compare(matrix.m02, m02) == 0
				&& Double.compare(matrix.m12, m12) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(m00, m10, m01, m11, m02, m12);
	}
}
