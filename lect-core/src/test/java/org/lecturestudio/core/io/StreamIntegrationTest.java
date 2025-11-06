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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.filter.AudioFilter;
import org.lecturestudio.core.model.Interval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StreamIntegrationTest {

	@TempDir
	Path tempDir;

	private byte[] testData;
	private File testFile;

	@BeforeEach
	void setUp() throws IOException {
		// Create test data: 0, 1, 2, ..., 999
		testData = new byte[1000];
		for (int i = 0; i < testData.length; i++) {
			testData[i] = (byte) (i % 256);
		}
		
		// Create a test file
		testFile = tempDir.resolve("test.bin").toFile();
		Files.write(testFile.toPath(), testData);
	}

	@Test
	void testDynamicInputStreamWithRandomAccessStream() throws IOException {
		RandomAccessStream randomAccessStream = new RandomAccessStream(testFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		
		// Add exclusions
		dynamicStream.addExclusion(new Interval<>(100L, 200L));
		dynamicStream.addExclusion(new Interval<>(300L, 400L));
		
		// Test reading with exclusions
		byte[] buffer = new byte[500];
		int read = dynamicStream.read(buffer);
		
		assertTrue(read > 0);
		assertTrue(read <= 500);
		
		// Test available
		int available = dynamicStream.available();
		assertTrue(available >= 0);
		
		// Test excluded length
		long excludedLength = dynamicStream.getExcludedLength();
		assertEquals(200L, excludedLength);
		
		dynamicStream.close();
	}

	@Test
	void testRandomAccessAudioStreamWithDynamicInputStream() throws IOException {
		DynamicInputStream dynamicStream = new DynamicInputStream(new ByteArrayInputStream(testData));
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(dynamicStream, true);
		
		// Set audio format
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		audioStream.setAudioFormat(audioFormat);
		
		// Add exclusions in milliseconds
		audioStream.addExclusionMillis(1000L, 2000L);
		audioStream.addExclusionMillis(3000L, 4000L);
		
		// Test reading
		byte[] buffer = new byte[100];
		int read = audioStream.read(buffer);
		
		assertTrue(read > 0);
		assertTrue(read <= 100);
		
		// Test available
		int available = audioStream.available();
		assertTrue(available >= 0);
		
		// Test length in milliseconds
		long lengthInMillis = audioStream.getLengthInMillis();
		assertTrue(lengthInMillis >= 0);
		
		audioStream.close();
	}

	@Test
	void testDynamicByteArrayInputStreamWithAudioFilters() throws Exception {
		DynamicByteArrayInputStream byteArrayStream = new DynamicByteArrayInputStream(testData);
		
		// Add exclusions
		byteArrayStream.addExclusion(new Interval<>(100L, 200L));
		byteArrayStream.addExclusion(new Interval<>(300L, 400L));
		
		// Add audio filters
		TestAudioFilter filter1 = new TestAudioFilter();
		TestAudioFilter filter2 = new TestAudioFilter();
		byteArrayStream.setAudioFilter(filter1, new Interval<>(50L, 150L));
		byteArrayStream.setAudioFilter(filter2, new Interval<>(250L, 350L));
		
		// Test reading
		byte[] buffer = new byte[500];
		int read = byteArrayStream.read(buffer);
		
		assertTrue(read > 0);
		assertTrue(read <= 500);
		
		// Test that filters were called
		assertTrue(filter1.getProcessCount() > 0);
		assertTrue(filter2.getProcessCount() > 0);
		
		byteArrayStream.close();
	}

	@Test
	void testChainedStreams() throws IOException {
		// Create a chain: RandomAccessStream -> DynamicInputStream -> RandomAccessAudioStream
		RandomAccessStream randomAccessStream = new RandomAccessStream(testFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(dynamicStream, true);
		
		// Set audio format
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		audioStream.setAudioFormat(audioFormat);
		
		// Add exclusions at different levels
		dynamicStream.addExclusion(new Interval<>(100L, 200L));
		audioStream.addExclusionMillis(1000L, 2000L);
		
		// Test reading
		byte[] buffer = new byte[100];
		int read = audioStream.read(buffer);
		
		assertTrue(read > 0);
		assertTrue(read <= 100);
		
		// Test that exclusions are applied
		long excludedLength = dynamicStream.getExcludedLength();
		assertTrue(excludedLength > 0);
		
		audioStream.close();
	}

	@Test
	void testConcurrentAccessToChainedStreams() throws InterruptedException, IOException {
		RandomAccessStream randomAccessStream = new RandomAccessStream(testFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(dynamicStream, true);
		
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		audioStream.setAudioFormat(audioFormat);
		
		CountDownLatch latch = new CountDownLatch(5);
		AtomicInteger successCount = new AtomicInteger(0);
		ExecutorService executor = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 5; i++) {
			final int threadId = i;
			executor.submit(() -> {
				try {
					for (int j = 0; j < 20; j++) {
						// Add exclusions
						dynamicStream.addExclusion(new Interval<>((long) (threadId * 50 + j), (long) (threadId * 50 + j + 1)));
						audioStream.addExclusionMillis(threadId * 1000L + j * 100L, threadId * 1000L + j * 100L + 50L);
						
						// Read data
						byte[] buffer = new byte[10];
						audioStream.read(buffer);
						
						// Check state
						audioStream.getLength();
						audioStream.getPosition();
						audioStream.available();
						
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
		
		try {
			audioStream.close();
		} catch (IOException e) {
			// Ignore close exceptions in test
		}
	}

	@Test
	void testCloneChainedStreams() throws IOException {
		RandomAccessStream randomAccessStream = new RandomAccessStream(testFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(dynamicStream, true);
		
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		audioStream.setAudioFormat(audioFormat);
		
		// Add exclusions
		dynamicStream.addExclusion(new Interval<>(100L, 200L));
		audioStream.addExclusionMillis(1000L, 2000L);
		
		// Clone the audio stream
		RandomAccessAudioStream clone = audioStream.clone();
		assertNotNull(clone);
		assertNotSame(audioStream, clone);
		
		// Clone should have the same exclusions
		assertEquals(1, clone.getExclusions().size());
		assertTrue(clone.getExcludedLength() > 0);
		
		// Clone should have the same audio format
		assertEquals(audioFormat, clone.getAudioFormat());
		
		// Test that clone works independently
		byte[] buffer1 = new byte[50];
		byte[] buffer2 = new byte[50];
		
		int read1 = audioStream.read(buffer1);
		int read2 = clone.read(buffer2);
		
		assertTrue(read1 > 0);
		assertTrue(read2 > 0);
		
		audioStream.close();
		clone.close();
	}

	@Test
	void testResetChainedStreams() throws IOException {
		RandomAccessStream randomAccessStream = new RandomAccessStream(testFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(dynamicStream, true);
		
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		audioStream.setAudioFormat(audioFormat);
		
		// Add exclusions
		dynamicStream.addExclusion(new Interval<>(100L, 200L));
		audioStream.addExclusionMillis(1000L, 2000L);
		
		// Read some data
		byte[] buffer = new byte[100];
		audioStream.read(buffer);
		
		// Reset
		audioStream.reset();
		
		// Should be back to the beginning
		assertEquals(0L, audioStream.getPosition());
		
		// Should have same exclusions
		assertEquals(1, audioStream.getExclusions().size());
		assertTrue(audioStream.getExcludedLength() > 0);
		
		audioStream.close();
	}

	@Test
	void testErrorHandlingInChainedStreams() {
		// Test with an invalid file
		File invalidFile = new File("non-existent-file.bin");
		assertThrows(IOException.class, () -> new RandomAccessStream(invalidFile));
		
		// Test with null input
		assertThrows(IllegalArgumentException.class, () -> new DynamicInputStream(null));
		assertThrows(IllegalArgumentException.class, () -> new RandomAccessAudioStream((DynamicInputStream) null));
		assertThrows(IllegalArgumentException.class, () -> new RandomAccessAudioStream((File) null));
	}

	@Test
	void testMemoryUsageWithLargeData() throws IOException {
		// Create large test data
		byte[] largeData = new byte[10000];
		for (int i = 0; i < largeData.length; i++) {
			largeData[i] = (byte) (i % 256);
		}
		
		File largeFile = tempDir.resolve("large.bin").toFile();
		Files.write(largeFile.toPath(), largeData);
		
		RandomAccessStream randomAccessStream = new RandomAccessStream(largeFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(dynamicStream, true);
		
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		audioStream.setAudioFormat(audioFormat);
		
		// Add many exclusions
		for (int i = 0; i < 100; i++) {
			dynamicStream.addExclusion(new Interval<>((long) (i * 50), (long) (i * 50 + 10)));
		}
		
		// Test that it still works
		byte[] buffer = new byte[1000];
		int read = audioStream.read(buffer);
		
		assertTrue(read > 0);
		assertTrue(read <= 1000);
		
		// Test excluded length calculation
		long excludedLength = dynamicStream.getExcludedLength();
		assertTrue(excludedLength > 0);
		
		audioStream.close();
	}

	@Test
	void testPerformanceWithManyExclusions() throws IOException {
		RandomAccessStream randomAccessStream = new RandomAccessStream(testFile);
		DynamicInputStream dynamicStream = new DynamicInputStream(randomAccessStream);
		
		try {
			// Add many exclusions
			List<Interval<Long>> exclusions = new ArrayList<>();
			for (int i = 0; i < 500; i++) {
				Interval<Long> exclusion = new Interval<>((long) (i * 2), (long) (i * 2 + 1));
				exclusions.add(exclusion);
				dynamicStream.addExclusion(exclusion);
			}
			
			// Test that excluded length calculation is efficient
			long startTime = System.currentTimeMillis();
			long excludedLength = dynamicStream.getExcludedLength();
			long endTime = System.currentTimeMillis();
			
			// Should complete quickly (less than 100ms)
			assertTrue(endTime - startTime < 100);
			
			// Should calculate correctly
			assertEquals(500L, excludedLength);
		} finally {
			dynamicStream.close();
		}
	}

	// Helper class for testing
	private static class TestAudioFilter implements AudioFilter {
		private int processCount = 0;
		
		@Override
		public void process(byte[] buffer, int offset, int length) {
			processCount++;
		}
		
		public int getProcessCount() {
			return processCount;
		}
	}
}
