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

package org.lecturestudio.swing.renderer.operation;

/**
 * Implements a multiplicative combination of the two color values.
 * 
 * @author Tobias
 * 
 */
public class MultiplyOperation implements ImageOperation {

	@Override
	public int execute(int[] src, int[] dst) {
		int a = Math.min(255, src[0] + dst[0]);
		int r = (src[1] * dst[1]) >> 8;
		int g = (src[2] * dst[2]) >> 8;
		int b = (src[3] * dst[3]) >> 8;

		return a << 24 | r << 16 | g << 8 | b;
	}

}
