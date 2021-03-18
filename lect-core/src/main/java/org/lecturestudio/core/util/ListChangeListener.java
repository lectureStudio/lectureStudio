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

/**
 * The listener that is called when changes to the {@link ObservableList} occur.
 * 
 * @author Alex Andres
 *
 * @param <T>
 */
public interface ListChangeListener<T extends ObservableList<?>> {

	/**
	 * Called whenever one or more elements in the list have changed.
	 * 
	 * @param list The changed list.
	 * @param startIndex The starting index of the first changed element.
	 * @param itemCount The number of changed elements.
	 */
	default void listItemsChanged(T list, int startIndex, int itemCount) {
		listChanged(list);
	}

	/**
	 * Called whenever elements have been inserted into the list.
	 * 
	 * @param list The changed list.
	 * @param startIndex The position of the first inserted element.
	 * @param itemCount The number of inserted elements.
	 */
	default void listItemsInserted(T list, int startIndex, int itemCount) {
		listChanged(list);
	}

	/**
	 * Called whenever elements in the list have been moved.
	 * 
	 * @param list The changed list.
	 * @param startIndex The starting index from which the elements were moved.
	 * @param destIndex The new position of the elements.
	 * @param itemCount The number of moved elements.
	 */
	default void listItemsMoved(T list, int startIndex, int destIndex, int itemCount) {
		listChanged(list);
	}

	/**
	 * Called whenever elements in the list have been deleted.
	 * 
	 * @param list The changed list.
	 * @param startIndex The starting index of the first deleted element.
	 * @param itemCount The number of removed elements.
	 */
	default void listItemsRemoved(T list, int startIndex, int itemCount) {
		listChanged(list);
	}

	/**
	 * Called whenever items of the list have changed. This method is by default
	 * a no-op.
	 *
	 * @param list The changed list.
	 */
	default void listChanged(T list) {

	}

}
