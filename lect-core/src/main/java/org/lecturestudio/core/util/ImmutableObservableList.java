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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an immutable (read-only) observable list.
 *
 * @param <T> The type of elements in the list.
 *
 * @author Alex Andres
 */
public class ImmutableObservableList<T> implements ObservableList<T> {

	/** The ListChangeListener's for this immutable list. */
	private final transient List<ListChangeListener<ObservableList<T>>> listeners;

	/** The listener observing the backing list. */
	private final transient ListChangeListener<ObservableList<T>> listener;

	/** The backing list. */
	private final ObservableList<T> list;

	/**
	 * Creates a new {@code ImmutableObservableList} that is backed by the
	 * provided list.
	 *
	 * @param list The backing list.
	 */
	public ImmutableObservableList(ObservableList<T> list) {
		listeners = new CopyOnWriteArrayList<>();
		listener = new ListChangeListener<>() {

			@Override
			public void listItemsChanged(ObservableList<T> list, int startIndex, int itemCount) {
				notifyChanged(startIndex, itemCount);
				listChanged(list);
			}

			@Override
			public void listItemsInserted(ObservableList<T> list, int startIndex, int itemCount) {
				notifyInserted(startIndex, itemCount);
				listChanged(list);
			}

			@Override
			public void listItemsRemoved(ObservableList<T> list, int startIndex, int itemCount) {
				notifyRemoved(startIndex, itemCount);
				listChanged(list);
			}
		};
		this.list = list;
		this.list.addListener(new WeakListChangeListener<>(listener));
	}

	@Override
	public void addListener(ListChangeListener<ObservableList<T>> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ListChangeListener<ObservableList<T>> listener) {
		listeners.remove(listener);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {

			private final Iterator<T> backingIter = list.iterator();


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
		return list.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return list.toArray(a);
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
		return new HashSet<>(list).containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return getListIterator(0);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return getListIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return this;
	}

	@Override
	public String toString() {
		return list.toString();
	}

	private ListIterator<T> getListIterator(int index) {
		return new ListIterator<>() {

			private final ListIterator<T> backingIter = list.listIterator(index);


			@Override
			public boolean hasNext() {
				return backingIter.hasNext();
			}

			@Override
			public T next() {
				return backingIter.next();
			}

			@Override
			public boolean hasPrevious() {
				return backingIter.hasPrevious();
			}

			@Override
			public T previous() {
				return backingIter.previous();
			}

			@Override
			public int nextIndex() {
				return backingIter.nextIndex();
			}

			@Override
			public int previousIndex() {
				return backingIter.previousIndex();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(T t) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(T t) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private void notifyInserted(int start, int count) {
		for (ListChangeListener<ObservableList<T>> listener : listeners) {
			listener.listItemsInserted(this, start, count);
		}
	}

	private void notifyRemoved(int start, int count) {
		for (ListChangeListener<ObservableList<T>> listener : listeners) {
			listener.listItemsRemoved(this, start, count);
		}
	}

	private void notifyChanged(int start, int count) {
		for (ListChangeListener<ObservableList<T>> listener : listeners) {
			listener.listItemsChanged(this, start, count);
		}
	}
}