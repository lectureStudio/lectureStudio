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

package org.lecturestudio.core.camera;

import java.util.Comparator;

/**
 * CameraFormat comparator implementation. This {@code Comparator} compares the
 * image width and height of the camera formats. The format with greater width
 * will be of higher order. If the width is equal, the format with greater
 * height will be of higher order.
 *
 * @author Alex Andres
 */
public class CameraFormatComparator implements Comparator<CameraFormat> {

	@Override
	public int compare(CameraFormat f1, CameraFormat f2) {
		if (f1 == null && f2 == null) {
			return 0;
		}
		if (f1.getWidth() == f2.getWidth()) {
			return Integer.compare(f1.getHeight(), f2.getHeight());
		}

		return Integer.compare(f1.getWidth(), f2.getWidth());
	}
	
}
