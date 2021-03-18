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

package org.lecturestudio.swing.beans;

import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.ObjectProperty;

public class ConvertibleNumberProperty<S, T extends Number> extends ObjectProperty<T> {

	private final ObjectProperty<S> wrappedProperty;

	private final ChangeListener<S> changeListener;

	private final Converter<S, T> converter;


	public ConvertibleNumberProperty(ObjectProperty<S> property, Converter<S, T> converter) {
		this.wrappedProperty = property;
		this.converter = converter;

		changeListener = (observable, oldValue, newValue) -> {
			fireChange(this, converter.to(oldValue), converter.to(newValue));
		};

		wrappedProperty.addListener(changeListener);
	}

	@Override
	public T get() {
		return converter.to(wrappedProperty.get());
	}

	@Override
	public void set(T value) {
		wrappedProperty.set(converter.from(value));
	}
}
