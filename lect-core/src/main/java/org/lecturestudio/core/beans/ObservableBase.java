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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observable base implementation that manages the change listeners and event
 * notifications.
 *
 * @param <T> The type of the observed data.
 *
 * @author Alex Andres
 */
public abstract class ObservableBase<T> implements Observable<T> {

	/** The registered change listeners. */
	private final List<ChangeListener<? super T>> listeners = new CopyOnWriteArrayList<>();


	@Override
	public void addListener(ChangeListener<? super T> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ChangeListener<? super T> listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify the change listeners that the observed value has changed.
	 *
	 * @param observable The {@link Observable} that caused the event.
	 * @param oldValue   The old value.
	 * @param newValue   The new value.
	 */
	protected void fireChange(Observable<T> observable, T oldValue, T newValue) {
		for (ChangeListener<? super T> listener : listeners) {
			listener.changed(observable, oldValue, newValue);
		}
	}

}
