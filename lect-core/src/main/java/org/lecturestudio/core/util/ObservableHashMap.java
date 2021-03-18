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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link ObservableMap} implementation backed by the {@link HashMap}.
 * 
 * @author Alex Andres
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class ObservableHashMap<K, V> extends HashMap<K, V> implements ObservableMap<K, V> {

	private static final long serialVersionUID = 1390495316242703829L;

	private final transient List<MapChangeListener<K, V>> listeners = new ArrayList<>();


	@Override
	public void clear() {
		int oldSize = size();

		super.clear();

		if (oldSize != 0) {
			notifyMapChanged();
		}
	}

	@Override
	public V put(K key, V value) {
		V put = super.put(key, value);

		if (isNull(put)) {
			notifyAdded(key, value);
		}
		else if (nonNull(value) && !value.equals(put)) {
			notifyChanged(key, value);
		}

		return put;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		super.putAll(map);

		notifyMapChanged();
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		V removed = super.remove(key);

		if (nonNull(removed)) {
			notifyRemoved((K) key, removed);
		}

		return removed;
	}

	@Override
	public void addListener(MapChangeListener<K, V> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(MapChangeListener<K, V> listener) {
		listeners.remove(listener);
	}

	private void notifyAdded(K key, V value) {
		for (MapChangeListener<K, V> listener : listeners) {
			listener.mapEntryAdded(this, key, value);
		}
	}

	private void notifyChanged(K key, V value) {
		for (MapChangeListener<K, V> listener : listeners) {
			listener.mapEntryChanged(this, key, value);
		}
	}

	private void notifyRemoved(K key, V value) {
		for (MapChangeListener<K, V> listener : listeners) {
			listener.mapEntryAdded(this, key, value);
		}
	}

	private void notifyMapChanged() {
		for (MapChangeListener<K, V> listener : listeners) {
			listener.mapChanged(this);
		}
	}

}
