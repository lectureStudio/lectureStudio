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

import java.util.List;

/**
 * A {@link List} that notifies when changes occur.
 * 
 * @author Alex Andres
 *
 * @param <T>
 */
public interface ObservableList<T> extends List<T> {

	/**
	 * Adds a listener to be notified when changes to the list occur.
	 * 
	 * @param listener The listener to be notified on list changes.
	 */
	void addListener(ListChangeListener<ObservableList<T>> listener);

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener The listener to remove.
	 */
	void removeListener(ListChangeListener<ObservableList<T>> listener);
	
}
