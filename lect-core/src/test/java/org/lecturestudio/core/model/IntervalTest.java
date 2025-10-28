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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IntervalTest {

	@Test
	void testConstructorWithValues() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		assertEquals(10L, interval.getStart());
		assertEquals(20L, interval.getEnd());
	}

	@Test
	void testConstructorWithNulls() {
		Interval<Long> interval = new Interval<>();
		assertNull(interval.getStart());
		assertNull(interval.getEnd());
	}

	@Test
	void testSet() {
		Interval<Long> interval = new Interval<>();
		interval.set(5L, 15L);
		assertEquals(5L, interval.getStart());
		assertEquals(15L, interval.getEnd());
	}

	@Test
	void testContainsWithValidValues() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		assertTrue(interval.contains(10L));
		assertTrue(interval.contains(15L));
		assertTrue(interval.contains(20L));
		assertFalse(interval.contains(9L));
		assertFalse(interval.contains(21L));
	}

	@Test
	void testContainsWithNullStart() {
		Interval<Long> interval = new Interval<>();
		interval.set(null, 20L);
		
		assertThrows(IllegalStateException.class, () -> interval.contains(10L));
	}

	@Test
	void testContainsWithNullEnd() {
		Interval<Long> interval = new Interval<>();
		interval.set(10L, null);
		
		assertThrows(IllegalStateException.class, () -> interval.contains(10L));
	}

	@Test
	void testContainsIntervalWithValidValues() {
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>(12L, 18L);
		Interval<Long> interval3 = new Interval<>(5L, 15L);
		
		assertTrue(interval1.contains(interval2));
		assertFalse(interval1.contains(interval3));
	}

	@Test
	void testContainsIntervalWithNull() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		
		assertThrows(IllegalArgumentException.class, () -> interval.contains((Interval<Long>) null));
	}

	@Test
	void testContainsIntervalWithNullValues() {
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>();
		interval2.set(12L, null);
		
		assertThrows(IllegalArgumentException.class, () -> interval1.contains(interval2));
	}

	@Test
	void testLengthIntWithValidValues() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		assertEquals(10, interval.lengthInt());
	}

	@Test
	void testLengthIntWithNullValues() {
		Interval<Long> interval = new Interval<>();
		interval.set(10L, null);
		
		assertThrows(IllegalStateException.class, interval::lengthInt);
	}

	@Test
	void testLengthLongWithValidValues() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		assertEquals(10L, interval.lengthLong());
	}

	@Test
	void testLengthLongWithNullValues() {
		Interval<Long> interval = new Interval<>();
		interval.set(null, 20L);
		
		assertThrows(IllegalStateException.class, interval::lengthLong);
	}

	@Test
	void testCompareToWithValidValues() {
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>(15L, 25L);
		Interval<Long> interval3 = new Interval<>(10L, 30L);
		Interval<Long> interval4 = new Interval<>(10L, 20L);
		
		assertTrue(interval1.compareTo(interval2) < 0);
		assertTrue(interval2.compareTo(interval1) > 0);
		assertTrue(interval1.compareTo(interval3) < 0);
		assertEquals(0, interval1.compareTo(interval4));
	}

	@Test
	void testCompareToWithNull() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		
		assertThrows(IllegalArgumentException.class, () -> interval.compareTo(null));
	}

	@Test
	void testCompareToWithNullValues() {
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>();
		interval2.set(null, 20L);
		
		assertThrows(IllegalStateException.class, () -> interval1.compareTo(interval2));
	}

	@Test
	void testEquals() {
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>(10L, 20L);
		Interval<Long> interval3 = new Interval<>(15L, 25L);
		
		assertEquals(interval1, interval2);
		assertNotEquals(interval1, interval3);
		assertNotEquals(null, interval1);
	}

	@Test
	void testEqualsWithNullValues() {
		Interval<Long> interval1 = new Interval<>();
		Interval<Long> interval2 = new Interval<>();
		
		assertEquals(interval1, interval2);
	}

	@Test
	void testHashCode() {
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>(10L, 20L);
		Interval<Long> interval3 = new Interval<>(15L, 25L);
		
		assertEquals(interval1.hashCode(), interval2.hashCode());
		assertNotEquals(interval1.hashCode(), interval3.hashCode());
	}

	@Test
	void testToString() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		String result = interval.toString();
		assertTrue(result.contains("Interval"));
		assertTrue(result.contains("10"));
		assertTrue(result.contains("20"));
	}
}
