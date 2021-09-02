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
 * An {@link Observable} allows to observe arbitrary content types for changes. Each
 * time the observed value changes the registered listeners will be notified.
 *
 * @param <T> The type of the observed data.
 *
 * @author Alex Andres
 */
public interface Observable<T> {

	/**
	 * Adds the given {@link ChangeListener} to be notified whenever the value of this
	 * {@link Observable} has changed.
	 *
	 * @param listener The listener to register.
	 *
	 * @throws NullPointerException If the listener is null.
	 */
	void addListener(ChangeListener<? super T> listener);

	/**
	 * Removes the given {@link ChangeListener} from the listener list.
	 *
	 * @param listener The listener to remove.
	 */
	void removeListener(ChangeListener<? super T> listener);

}
