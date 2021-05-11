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
 * Integer property implementation.
 *
 * @author Alex Andres
 */
public class IntegerProperty extends ObjectProperty<Integer> {

	/**
	 * Create an {@link IntegerProperty] with the initial value set to {@code 0}.
	 */
	public IntegerProperty() {
		this(0);
	}

	/**
	 * Create an {@link IntegerProperty} with the specified initial value.
	 *
	 * @param defaultValue The initial value.
	 */
	public IntegerProperty(int defaultValue) {
		super(defaultValue);
	}

}
