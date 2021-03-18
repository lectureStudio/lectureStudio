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

import static java.util.Objects.nonNull;

import java.lang.ref.WeakReference;

/**
 * WeakListChangeListener's are used to maintain a weak reference to the
 * {@code ListChangeListener} with the goal to avoid memory leaks, that can
 * occur if the listener is not unregistered from the observed list.
 *
 * @author Alex Andres
 *
 * @param <T> The type of elements in the observable list.
 *
 * @see ListChangeListener
 * @see ObservableList
 */
public final class WeakListChangeListener<T> implements ListChangeListener<ObservableList<T>> {

	private final WeakReference<ListChangeListener<ObservableList<T>>> ref;


	/**
	 * Creates a new {@code WeakListChangeListener}.
	 *
	 * @param listener The actual and backing {@link ListChangeListener}.
	 */
	public WeakListChangeListener(ListChangeListener<ObservableList<T>> listener) {
		this.ref = new WeakReference<>(listener);
	}

	@Override
	public void listItemsChanged(ObservableList<T> list, int startIndex, int itemCount) {
		final ListChangeListener<ObservableList<T>> listener = ref.get();

		if (nonNull(listener)) {
			listener.listItemsChanged(list, startIndex, itemCount);
		}
		else {
			// The listener reference has been garbage collected.
			list.removeListener(this);
		}
	}

	@Override
	public void listItemsInserted(ObservableList<T> list, int startIndex, int itemCount) {
		final ListChangeListener<ObservableList<T>> listener = ref.get();

		if (nonNull(listener)) {
			listener.listItemsInserted(list, startIndex, itemCount);
		}
		else {
			// The listener reference has been garbage collected.
			list.removeListener(this);
		}
	}

	@Override
	public void listItemsRemoved(ObservableList<T> list, int startIndex, int itemCount) {
		final ListChangeListener<ObservableList<T>> listener = ref.get();

		if (nonNull(listener)) {
			listener.listItemsRemoved(list, startIndex, itemCount);
		}
		else {
			// The listener reference has been garbage collected.
			list.removeListener(this);
		}
	}
}
