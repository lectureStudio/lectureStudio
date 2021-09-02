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

/**
 * Geometry-related utility methods.
 *
 * @author Alex Andres
 */
public abstract class GeometryUtils {

	/**
	 * Transform the specified dimension so that it has the same aspect ratio as the specified ratio.
	 *
	 * @param in    The dimension to transform.
	 * @param ratio The desired aspect ratio.
	 *
	 * @return The transformed dimension with the specified aspect ratio.
	 */
	public static Dimension2D keepAspectRatio(Dimension2D in, Dimension2D ratio) {
		double width = in.getWidth();
		double height = Math.abs(width * ratio.getHeight() / ratio.getWidth()) * Math.signum(in.getHeight());
			
		return new Dimension2D(width, height);
	}

}
