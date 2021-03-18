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
import java.util.Iterator;
import java.util.List;

/**
 * Represents an immutable (read-only) observable set.
 *
 * @param <T> The type of elements in the set.
 *
 * @author Alex Andres
 */
public class ImmutableObservableSet<T> implements ObservableSet<T> {

	/** The SetChangeListener's for this immutable set. */
	private final transient List<SetChangeListener<ObservableSet<T>>> listeners = new ArrayList<>();

	/** The listener observing the backing set. */
	private final transient SetChangeListener<ObservableSet<T>> listener;

	/** The backing set. */
	private final ObservableSet<T> set;


	/**
	 * Creates a new {@code ImmutableObservableSet} that is backed by the
	 * provided set.
	 *
	 * @param set The backing set.
	 */
	public ImmutableObservableSet(ObservableSet<T> set) {
		this.listener = changedSet -> notifyChanged();
		this.set = set;
		this.set.addListener(new WeakSetChangeListener<>(listener));
	}

	@Override
	public void addListener(SetChangeListener<ObservableSet<T>> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(SetChangeListener<ObservableSet<T>> listener) {
		listeners.remove(listener);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {

			private final Iterator<T> backingIter = set.iterator();


			@Override
			public boolean hasNext() {
				return backingIter.hasNext();
			}

			@Override
			public T next() {
				return backingIter.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	private void notifyChanged() {
		for (SetChangeListener<ObservableSet<T>> listener : listeners) {
			listener.setChanged(this);
		}
	}
}
