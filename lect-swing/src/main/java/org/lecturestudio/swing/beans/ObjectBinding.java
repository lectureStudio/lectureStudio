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
import org.lecturestudio.core.beans.ObjectProperty;

public class ObjectBinding<T> implements Binding {

	private final ObjectProperty<T> source;

	private final ObjectProperty<T> target;

	private final ChangeListener<T> sourceListener;

	private final ChangeListener<T> targetListener;


	public ObjectBinding(ObjectProperty<T> source, ObjectProperty<T> target) {
		this.source = source;
		this.target = target;

		sourceListener = (observable, oldValue, newValue) -> {
			target.set(newValue);
		};
		targetListener = (observable, oldValue, newValue) -> {
			source.set(newValue);
		};

		source.addListener(sourceListener);
		target.addListener(targetListener);
		target.set(source.get());
	}

	@Override
	public void unbind() {
		source.removeListener(sourceListener);
		target.removeListener(targetListener);
	}
}
