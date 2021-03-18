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

package org.lecturestudio.core.util;

import java.lang.ref.WeakReference;

import static java.util.Objects.nonNull;

/**
 * WeakSetChangeListener's are used to maintain a weak reference to the
 * {@code SetChangeListener} with the goal to avoid memory leaks, that can
 * occur if the listener is not unregistered from the observed set.
 *
 * @author Alex Andres
 *
 * @param <T> The type of elements in the observable set.
 *
 * @see SetChangeListener
 * @see ObservableSet
 */
public final class WeakSetChangeListener<T> implements SetChangeListener<ObservableSet<T>> {

	private final WeakReference<SetChangeListener<ObservableSet<T>>> ref;


	/**
	 * Creates a new {@code WeakSetChangeListener}.
	 *
	 * @param listener The actual and backing {@link SetChangeListener}.
	 */
	public WeakSetChangeListener(SetChangeListener<ObservableSet<T>> listener) {
		this.ref = new WeakReference<>(listener);
	}

	@Override
	public void setChanged(ObservableSet<T> set) {
		final SetChangeListener<ObservableSet<T>> listener = ref.get();

		if (nonNull(listener)) {
			listener.setChanged(set);
		}
		else {
			// The listener reference has been garbage collected.
			set.removeListener(this);
		}
	}
}
