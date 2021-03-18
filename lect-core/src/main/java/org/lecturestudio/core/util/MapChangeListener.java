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
 * The listener that is called when changes to the {@link ObservableMap} occur.
 * 
 * @author Alex Andres
 * 
 * @param <K> The key type.
 * @param <V> The value type.
 */
public interface MapChangeListener<K, V> {

	/**
	 * Called whenever a new key-value pair is added.
	 *
	 * @param map The changed map.
	 * @param key The key.
	 * @param value The value.
	 */
	default void mapEntryAdded(ObservableMap<K, V> map, K key, V value) {
		mapChanged(map);
	}

	/**
	 * Called whenever a key-value pair has changed.
	 *
	 * @param map The changed map.
	 * @param key The key.
	 * @param value The value.
	 */
	default void mapEntryChanged(ObservableMap<K, V> map, K key, V value) {
		mapChanged(map);
	}

	/**
	 * Called whenever a key-value pair has been removed.
	 *
	 * @param map The changed map.
	 * @param key The key.
	 * @param value The value.
	 */
	default void mapEntryRemoved(ObservableMap<K, V> map, K key, V value) {
		mapChanged(map);
	}

	/**
	 * Called whenever items of the map have changed. This method is by default
	 * a no-op.
	 *
	 * @param map The changed map.
	 */
	default void mapChanged(ObservableMap<K, V> map) {

	}

}
