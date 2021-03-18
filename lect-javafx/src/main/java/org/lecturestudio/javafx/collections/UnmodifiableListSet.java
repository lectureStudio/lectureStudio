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

package org.lecturestudio.javafx.collections;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;

/**
 * A special unmodifiable implementation of Set which wraps a List.
 * <strong>It does not check for uniqueness!</strong> There are
 * several places in our implementation (Node.lookupAll and
 * ObservableSetWrapper are two such places) where we want to use
 * a List for speed of insertion and will be in a position to ensure
 * that the List is unique without having the overhead of hashing,
 * but want to present an unmodifiable Set in the public API.
 *
 * COPY of: com.sun.javafx.collections.UnmodifiableListSet
 */
public class UnmodifiableListSet<E> extends AbstractSet<E> {

	private final List<E> backingList;


	public UnmodifiableListSet(List<E> backingList) {
		if (backingList == null) {
			throw new NullPointerException();
		}

		this.backingList = backingList;
	}

	/**
	 * Required implementation that returns an iterator. Note that I
	 * don't just return backingList.iterator() because doing so would
	 * open up a whole through which developers could remove items from
	 * this supposedly unmodifiable set. So the iterator is wrapped
	 * such that it throws an exception on remove.
	 */
	@Override
	public Iterator<E> iterator() {
		final Iterator<E> itr = backingList.iterator();

		return new Iterator<E>() {

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public E next() {
				return itr.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public int size() {
		return backingList.size();
	}

}
