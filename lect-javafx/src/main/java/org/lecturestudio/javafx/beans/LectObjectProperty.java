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

package org.lecturestudio.javafx.beans;

import static java.util.Objects.nonNull;

import javafx.beans.property.ObjectPropertyBase;

import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.javafx.util.FxUtils;

public class LectObjectProperty<T> extends ObjectPropertyBase<T> {

	private final ObjectProperty<T> wrappedProperty;
	
	private final ChangeListener<T> changeListener;


	public LectObjectProperty(ObjectProperty<T> property) {
		this.wrappedProperty = property;

		changeListener = (observable, oldValue, newValue) -> {
			FxUtils.invoke(() -> {
				invalidated();
				fireValueChangedEvent();
			});
		};

		wrappedProperty.addListener(changeListener);
	}

	@Override
	public Object getBean() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public T get() {
		return wrappedProperty.get();
	}

	@Override
	public void set(T value) {
		wrappedProperty.set(value);
	}

	@Override
	public void unbind() {
		super.unbind();

		if (nonNull(wrappedProperty)) {
			wrappedProperty.removeListener(changeListener);
		}
	}

}
