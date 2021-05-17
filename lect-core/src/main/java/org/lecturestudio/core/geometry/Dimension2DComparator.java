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

import java.util.Comparator;

/**
 * Dimension2D comparator implementation. This {@link Comparator} compares the
 * width and height of the dimension. The dimension with greater width will be
 * of higher order. If the width is equal, the dimension with greater height
 * will be of higher order.
 *
 * @author Alex Andres
 */
public class Dimension2DComparator implements Comparator<Dimension2D> {

	@Override
	public int compare(Dimension2D d1, Dimension2D d2) {
		if (d1 == null && d2 == null) {
			return 0;
		}
		if (d1.getWidth() == d2.getWidth()) {
			return Double.compare(d1.getHeight(), d2.getHeight());
		}

		return Double.compare(d1.getWidth(), d2.getWidth());
	}

}
