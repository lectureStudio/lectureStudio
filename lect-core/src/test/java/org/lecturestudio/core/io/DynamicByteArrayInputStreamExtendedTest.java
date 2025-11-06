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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.lecturestudio.core.model.Interval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicByteArrayInputStreamExtendedTest {

	private DynamicByteArrayInputStream stream;

	/**
	 * Creates a valid WAV file with the specified audio data.
	 * The WAV header is 44 bytes, followed by the audio data.
	 *
	 * @param audioData The audio data to include in the WAV file.
	 * @return A byte array containing a valid WAV file.
	 */
	private byte[] createWavFile(byte[] audioData) {
		int dataSize = audioData.length;
		int chunkSize = 36 + dataSize;
		int subchunk1Size = 16; // PCM format
		short audioFormat = 1; // PCM = 1
		short numChannels = 1; // Mono
		int sampleRate = 44100;
		short bitsPerSample = 16;
		short blockAlign = (short) (numChannels * (bitsPerSample / 8));
		int byteRate = sampleRate * numChannels * (bitsPerSample / 8);
		int subchunk2Size = dataSize;

		byte[] wavFile = new byte[44 + dataSize];
		int offset = 0;

		// RIFF chunk
		System.arraycopy("RIFF".getBytes(), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy(BitConverter.getLittleEndianBytes(chunkSize), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy("WAVE".getBytes(), 0, wavFile, offset, 4);
		offset += 4;

		// fmt chunk
		System.arraycopy("fmt ".getBytes(), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy(BitConverter.getLittleEndianBytes(subchunk1Size), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy(BitConverter.getLittleEndianBytes(audioFormat), 0, wavFile, offset, 2);
		offset += 2;
		System.arraycopy(BitConverter.getLittleEndianBytes(numChannels), 0, wavFile, offset, 2);
		offset += 2;
		System.arraycopy(BitConverter.getLittleEndianBytes(sampleRate), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy(BitConverter.getLittleEndianBytes(byteRate), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy(BitConverter.getLittleEndianBytes(blockAlign), 0, wavFile, offset, 2);
		offset += 2;
		System.arraycopy(BitConverter.getLittleEndianBytes(bitsPerSample), 0, wavFile, offset, 2);
		offset += 2;

		// data chunk
		System.arraycopy("data".getBytes(), 0, wavFile, offset, 4);
		offset += 4;
		System.arraycopy(BitConverter.getLittleEndianBytes(subchunk2Size), 0, wavFile, offset, 4);
		offset += 4;

		// Audio data
		System.arraycopy(audioData, 0, wavFile, offset, dataSize);

		return wavFile;
	}

	@BeforeEach
	void setUp() throws Exception {
		// Create test data: 0, 1, 2, ..., 99
		byte[] testData = new byte[100];
		for (int i = 0; i < testData.length; i++) {
			testData[i] = (byte) i;
		}
		// Wrap test data in a valid WAV file format
		byte[] wavData = createWavFile(testData);
		stream = new DynamicByteArrayInputStream(wavData);
	}

	@Test
	void testConstructorWithNullData() {
		assertThrows(NullPointerException.class, () -> new DynamicByteArrayInputStream(null));
	}

	@Test
	void testConstructorWithEmptyData() throws Exception {
		byte[] emptyData = new byte[0];
		// Wrap empty data in a valid WAV file format
		byte[] wavData = createWavFile(emptyData);
		DynamicByteArrayInputStream emptyStream = new DynamicByteArrayInputStream(wavData);
		assertNotNull(emptyStream);
		assertEquals(0, emptyStream.available());
	}

	@Test
	void testCloneWithExclusions() throws Exception {
		// Add exclusions to the original stream
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		assertNotSame(stream, clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithNoExclusions() throws Exception {
		// No exclusions
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		assertNotSame(stream, clone);
		
		// Clone should have no exclusions
		assertEquals(0, clone.getExclusions().size());
		assertEquals(0L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithManyExclusions() throws Exception {
		// Add many exclusions
		for (int i = 0; i < 10; i++) {
			stream.addExclusion(new Interval<>((long) (i * 5), (long) (i * 5 + 2)));
		}
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(10, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithOverlappingExclusions() throws Exception {
		// Add overlapping exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(15L, 25L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(3, clone.getExclusions().size());
		// Excluded length should be calculated correctly (10-25 merged + 30-40 = 15 + 10 = 25)
		assertEquals(25L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithAdjacentExclusions() throws Exception {
		// Add adjacent exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(20L, 30L));
		stream.addExclusion(new Interval<>(40L, 50L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(3, clone.getExclusions().size());
		// Excluded length should be calculated correctly (10-30 merged + 40-50 = 20 + 10 = 30)
		assertEquals(30L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithNestedExclusions() throws Exception {
		// Add nested exclusions
		stream.addExclusion(new Interval<>(10L, 30L));
		stream.addExclusion(new Interval<>(15L, 25L));
		stream.addExclusion(new Interval<>(40L, 50L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(3, clone.getExclusions().size());
		// Excluded length should be calculated correctly (10-30 + 40-50 = 20 + 10 = 30)
		assertEquals(30L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithZeroLengthExclusions() throws Exception {
		// Add zero-length exclusions
		stream.addExclusion(new Interval<>(10L, 10L));
		stream.addExclusion(new Interval<>(20L, 20L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		// Zero-length exclusions should not contribute to excluded length
		assertEquals(0L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithNegativeLengthExclusions() throws Exception {
		// Invalid intervals (end < start) should be rejected at the point of addition
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(20L, 10L)));
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(30L, 15L)));
		
		// No exclusions should have been added
		assertEquals(0, stream.getExclusions().size());
		assertEquals(0L, stream.getExcludedLength());
	}

	@Test
	void testAddExclusionWithNegativeValues() throws Exception {
		// Intervals with negative start values should be rejected
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(-10L, 5L)));
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(-5L, -1L)));
		
		// Intervals with negative end values should be rejected
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(10L, -5L)));
		
		// Valid intervals with non-negative values should be accepted
		stream.addExclusion(new Interval<>(0L, 10L));
		stream.addExclusion(new Interval<>(20L, 30L));
		
		// Only valid exclusions should have been added
		assertEquals(2, stream.getExclusions().size());
		assertEquals(20L, stream.getExcludedLength());
	}

	@Test
	void testCloneWithLargeExclusions() throws Exception {
		// Add exclusions that cover most of the data
		stream.addExclusion(new Interval<>(0L, 50L));
		stream.addExclusion(new Interval<>(50L, 100L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		// Excluded length should be 100 (0-100 merged)
		assertEquals(100L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithSingleByteExclusions() throws Exception {
		// Add single-byte exclusions
		for (int i = 0; i < 10; i++) {
			stream.addExclusion(new Interval<>((long) i, (long) (i + 1)));
		}
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(10, clone.getExclusions().size());
		// Excluded length should be 10
		assertEquals(10L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithExclusionsAtBoundaries() throws Exception {
		// Add exclusions at the beginning and end
		stream.addExclusion(new Interval<>(0L, 10L));
		stream.addExclusion(new Interval<>(90L, 100L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		// Excluded length should be 20
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithExclusionsInMiddle() throws Exception {
		// Add exclusions in the middle
		stream.addExclusion(new Interval<>(25L, 35L));
		stream.addExclusion(new Interval<>(45L, 55L));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		// Excluded length should be 20
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithMixedExclusions() throws Exception {
		// Add mixed exclusions (overlapping, adjacent, nested, zero-length)
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(15L, 25L)); // Overlapping
		stream.addExclusion(new Interval<>(30L, 30L)); // Zero-length
		stream.addExclusion(new Interval<>(40L, 50L));
		stream.addExclusion(new Interval<>(50L, 60L)); // Adjacent
		stream.addExclusion(new Interval<>(70L, 80L));
		stream.addExclusion(new Interval<>(75L, 85L)); // Overlapping
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(7, clone.getExclusions().size());
		// Excluded length should be calculated correctly
		// 10-25 merged + 30-30 (zero) + 40-60 merged + 70-85 merged = 15 + 0 + 20 + 15 = 50
		assertEquals(50L, clone.getExcludedLength());
	}

	@Test
	void testConcurrentClone() throws InterruptedException {
		// Add some exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		CountDownLatch latch = new CountDownLatch(5);
		AtomicInteger successCount = new AtomicInteger(0);
		ExecutorService executor = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 5; i++) {
			executor.submit(() -> {
				try {
					for (int j = 0; j < 10; j++) {
						DynamicByteArrayInputStream clone = stream.clone();
						assertNotNull(clone);
						assertEquals(2, clone.getExclusions().size());
						assertEquals(20L, clone.getExcludedLength());
						successCount.incrementAndGet();
					}
				} catch (Exception e) {
					fail("Concurrent clone test failed: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();
		
		// All operations should have succeeded
		assertEquals(50, successCount.get());
	}

	@Test
	void testCloneWithReadOperations() throws Exception {
		// Add exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		// Read some data from the original
		byte[] buffer = new byte[25];
		stream.read(buffer);
		
		// Clone should be independent
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions but independent position
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
		
		// Clone should start from the beginning
		assertEquals(0L, clone.getPosition());
	}

	@Test
	void testCloneWithSkipOperations() throws Exception {
		// Add exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		// Skip some data from the original
		stream.skip(25);
		
		// Clone should be independent
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions but independent position
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
		
		// Clone should start from the beginning
		assertEquals(0L, clone.getPosition());
	}

	@Test
	void testCloneWithResetOperations() throws Exception {
		// Add exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		// Read and reset original
		stream.read(new byte[25]);
		stream.reset();
		
		// Clone should be independent
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions but independent position
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
		
		// Clone should start from the beginning
		assertEquals(0L, clone.getPosition());
	}

	@Test
	void testCloneWithMultipleExclusions() throws Exception {
		// Add many exclusions
		for (int i = 0; i < 20; i++) {
			stream.addExclusion(new Interval<>((long) (i * 2), (long) (i * 2 + 1)));
		}
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(20, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithExclusionsAtEveryPosition() throws Exception {
		// Add exclusions at every position (should exclude everything)
		for (int i = 0; i < 100; i++) {
			stream.addExclusion(new Interval<>((long) i, (long) (i + 1)));
		}
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(100, clone.getExclusions().size());
		assertEquals(100L, clone.getExcludedLength());
	}

	@Test
	void testCloneWithExclusionsOutsideRange() throws Exception {
		// Add exclusions outside the data range (but with non-negative values)
		stream.addExclusion(new Interval<>(100L, 200L));
		
		// Negative intervals should be rejected
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusion(new Interval<>(-10L, -5L)));
		
		DynamicByteArrayInputStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions (only the valid one)
		assertEquals(1, clone.getExclusions().size());
		// Excluded length should be 100 (100-200 range)
		assertEquals(100L, clone.getExcludedLength());
		// Clone should start from the beginning
		assertEquals(0L, clone.getPosition());
	}
}
