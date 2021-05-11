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
 * Object property implementation.
 *
 * @param <T> The type of the object value.
 *
 * @author Alex Andres
 */
public class ObjectProperty<T> extends ObservableBase<T> implements Property<T> {

	/** The object value. */
	private T value;


	/**
	 * Create an {@link ObjectProperty} with the initial value set to {@code null}.
	 */
	public ObjectProperty() {
		this(null);
	}

	/**
	 * Create an {@link ObjectProperty} with the specified initial value.
	 *
	 * @param value The initial value.
	 */
	public ObjectProperty(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public void set(T newValue) {
		if (value != newValue) {
			final T oldValue = value;
			value = newValue;

			fireChange(this, oldValue, newValue);
		}
	}

}
