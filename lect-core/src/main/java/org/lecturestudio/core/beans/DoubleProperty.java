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

package org.lecturestudio.core.beans;

/**
 * Double property implementation.
 *
 * @author Alex Andres
 */
public class DoubleProperty extends ObjectProperty<Double> {

	/**
	 * Create a {@link DoubleProperty} with the initial value set to {@code 0}.
	 */
	public DoubleProperty() {
		this(0);
	}

	/**
	 * Create a {@link DoubleProperty} with the specified initial value.
	 *
	 * @param defaultValue The initial value.
	 */
	public DoubleProperty(double defaultValue) {
		super(defaultValue);
	}

}
