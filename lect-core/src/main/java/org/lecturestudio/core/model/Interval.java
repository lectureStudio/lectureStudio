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

/**
 *
 * @param <T> The {@link Number} type of this interval.
 *
 * @author Alex Andres
 */
public class Interval<T extends Number> implements Comparable<Interval<T>> {
	
	private T start;
	
	private T end;
	
	
	public Interval() {
		set(null, null);
	}
	
	public Interval(T start, T end) {
		set(start, end);
	}
	
	public void set(T start, T end) {
		this.start = start;
		this.end = end;
	}
	
	public T getStart() {
		return start;
	}

	public T getEnd() {
		return end;
	}
	
	public boolean contains(long value) {
		return (value >= start.longValue() && value <= end.longValue());
	}

	public boolean contains(Interval<T> value) {
		return contains(value.start.longValue()) && contains(value.end.longValue());
	}

	public int lengthInt() {
		return end.intValue() - start.intValue();
	}

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
