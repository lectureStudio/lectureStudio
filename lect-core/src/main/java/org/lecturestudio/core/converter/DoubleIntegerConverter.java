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

package org.lecturestudio.core.converter;

import org.lecturestudio.core.beans.Converter;

public class DoubleIntegerConverter implements Converter<Double, Integer> {

	/** The scalar of the converter. */
	private final double scalar;

	/**
	 * Create a {@link DoubleIntegerConverter} with the specified scalar.
	 *
	 * @param scalar The scalar of the converter.
	 */
	public DoubleIntegerConverter(double scalar) {
		this.scalar = scalar;
	}

	@Override
	public Integer to(Double value) {
		return (int) (value * scalar);
	}

	@Override
	public Double from(Integer value) {
		return value / scalar;
	}

}
