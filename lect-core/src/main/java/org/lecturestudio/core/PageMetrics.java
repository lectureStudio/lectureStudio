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

package org.lecturestudio.core;

import org.lecturestudio.core.geometry.Dimension2D;

/**
 * A helper class to compute and keep the aspect ratio of an rectangle. This
 * class is meant to keep the aspect ratio of slides that are rendered at
 * different sizes.
 *
 * @author Alex Andres
 */
public class PageMetrics {

	/** The width of the rectangle. */
	private final double m_width;

	/** The height of the rectangle. */
	private final double m_height;

	/** The initial aspect ratio of the rectangle. */
	private final double m_ratio;


	/**
	 * Create a new PageMetrics instance with the given width and height.
	 *
	 * @param width  The initial width of the rectangle.
	 * @param height The initial height of the rectangle.
	 */
	public PageMetrics(double width, double height) {
		m_width = width;
		m_height = height;
		m_ratio = width / height;
	}

	/**
	 * Obtain the initial width.
	 *
	 * @return the initial width.
	 */
	public double getWidth() {
		return m_width;
	}

	/**
	 * Obtain the new width of the rectangle by keeping the aspect ratio
	 * according to the specified height.
	 *
	 * @param height The height of the rectangle.
	 *
	 * @return the width of the rectangle in relation to the height.
	 */
	public double getWidth(double height) {
		return Math.round((height / m_height) * m_width);
	}

	/**
	 * Obtain the initial height.
	 *
	 * @return the initial height.
	 */
	public double getHeight() {
		return m_height;
	}

	/**
	 * Obtain the new height of the rectangle by keeping the aspect ratio
	 * according to the specified width.
	 *
	 * @param width The width of the rectangle.
	 *
	 * @return the height of the rectangle in relation to the width.
	 */
	public double getHeight(double width) {
		return (width / m_width) * m_height;
	}

	/**
	 * Obtain the initial ratio.
	 *
	 * @return the initial ratio.
	 */
	public double getRatio() {
		return m_ratio;
	}

	/**
	 * Convert the rectangle defined by the provided width and height to a
	 * rectangle that has the aspect ratio of this page metrics.
	 *
	 * @param width  The width of the rectangle to convert.
	 * @param height The height of the rectangle to convert.
	 *
	 * @return the converted rectangle having the aspect ratio of this page
	 * metrics.
	 */
	public Dimension2D convert(double width, double height) {
		double ratio = width / height;

		if (ratio > m_ratio) {
			width = getWidth(height);
		}
		else {
			height = getHeight(width);
		}

		return new Dimension2D(width, height);
	}

}
