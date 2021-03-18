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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * An {@link ObservableSet} implementation backed by the {@link HashSet}.
 * 
 * @author Alex Andres
 *
 * @param <T> The type of elements in the set.
 */
public class ObservableHashSet<T> extends HashSet<T> implements ObservableSet<T> {

	private static final long serialVersionUID = 1390495316242703829L;

	private final transient List<SetChangeListener<ObservableSet<T>>> listeners = new ArrayList<>();


	@Override
	public boolean add(T element) {
		boolean added = super.add(element);

		if (added) {
			notifyChanged();
		}

		return added;
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		boolean added = super.addAll(collection);

		if (added) {
			notifyChanged();
		}

		return added;
	}

	@Override
	public void clear() {
		int oldSize = size();

		super.clear();

		if (oldSize != 0) {
			notifyChanged();
		}
	}

	@Override
	public boolean remove(Object object) {
		boolean removed = super.remove(object);

		if (removed) {
			notifyChanged();
		}

		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removed = super.removeAll(c);

		if (removed) {
			notifyChanged();
		}

		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = super.retainAll(c);

		if (changed) {
			notifyChanged();
		}

		return changed;
	}

	@Override
	public void addListener(SetChangeListener<ObservableSet<T>> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(SetChangeListener<ObservableSet<T>> listener) {
		listeners.remove(listener);
	}

	private void notifyChanged() {
		for (SetChangeListener<ObservableSet<T>> listener : listeners) {
			listener.setChanged(this);
		}
	}

}
