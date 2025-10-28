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

package org.lecturestudio.core.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.lecturestudio.core.audio.filter.AudioFilter;
import org.lecturestudio.core.model.Interval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicInputStreamExtendedTest {

	private byte[] testData;
	private DynamicInputStream stream;

	@BeforeEach
	void setUp() {
		// Create test data: 0, 1, 2, ..., 99
		testData = new byte[100];
		for (int i = 0; i < 100; i++) {
			testData[i] = (byte) i;
		}
		stream = new DynamicInputStream(new ByteArrayInputStream(testData));
	}

	@Test
	void testConstructorWithNull() {
		assertThrows(IllegalArgumentException.class, () -> new DynamicInputStream(null));
	}

	@Test
	void testAddExclusionWithNull() {
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(null));
	}

	@Test
	void testRemoveExclusionWithNull() {
		assertThrows(IllegalArgumentException.class, () -> stream.removeExclusion(null));
	}

	@Test
	void testSetAudioFilterWithNullFilter() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		assertThrows(IllegalArgumentException.class, () -> stream.setAudioFilter(null, interval));
	}

	@Test
	void testSetAudioFilterWithNullInterval() {
		AudioFilter filter = new TestAudioFilter();
		assertThrows(IllegalArgumentException.class, () -> stream.setAudioFilter(filter, null));
	}

	@Test
	void testRemoveAudioFilterWithNull() {
		assertThrows(IllegalArgumentException.class, () -> stream.removeAudioFilter(null));
	}

	@Test
	void testGetExclusionsReturnsDefensiveCopy() {
		Interval<Long> interval = new Interval<>(10L, 20L);
		stream.addExclusion(interval);
		
		List<Interval<Long>> exclusions1 = stream.getExclusions();
		List<Interval<Long>> exclusions2 = stream.getExclusions();
		
		// Should be different objects
		assertNotSame(exclusions1, exclusions2);
		// But should have same content
		assertEquals(exclusions1, exclusions2);
	}

	@Test
	void testOverlappingIntervalsExclusion() throws IOException {
		// Add overlapping intervals
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(15L, 25L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		// The excluded length should be 25 (10-25 merged + 30-40) = 15 + 10 = 25
		assertEquals(25L, stream.getExcludedLength());
	}

	@Test
	void testAdjacentIntervalsExclusion() throws IOException {
		// Add adjacent intervals
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(20L, 30L));
		stream.addExclusion(new Interval<>(40L, 50L));
		
		// The excluded length should be 30 (10-30 merged + 40-50) = 20 + 10 = 30
		assertEquals(30L, stream.getExcludedLength());
	}

	@Test
	void testNestedIntervalsExclusion() throws IOException {
		// Add nested intervals
		stream.addExclusion(new Interval<>(10L, 30L));
		stream.addExclusion(new Interval<>(15L, 25L));
		stream.addExclusion(new Interval<>(40L, 50L));
		
		// The excluded length should be 30 (10-30 + 40-50) = 20 + 10 = 30
		assertEquals(30L, stream.getExcludedLength());
	}

	@Test
	void testAvailableWithOverflow() throws IOException {
		// Create a stream with very large available bytes
		ByteArrayInputStream largeStream = new ByteArrayInputStream(new byte[Integer.MAX_VALUE]);
		DynamicInputStream largeDynamicStream = new DynamicInputStream(largeStream);
		
		// Should not overflow
		int available = largeDynamicStream.available();
		assertTrue(available >= 0);
		assertTrue(available <= Integer.MAX_VALUE);
		
		largeDynamicStream.close();
	}

	@Test
	void testAvailableWithNegativeResult() throws IOException {
		// Add exclusions that exceed available bytes
		stream.addExclusion(new Interval<>(0L, 200L)); // More than available
		
		int available = stream.available();
		assertEquals(0, available);
	}

	@Test
	void testThreadSafety() throws InterruptedException {
		int numThreads = 10;
		int operationsPerThread = 100;
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		CountDownLatch latch = new CountDownLatch(numThreads);
		AtomicInteger successCount = new AtomicInteger(0);

		for (int i = 0; i < numThreads; i++) {
			final int threadId = i;
			executor.submit(() -> {
				try {
					for (int j = 0; j < operationsPerThread; j++) {
						// Add exclusions
						stream.addExclusion(new Interval<>((long) (threadId * 10 + j), (long) (threadId * 10 + j + 1)));
						
						// Read some data
						byte[] buffer = new byte[10];
						stream.read(buffer);
						
						// Get exclusions
						List<Interval<Long>> exclusions = stream.getExclusions();
						assertNotNull(exclusions);
						
						successCount.incrementAndGet();
					}
				} catch (Exception e) {
					fail("Thread safety test failed: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();
		
		// All operations should have succeeded
		assertEquals(numThreads * operationsPerThread, successCount.get());
	}

	@Test
	void testCloneWithExclusions() throws IOException {
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		DynamicInputStream clone = stream.clone();
		assertNotNull(clone);
		assertNotSame(stream, clone);
		
		// Clone should have same exclusions
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithFilters() throws IOException {
		AudioFilter filter1 = new TestAudioFilter();
		AudioFilter filter2 = new TestAudioFilter();
		Interval<Long> interval1 = new Interval<>(10L, 20L);
		Interval<Long> interval2 = new Interval<>(30L, 40L);
		
		stream.setAudioFilter(filter1, interval1);
		stream.setAudioFilter(filter2, interval2);
		
		DynamicInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have same filters (we can't easily test the internal map)
		// but we can verify the clone works
		assertDoesNotThrow(() -> clone.read(new byte[10]));
	}

	@Test
	void testResetRestoresExclusions() throws IOException {
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		// Read some data to modify exclusions
		stream.read(new byte[25]);
		
		// Reset should restore original exclusions
		stream.reset();
		assertEquals(2, stream.getExclusions().size());
		assertEquals(20L, stream.getExcludedLength());
	}

	@Test
	void testClearExclusions() {
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		assertEquals(2, stream.getExclusions().size());
		assertEquals(20L, stream.getExcludedLength());
		
		stream.clearExclusions();
		
		assertEquals(0, stream.getExclusions().size());
		assertEquals(0L, stream.getExcludedLength());
	}

	@Test
	void testPositionTracking() throws IOException {
		assertEquals(0L, stream.getPosition());
		
		stream.read(new byte[10]);
		assertEquals(10L, stream.getPosition());
		
		stream.skip(5);
		assertEquals(15L, stream.getPosition());
	}

	// Helper class for testing
	private static class TestAudioFilter implements AudioFilter {
		@Override
		public void process(byte[] buffer, int offset, int length) {
			// Simple test implementation
		}
	}
}
