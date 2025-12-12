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
	 * Check whether the interval is empty.
	 * <p>
	 * An interval is considered empty when its start and end values are equal.
	 * Two {@code null} values are treated as equal (both {@code null} = empty).
	 *
	 * @return {@code true} if {@code start} equals {@code end}, otherwise {@code false}.
	 */
	public boolean isEmpty() {
		return Objects.equals(start, end);
	}

	/**
	 * Specifies whether the given value is lying within the interval.
	 *
	 * @param value The value to be checked.
	 *
	 * @return {@code true} if the specified value is greater or equal to the start value
	 * and less than or equal to the end value, otherwise {@code false}.
	 */
	public boolean contains(long value) {
		return (value >= start.longValue() && value <= end.longValue());
	}

	/**
	 * Specifies whether the given interval is completely lying within this interval.
	 *
	 * @param value The interval to be checked.
	 *
	 * @return {@code true} if this interval contains the start and end value of the specified interval,
	 * otherwise {@code false}.
	 *
	 * @see #contains(long)
	 */
	public boolean contains(Interval<T> value) {
		return contains(value.start.longValue()) && contains(value.end.longValue());
	}

	/**
	 * Returns the length of the interval as an integer.
	 *
	 * @return The length of the interval as an integer.
	 */
	public int lengthInt() {
		return end.intValue() - start.intValue();
	}

	/**
	 * Returns the length of the interval as a long.
	 *
	 * @return The length of the interval as a long.
	 */
	public long lengthLong() {
		return end.longValue() - start.longValue();
	}

	@Override
	public int compareTo(Interval<T> o) {
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
		
		return (start.equals(other.start) && end.equals(other.end));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + start + ", " + end + "]";
	}
}
