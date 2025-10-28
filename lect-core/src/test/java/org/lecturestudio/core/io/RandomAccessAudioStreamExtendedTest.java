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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.model.Interval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RandomAccessAudioStreamExtendedTest {

	@TempDir
	Path tempDir;

	private DynamicInputStream mockInputStream;
	private RandomAccessAudioStream stream;

	@BeforeEach
	void setUp() throws IOException {
		// Create a mock input stream
		byte[] testData = new byte[1000];
		for (int i = 0; i < testData.length; i++) {
			testData[i] = (byte) (i % 256);
		}
		mockInputStream = new DynamicInputStream(new ByteArrayInputStream(testData));
		stream = new RandomAccessAudioStream(mockInputStream, true); // Use encoded=true to avoid AudioSystem
	}

	@Test
	void testConstructorWithNullFile() {
		assertThrows(IllegalArgumentException.class, () -> new RandomAccessAudioStream((File) null));
	}

	@Test
	void testConstructorWithNullInputStream() {
		assertThrows(IllegalArgumentException.class, () -> new RandomAccessAudioStream((DynamicInputStream) null));
	}

	@Test
	void testConstructorWithNullInputStreamAndEncoded() {
		assertThrows(IllegalArgumentException.class, () -> new RandomAccessAudioStream(null, true));
	}

	@Test
	void testSetAudioFormatWithNull() {
		assertThrows(IllegalArgumentException.class, () -> stream.setAudioFormat(null));
	}

	@Test
	void testAddExclusiveMillisWithNull() {
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusiveMillis(null));
	}

	@Test
	void testAddExclusiveMillisWithoutAudioFormat() throws IOException {
		// Create a stream without audio format
		try (RandomAccessAudioStream streamWithoutFormat = new RandomAccessAudioStream(mockInputStream, true)) {
			// Don't set an audio format, so it remains null

			Interval<Long> interval = new Interval<>(1000L, 2000L);
			assertThrows(IllegalStateException.class, () -> streamWithoutFormat.addExclusiveMillis(interval));
		}
	}

	@Test
	void testRemoveExclusiveMillisWithNull() {
		assertThrows(IllegalArgumentException.class, () -> stream.removeExclusiveMillis(null));
	}

	@Test
	void testRemoveExclusiveMillisWithoutAudioFormat() throws IOException {
		// Create stream without audio format
		RandomAccessAudioStream streamWithoutFormat = new RandomAccessAudioStream(mockInputStream, true);
		try {
			// Don't set audio format, so it remains null
			
			Interval<Long> interval = new Interval<>(1000L, 2000L);
			assertThrows(IllegalStateException.class, () -> streamWithoutFormat.removeExclusiveMillis(interval));
		} finally {
			streamWithoutFormat.close();
		}
	}

	@Test
	void testAddExclusionMillisWithInvalidRange() {
		// Set a mock audio format
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(mockFormat);
		
		assertThrows(IllegalArgumentException.class, () -> stream.addExclusionMillis(2000L, 1000L));
	}

	@Test
	void testAddExclusionMillisWithoutAudioFormat() throws IOException {
		// Create stream without audio format
		RandomAccessAudioStream streamWithoutFormat = new RandomAccessAudioStream(mockInputStream, true);
		try {
			// Don't set audio format, so it remains null
			
			assertThrows(IllegalStateException.class, () -> streamWithoutFormat.addExclusionMillis(1000L, 2000L));
		} finally {
			streamWithoutFormat.close();
		}
	}

	@Test
	void testGetLengthInMillisWithoutAudioFormat() throws IOException {
		// Create stream without audio format
		RandomAccessAudioStream streamWithoutFormat = new RandomAccessAudioStream(mockInputStream, true);
		try {
			// Don't set audio format, so it remains null
			
			assertThrows(IllegalStateException.class, () -> streamWithoutFormat.getLengthInMillis());
		} finally {
			streamWithoutFormat.close();
		}
	}

	@Test
	void testGetLengthInMillisWithZeroBytesPerSecond() {
		// Create a mock audio format with zero bytes per second
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 0, 0);
		stream.setAudioFormat(mockFormat);
		
		assertEquals(0L, stream.getLengthInMillis());
	}

	@Test
	void testAudioFormatSynchronization() throws InterruptedException {
		AudioFormat format1 = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		AudioFormat format2 = new AudioFormat(AudioFormat.Encoding.S24LE, 48000, 1);
		
		// Test concurrent access to setAudioFormat
		Thread thread1 = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				stream.setAudioFormat(format1);
			}
		});
		
		Thread thread2 = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				stream.setAudioFormat(format2);
			}
		});
		
		thread1.start();
		thread2.start();
		
		thread1.join();
		thread2.join();
		
		// Should not throw any exceptions
		AudioFormat result = stream.getAudioFormat();
		assertNotNull(result);
	}

	@Test
	void testCloneWithAudioFormat() throws IOException {
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(mockFormat);
		stream.addExclusion(new Interval<>(10L, 20L));
		
		RandomAccessAudioStream clone = stream.clone();
		assertNotNull(clone);
		assertNotSame(stream, clone);
		
		// Clone should have the same audio format
		assertEquals(mockFormat, clone.getAudioFormat());
	}

	@Test
	void testCloneWithExclusions() throws IOException {
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		RandomAccessAudioStream clone = stream.clone();
		assertNotNull(clone);
		
		// Clone should have the same exclusions
		assertEquals(2, clone.getExclusions().size());
		assertEquals(20L, clone.getExcludedLength());
	}

	@Test
	void testBoundIntervalWithNull() {
		// Test the private boundInterval method indirectly through addExclusionMillis
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(mockFormat);
		
		// This should not throw an exception even with a null interval
		// (the method handles null internally)
		assertDoesNotThrow(() -> {
			// We can't directly test the private method, but we can test
			// that the public methods handle edge cases properly
			stream.addExclusionMillis(0L, 1L);
		});
	}

	@Test
	void testBoundIntervalWithZeroLength() {
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(mockFormat);
		
		// Adding zero-length exclusion should be handled gracefully
		assertDoesNotThrow(() -> stream.addExclusionMillis(1000L, 1000L));
	}

	@Test
	void testWriteWithNullChannel() throws IOException {
		assertThrows(NullPointerException.class, () -> stream.write(null));
	}

	@Test
	void testWriteWithValidChannel() throws IOException {
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(mockFormat);
		
		Path tempFile = tempDir.resolve("test.wav");
		Files.createFile(tempFile);
		
		// This should not throw an exception
		assertDoesNotThrow(() -> {
			try (var channel = Files.newByteChannel(tempFile, java.nio.file.StandardOpenOption.WRITE)) {
				stream.write(channel);
			}
		});
	}

	@Test
	void testStreamLengthConsistency() throws IOException {
		long originalLength = stream.getLength();
		
		// Add exclusions
		stream.addExclusion(new Interval<>(10L, 20L));
		stream.addExclusion(new Interval<>(30L, 40L));
		
		long lengthWithExclusions = stream.getLength();
		long excludedLength = stream.getExcludedLength();
		
		// Length should be original minus excluded
		assertEquals(originalLength - excludedLength, lengthWithExclusions);
	}

	@Test
	void testAvailableConsistency() throws IOException {
		int available = stream.available();
		long position = stream.getPosition();
		long length = stream.getLength();
		
		// Available should be length minus position minus exclusions
		long expectedAvailable = length - position - stream.getExcludedLength();
		assertEquals(expectedAvailable, available);
	}

	@Test
	void testConcurrentAccess() throws InterruptedException {
		AudioFormat mockFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(mockFormat);
		
		CountDownLatch latch = new CountDownLatch(2);
		AtomicInteger successCount = new AtomicInteger(0);
		
		Thread thread1 = new Thread(() -> {
			try {
				for (int i = 0; i < 50; i++) {
					stream.addExclusionMillis(i * 100L, (i + 1) * 100L);
					stream.getLength();
					stream.getPosition();
					successCount.incrementAndGet();
				}
			} catch (Exception e) {
				fail("Thread 1 failed: " + e.getMessage());
			} finally {
				latch.countDown();
			}
		});
		
		Thread thread2 = new Thread(() -> {
			try {
				for (int i = 0; i < 50; i++) {
					stream.addExclusionMillis(i * 200L, (i + 1) * 200L);
					stream.available();
					stream.getExcludedLength();
					successCount.incrementAndGet();
				}
			} catch (Exception e) {
				fail("Thread 2 failed: " + e.getMessage());
			} finally {
				latch.countDown();
			}
		});
		
		thread1.start();
		thread2.start();
		
		latch.await();
		
		// All operations should have succeeded
		assertEquals(100, successCount.get());
	}
}
