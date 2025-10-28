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

package org.lecturestudio.core.model;

import java.util.Objects;

/**
 *
 * @param <T> The {@link Number} type of this interval.
 *
 * @author Alex Andres
 */
public class Interval<T extends Number> implements Comparable<Interval<T>> {

	/** The start value of the interval. */
	private T start;

	/** The end value of the interval. */
	private T end;


	/**
	 * Create a new {@link Interval}.
	 * (Calls {@link #set(Number, Number)} with {@code null} as start and end.)
	 */
	public Interval() {
		set(null, null);
	}

	/**
	 * Create new {@link Interval} with the specified start and end value.
	 *
	 * @param start The start value.
	 * @param end The end value.
	 */
	public Interval(T start, T end) {
		set(start, end);
	}

	/**
	 * Set a new start and end value.
	 *
	 * @param start The new start value.
	 * @param end The new end value.
	 */
	public void set(T start, T end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Get the start value.
	 *
	 * @return The start value.
	 */
	public T getStart() {
		return start;
	}

	/**
	 * Get the end value.
	 *
	 * @return The end value.
	 */
	public T getEnd() {
		return end;
	}

	/**
	 * Specifies whether the given value is lying within the interval.
	 *
	 * @param value The value to be checked.
	 *
	 * @return {@code true} if the specified value is greater or equal to the start value
	 * and less than or equal to the end value, otherwise {@code false}.
	 * @throws IllegalStateException if start or end is null
	 */
	public boolean contains(long value) {
		if (start == null || end == null) {
			throw new IllegalStateException("Interval start or end is null");
		}
		return (value >= start.longValue() && value <= end.longValue());
	}

	/**
	 * Specifies whether the given interval is completely lying within this interval.
	 *
	 * @param value The interval to be checked.
	 *
	 * @return {@code true} if this interval contains the start and end value of the specified interval,
	 * otherwise {@code false}.
	 * @throws IllegalStateException if start or end is null
	 * @throws IllegalArgumentException if value is null or has null start/end
	 *
	 * @see #contains(long)
	 */
	public boolean contains(Interval<T> value) {
		if (value == null) {
			throw new IllegalArgumentException("Interval value cannot be null");
		}
		if (value.start == null || value.end == null) {
			throw new IllegalArgumentException("Interval value start or end cannot be null");
		}
		return contains(value.start.longValue()) && contains(value.end.longValue());
	}

	/**
	 * Returns the length of the interval as an integer.
	 *
	 * @return The length of the interval as an integer.
	 * @throws IllegalStateException if start or end is null
	 */
	public int lengthInt() {
		if (start == null || end == null) {
			throw new IllegalStateException("Interval start or end is null");
		}
		return end.intValue() - start.intValue();
	}

	/**
	 * Returns the length of the interval as a long.
	 *
	 * @return The length of the interval as a long.
	 * @throws IllegalStateException if start or end is null
	 */
	public long lengthLong() {
		if (start == null || end == null) {
			throw new IllegalStateException("Interval start or end is null");
		}
		return end.longValue() - start.longValue();
	}

	@Override
	public int compareTo(Interval<T> o) {
		if (o == null) {
			throw new IllegalArgumentException("Cannot compare with null interval");
		}
		if (this.start == null || this.end == null || o.start == null || o.end == null) {
			throw new IllegalStateException("Cannot compare intervals with null start or end values");
		}
		
		if (this.start.doubleValue() < o.start.doubleValue()) {
			return -1;
		}
		else if (this.start.doubleValue() > o.start.doubleValue()) {
			return +1;
		}
		else if (this.end.doubleValue() < o.end.doubleValue()) {
			return -1;
		}
		else if (this.end.doubleValue() > o.end.doubleValue()) {
			return +1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Interval<?> other = (Interval<?>) obj;
		
		if (start == null && other.start != null) {
			return false;
		}
		if (end == null && other.end != null) {
			return false;
		}
		
		return (Objects.equals(start, other.start) && Objects.equals(end, other.end));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + start + ", " + end + "]";
	}
}
