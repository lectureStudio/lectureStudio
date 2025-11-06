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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.lecturestudio.core.model.Interval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RandomAccessStreamExtendedTest {

	@TempDir
	Path tempDir;

	private File testFile;
	private RandomAccessStream stream;

	@BeforeEach
	void setUp() throws IOException {
		// Create a test file with known content
		testFile = tempDir.resolve("test.txt").toFile();
		byte[] testData = new byte[1000];
		for (int i = 0; i < testData.length; i++) {
			testData[i] = (byte) (i % 256);
		}
		Files.write(testFile.toPath(), testData);
		
		stream = new RandomAccessStream(testFile);
	}

	@Test
	void testConstructorWithNullFile() {
		assertThrows(NullPointerException.class, () -> new RandomAccessStream(null));
	}

	@Test
	void testConstructorWithNonExistentFile() {
		File nonExistentFile = new File("non-existent-file.txt");
		assertThrows(IOException.class, () -> new RandomAccessStream(nonExistentFile));
	}

	@Test
	void testConstructorWithSpecificRange() throws IOException {
		RandomAccessStream rangeStream = new RandomAccessStream(testFile, 100L, 200L);
		assertNotNull(rangeStream);
		assertEquals(200, rangeStream.available());
	}

	@Test
	void testAvailableWithExclusions() {
		// Add some exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		int available = stream.available();
		long excludedLength = stream.getExcludedLength();
		
		// Available should be total length minus excluded length
		assertEquals(1000 - excludedLength, available);
	}

	@Test
	void testCloneWithExclusions() throws IOException {
		// Add exclusions to the original stream
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		RandomAccessStream clone = stream.clone();
		assertNotNull(clone);
		assertNotSame(stream, clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithDifferentFile() throws IOException {
		// Create another test file
		File anotherFile = tempDir.resolve("another.txt").toFile();
		byte[] anotherData = new byte[500];
		for (int i = 0; i < anotherData.length; i++) {
			anotherData[i] = (byte) (i % 128);
		}
		Files.write(anotherFile.toPath(), anotherData);
		
		RandomAccessStream anotherStream = new RandomAccessStream(anotherFile);
		anotherStream.addExclusion(new Interval<>(5L, 15L));
		
		RandomAccessStream clone = anotherStream.clone();
		assertNotNull(clone);
		
		// Clone should work with different file
		assertDoesNotThrow(() -> clone.read(new byte[10]));
		
		// Close streams to prevent resource leaks
		anotherStream.close();
		clone.close();
	}

	@Test
	void testResetWithStartPointer() throws IOException {
		// Create a stream with a start pointer
		RandomAccessStream rangeStream = new RandomAccessStream(testFile, 100L, 200L);
		
		// Read some data
		byte[] buffer = new byte[50];
		rangeStream.read(buffer);
		
		// Reset should go back to the start pointer
		rangeStream.reset();
		assertEquals(200, rangeStream.available());
		
		// Close stream to prevent resource leak
		rangeStream.close();
	}

	@Test
	void testResetWithExclusions() throws IOException {
		// Add exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		// Read some data
		stream.read(new byte[50]);
		
		// Reset should restore exclusions
		stream.reset();
		assertEquals(2, stream.getExclusions().size());
		assertEquals(20L, stream.getExcludedLength());
	}

	@Test
	void testConcurrentAccess() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(5);
		AtomicInteger successCount = new AtomicInteger(0);
		ExecutorService executor = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 5; i++) {
			final int threadId = i;
			executor.submit(() -> {
				try {
					for (int j = 0; j < 20; j++) {
						// Add exclusions
						stream.addExclusion(new Interval<>((long) (threadId * 20 + j), (long) (threadId * 20 + j + 1)));
						
						// Read data
						byte[] buffer = new byte[10];
						stream.read(buffer);
						
						// Check available
						int available = stream.available();
						assertTrue(available >= 0);
						
						successCount.incrementAndGet();
					}
				} catch (Exception e) {
					fail("Concurrent access test failed: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();
		
		// All operations should have succeeded
		assertEquals(100, successCount.get());
	}

	@Test
	void testReadWithExclusions() throws IOException {
		// Add exclusions that skip some data
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		byte[] buffer = new byte[50];
		int read = stream.read(buffer);
		
		// Should read data, skipping excluded ranges
		assertTrue(read > 0);
		assertTrue(read <= 50);
	}

	@Test
	void testSkipWithExclusions() throws IOException {
		// Add exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		long skipped = stream.skip(50);
		
		// Should skip the requested amount, accounting for exclusions
		assertTrue(skipped >= 0);
		assertTrue(skipped <= 50);
	}

	@Test
	void testPositionTracking() throws IOException {
		assertEquals(0L, stream.getPosition());
		
		stream.read(new byte[10]);
		assertEquals(10L, stream.getPosition());
		
		stream.skip(5);
		assertEquals(15L, stream.getPosition());
	}

	@Test
	void testExcludedLengthCalculation() {
		// Add overlapping exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(15L, 25L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		long excludedLength = stream.getExcludedLength();
		
		// Should be 25 (10-25 merged + 30-40) = 15 + 10 = 25
		assertEquals(25L, excludedLength);
	}

	@Test
	void testExcludedLengthWithAdjacentIntervals() {
		// Add adjacent exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(20L, 30L));
		stream.addExclusion(new Interval<>(40L, 50L));
		
		long excludedLength = stream.getExcludedLength();
		
		// Should be 30 (10-30 merged + 40-50) = 20 + 10 = 30
		assertEquals(30L, excludedLength);
	}

	@Test
	void testExcludedLengthWithNestedIntervals() {
		// Add nested exclusions
		stream.addExclusion(new Interval<>(10L, 30L));
		stream.addExclusion(new Interval<>(15L, 25L));
		stream.addExclusion(new Interval<>(40L, 50L));
		
		long excludedLength = stream.getExcludedLength();
		
		// Should be 30 (10-30 + 40-50) = 20 + 10 = 30
		assertEquals(30L, excludedLength);
	}

	@Test
	void testExcludedLengthWithEmptyList() {
		// No exclusions
		long excludedLength = stream.getExcludedLength();
		assertEquals(0L, excludedLength);
	}

	@Test
	void testExcludedLengthWithSingleInterval() {
		// Single exclusion
		stream.addExclusion(new Interval<>(10L, 20L));
		long excludedLength = stream.getExcludedLength();
		assertEquals(10L, excludedLength);
	}

	@Test
	void testExcludedLengthWithZeroLengthInterval() {
		// Zero-length exclusion
		stream.addExclusion(new Interval<>(10L, 10L));
		long excludedLength = stream.getExcludedLength();
		assertEquals(0L, excludedLength);
	}

	@Test
	void testExcludedLengthWithNegativeLengthInterval() {
		// Negative-length exclusion (end < start) should be rejected
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(20L, 10L)));
		
		// No exclusions should have been added
		assertEquals(0, stream.getExclusions().size());
		assertEquals(0L, stream.getExcludedLength());
	}
}
