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

package org.lecturestudio.editor.api.model;

public class ZoomConstraints {

	private final double min;
	private final double max;


	public ZoomConstraints(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public final double getMinZoom() {
		return min;
	}

	public final double getMaxZoom() {
		return max;
	}

	public final double getValue(double value) {
		return Math.min(Math.max(value, min), max);
	}
}
