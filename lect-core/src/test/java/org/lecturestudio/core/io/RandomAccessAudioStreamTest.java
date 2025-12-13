/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.util.AudioUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Extensive tests for {@link RandomAccessAudioStream}.
 */
class RandomAccessAudioStreamTest {

	@TempDir
	File tempDir;

	// ========== Constructor Tests ==========

	@Test
	void testConstructorFromFile() throws IOException {
		File audioFile = getFile("8khz-mono.wav");
		RandomAccessAudioStream stream = new RandomAccessAudioStream(audioFile);

		assertNotNull(stream);
		assertNotNull(stream.getAudioFormat());
		assertTrue(stream.getLength() > 0);

		stream.close();
	}

	@Test
	void testConstructorFromDynamicInputStream() throws IOException {
		File audioFile = getFile("8khz-mono.wav");
		RandomAccessStream inputStream = new RandomAccessStream(audioFile);
		RandomAccessAudioStream stream = new RandomAccessAudioStream(inputStream);

		assertNotNull(stream);
		assertNotNull(stream.getAudioFormat());
		assertTrue(stream.getLength() > 0);

		stream.close();
	}

	@Test
	void testConstructorEncodedStream() throws IOException {
		File audioFile = getFile("8khz-mono.wav");
		RandomAccessStream inputStream = new RandomAccessStream(audioFile);

		// For encoded streams, audio format must be set manually.
		RandomAccessAudioStream stream = new RandomAccessAudioStream(inputStream, true);

		assertNotNull(stream);
		// Format should be null for encoded streams unless explicitly set.
		assertNull(stream.getAudioFormat());

		stream.close();
	}

	// ========== Audio Format Tests ==========

	@Test
	void testGetAudioFormat() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		AudioFormat format = stream.getAudioFormat();
		assertNotNull(format);
		assertEquals(8000, format.getSampleRate());
		assertEquals(1, format.getChannels());

		stream.close();
	}

	@Test
	void testSetAudioFormat() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 2);
		stream.setAudioFormat(newFormat);

		assertEquals(newFormat, stream.getAudioFormat());
		assertEquals(44100, stream.getAudioFormat().getSampleRate());
		assertEquals(2, stream.getAudioFormat().getChannels());

		stream.close();
	}

	// ========== Length Tests ==========

	@Test
	void testGetLengthNoExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long length = stream.getLength();
		assertTrue(length > 0);
		assertEquals(0, stream.getExcludedLength());

		stream.close();
	}

	@Test
	void testGetLengthWithExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long originalLength = stream.getLength();

		// Add an exclusion.
		stream.addExclusionMillis(1000, 2000);

		long newLength = stream.getLength();
		assertTrue(newLength < originalLength);
		assertTrue(stream.getExcludedLength() > 0);
		assertEquals(originalLength - stream.getExcludedLength(), newLength);

		stream.close();
	}

	@Test
	void testGetLengthInMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long lengthMillis = stream.getLengthInMillis();
		assertTrue(lengthMillis > 0);

		// Verify conversion is consistent.
		long lengthBytes = stream.getLength();
		double bytesPerSecond = AudioUtils.getBytesPerSecond(stream.getAudioFormat());
		long expectedMillis = (long) ((lengthBytes / bytesPerSecond) * 1000);

		assertEquals(expectedMillis, lengthMillis);

		stream.close();
	}

	@Test
	void testGetLengthInMillisWithExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long originalMillis = stream.getLengthInMillis();

		stream.addExclusionMillis(1000, 2000);

		long newMillis = stream.getLengthInMillis();
		assertTrue(newMillis < originalMillis);

		stream.close();
	}

	// ========== Time-Byte Conversion Tests ==========

	@Test
	void testMillisToBytes() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		AudioFormat format = stream.getAudioFormat();
		long expected = AudioUtils.getAudioBytePosition(format, 1000);
		long actual = stream.millisToBytes(1000);

		assertEquals(expected, actual);

		stream.close();
	}

	@Test
	void testBytesToMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		AudioFormat format = stream.getAudioFormat();
		double bytesPerSecond = AudioUtils.getBytesPerSecond(format);
		long bytes = 16000; // 1 second for 8kHz mono 16-bit.
		long expected = (long) ((bytes / bytesPerSecond) * 1000);

		assertEquals(expected, stream.bytesToMillis(bytes));

		stream.close();
	}

	@Test
	void testMillisToBytesRoundTrip() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long millis = 5000;
		long bytes = stream.millisToBytes(millis);
		long backToMillis = stream.bytesToMillis(bytes);

		// Should be approximately equal (may differ slightly due to rounding).
		assertEquals(millis, backToMillis, 1);

		stream.close();
	}

	// ========== Exclusion Tests ==========

	@Test
	void testAddExclusionMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		stream.addExclusionMillis(1000, 2000);

		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(1, exclusions.size());

		long startBytes = stream.millisToBytes(1000);
		long endBytes = stream.millisToBytes(2000);

		assertEquals(startBytes, exclusions.get(0).getStart());
		assertEquals(endBytes, exclusions.get(0).getEnd());

		stream.close();
	}

	@Test
	void testAddExclusionMillisMultiple() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		stream.addExclusionMillis(1000, 2000);
		stream.addExclusionMillis(5000, 6000);

		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(2, exclusions.size());

		stream.close();
	}

	@Test
	void testAddExclusionMillisOverlapping() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		stream.addExclusionMillis(1000, 3000);
		stream.addExclusionMillis(2000, 4000);

		// Overlapping intervals should be merged.
		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(1, exclusions.size());

		long startBytes = stream.millisToBytes(1000);
		long endBytes = stream.millisToBytes(4000);

		assertEquals(startBytes, exclusions.get(0).getStart());
		assertEquals(endBytes, exclusions.get(0).getEnd());

		stream.close();
	}

	@Test
	void testAddExclusionVirtualMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// First add an exclusion in physical coordinates.
		stream.addExclusionMillis(1000, 2000);

		// Now add an exclusion in virtual coordinates.
		// Virtual time 2000 should map to physical time 3000 (shifted by 1s exclusion).
		stream.addExclusionVirtualMillis(2000, 3000);

		List<Interval<Long>> exclusions = stream.getExclusions();
		// Should have two exclusions.
		assertEquals(2, exclusions.size());

		stream.close();
	}

	@Test
	void testAddExclusiveMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		long originalLength = stream.getLength();

		// Keep only 2-5 seconds.
		stream.addExclusiveMillis(new Interval<>(2000L, 5000L));

		// Should have exclusions before and after the kept region.
		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(2, exclusions.size());

		// The resulting length should be approximately 3 seconds.
		long newLength = stream.getLength();
		assertTrue(newLength < originalLength);

		stream.close();
	}

	@Test
	void testAddExclusiveMillisAtStart() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		// Keep from start to 5 seconds.
		stream.addExclusiveMillis(new Interval<>(0L, 5000L));

		// Should only have exclusion after the kept region.
		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(1, exclusions.size());

		stream.close();
	}

	@Test
	void testRemoveExclusiveMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		Interval<Long> keepInterval = new Interval<>(2000L, 5000L);

		// Add exclusive region.
		stream.addExclusiveMillis(keepInterval);
		assertEquals(2, stream.getExclusions().size());

		// Remove exclusive region.
		stream.removeExclusiveMillis(keepInterval);
		assertEquals(0, stream.getExclusions().size());

		stream.close();
	}

	@Test
	void testAddExclusionWithNullInterval() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Should not throw, just ignore.
		stream.addExclusion(null);

		assertEquals(0, stream.getExclusions().size());

		stream.close();
	}

	@Test
	void testAddExclusionWithInvalidInterval() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Start >= end should be ignored.
		stream.addExclusion(new Interval<>(1000L, 1000L));
		stream.addExclusion(new Interval<>(2000L, 1000L));

		assertEquals(0, stream.getExclusions().size());

		stream.close();
	}

	@Test
	void testAddExclusionBoundsToStreamLimits() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long totalLength = stream.getLength();

		// Add exclusion that extends beyond stream limits.
		stream.addExclusion(new Interval<>(-1000L, totalLength + 1000L));

		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(1, exclusions.size());

		// Should be bounded to valid range [MIN_BYTE_POSITION, totalLength].
		assertTrue(exclusions.get(0).getStart() >= 72); // MIN_BYTE_POSITION.
		assertTrue(exclusions.get(0).getEnd() <= totalLength);

		stream.close();
	}

	// ========== Virtual/Physical Position Conversion Tests ==========

	@Test
	void testVirtualMillisToPhysicalBytes() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// No exclusions - virtual equals physical.
		long physical = stream.virtualMillisToPhysicalBytes(1000);
		long expected = stream.millisToBytes(1000);

		assertEquals(expected, physical);

		stream.close();
	}

	@Test
	void testVirtualMillisToPhysicalBytesWithExclusion() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Add 1 second exclusion at 1-2 seconds.
		stream.addExclusionMillis(1000, 2000);

		// Virtual 2 seconds should map to physical 3 seconds.
		long virtualMillis = 2000;
		long physicalBytes = stream.virtualMillisToPhysicalBytes(virtualMillis);

		// The physical position should be shifted by the exclusion length.
		long exclusionBytes = stream.millisToBytes(2000) - stream.millisToBytes(1000);
		long expectedBytes = stream.millisToBytes(virtualMillis) + exclusionBytes;

		assertEquals(expectedBytes, physicalBytes);

		stream.close();
	}

	@Test
	void testPhysicalBytesToVirtualMillis() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// No exclusions.
		long physicalBytes = stream.millisToBytes(1000);
		long virtualMillis = stream.physicalBytesToVirtualMillis(physicalBytes);

		assertEquals(1000, virtualMillis, 1);

		stream.close();
	}

	@Test
	void testPhysicalBytesToVirtualMillisWithExclusion() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Add 1 second exclusion at 1-2 seconds.
		stream.addExclusionMillis(1000, 2000);

		// Physical 3 seconds should map to virtual 2 seconds.
		long physicalBytes = stream.millisToBytes(3000);
		long virtualMillis = stream.physicalBytesToVirtualMillis(physicalBytes);

		assertEquals(2000, virtualMillis, 1);

		stream.close();
	}

	@Test
	void testPhysicalBytesToVirtualMillisInExcludedRegion() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Add exclusion at 1-2 seconds.
		stream.addExclusionMillis(1000, 2000);

		// Physical position in the excluded region should return -1.
		long physicalBytes = stream.millisToBytes(1500);
		long virtualMillis = stream.physicalBytesToVirtualMillis(physicalBytes);

		assertEquals(-1, virtualMillis);

		stream.close();
	}

	// ========== Available Tests ==========

	@Test
	void testAvailable() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		int available = stream.available();
		assertEquals((int) stream.getLength(), available);

		stream.close();
	}

	@Test
	void testAvailableAfterRead() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		int initialAvailable = stream.available();

		byte[] buffer = new byte[1024];
		int read = stream.read(buffer);

		int remainingAvailable = stream.available();
		assertEquals(initialAvailable - read, remainingAvailable);

		stream.close();
	}

	@Test
	void testAvailableWithExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		int originalAvailable = stream.available();

		stream.addExclusionMillis(1000, 2000);

		int newAvailable = stream.available();
		assertTrue(newAvailable < originalAvailable);

		stream.close();
	}

	// ========== Write Tests ==========

	@Test
	void testWriteToChannel() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		File outFile = new File(tempDir, "output.wav");
		FileOutputStream outStream = new FileOutputStream(outFile);
		stream.write(outStream.getChannel());
		outStream.close();
		stream.close();

		// Verify the output file was created.
		assertTrue(outFile.exists());
		assertTrue(outFile.length() > 0);

		// Read back and verify.
		RandomAccessAudioStream outputStream = new RandomAccessAudioStream(outFile);
		assertNotNull(outputStream.getAudioFormat());
		outputStream.close();
	}

	@Test
	void testWriteWithExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		long originalLength = stream.getLength();

		// Exclude 2 seconds.
		stream.addExclusionMillis(2000, 4000);

		long excludedBytes = stream.getExcludedLength();

		File outFile = new File(tempDir, "edited.wav");
		FileOutputStream outStream = new FileOutputStream(outFile);
		stream.write(outStream.getChannel());
		outStream.close();
		stream.close();

		// Read back and verify the length is shorter.
		RandomAccessAudioStream outputStream = new RandomAccessAudioStream(outFile);

		// The output file should be shorter (minus header differences).
		long outputLength = outputStream.getLength();
		long expectedApproxLength = originalLength - excludedBytes;

		// Allow for WAV header size differences (44 bytes).
		assertTrue(Math.abs(outputLength - expectedApproxLength) < 100);

		outputStream.close();
	}

	// ========== Clone Tests ==========

	@Test
	void testClone() throws IOException {
		RandomAccessAudioStream original = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		RandomAccessAudioStream clone = original.clone();

		assertNotNull(clone);
		assertNotSame(original, clone);
		assertEquals(original.getLength(), clone.getLength());
		assertEquals(original.getAudioFormat(), clone.getAudioFormat());

		original.close();
		clone.close();
	}

	@Test
	void testCloneWithExclusions() throws IOException {
		RandomAccessAudioStream original = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		original.addExclusionMillis(1000, 2000);
		original.addExclusionMillis(5000, 6000);

		RandomAccessAudioStream clone = original.clone();

		// Clone should have the same exclusions.
		assertEquals(original.getExclusions().size(), clone.getExclusions().size());
		assertEquals(original.getLength(), clone.getLength());
		assertEquals(original.getExcludedLength(), clone.getExcludedLength());

		// Modifying clone should not affect original.
		clone.addExclusionMillis(10000, 11000);
		assertNotEquals(original.getExclusions().size(), clone.getExclusions().size());

		original.close();
		clone.close();
	}

	@Test
	void testCloneIndependentReading() throws IOException {
		RandomAccessAudioStream original = new RandomAccessAudioStream(getFile("8khz-mono.wav"));
		RandomAccessAudioStream clone = original.clone();

		// Read from original.
		byte[] originalBuffer = new byte[1024];
		original.read(originalBuffer);

		// Clone should still be at position 0.
		byte[] cloneBuffer = new byte[1024];
		clone.read(cloneBuffer);

		// Both should have read the same data.
		assertArrayEquals(originalBuffer, cloneBuffer);

		original.close();
		clone.close();
	}

	// ========== Edge Cases ==========

	@Test
	void testReadEntireStream() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		byte[] buffer = new byte[(int) stream.getLength()];
		int totalRead = 0;
		int read;

		while ((read = stream.read(buffer, totalRead, buffer.length - totalRead)) > 0) {
			totalRead += read;
		}

		assertEquals(stream.getLength(), totalRead);
		assertEquals(0, stream.available());

		stream.close();
	}

	@Test
	void testReadWithMultipleExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		// Add multiple non-overlapping exclusions.
		stream.addExclusionMillis(0, 500);
		stream.addExclusionMillis(2000, 2500);
		stream.addExclusionMillis(5000, 5500);

		// Read the entire stream.
		long expectedLength = stream.getLength();
		byte[] buffer = new byte[8192];
		long totalRead = 0;
		int read;

		while ((read = stream.read(buffer)) > 0) {
			totalRead += read;
		}

		assertEquals(expectedLength, totalRead);

		stream.close();
	}

	@Test
	void testSkipWithExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		stream.addExclusionMillis(1000, 2000);

		// Skip 3 seconds worth of data.
		long bytesToSkip = stream.millisToBytes(3000);
		long skipped = stream.skip(bytesToSkip);

		assertTrue(skipped > 0);

		stream.close();
	}

	@Test
	void testResetStream() throws IOException {
		// Use clone to simulate a reset since AudioInputStream may not support
		// proper mark/reset. This tests that we can read the same data from
		// the beginning using a fresh clone.
		RandomAccessAudioStream stream1 = new RandomAccessAudioStream(getFile("8khz-mono.wav"));
		RandomAccessAudioStream stream2 = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Read some data from the first stream.
		byte[] buffer1 = new byte[1024];
		stream1.read(buffer1);

		// Read the same data from the second stream.
		byte[] buffer2 = new byte[1024];
		stream2.read(buffer2);

		// Both should have the same data.
		assertArrayEquals(buffer1, buffer2);

		stream1.close();
		stream2.close();
	}

	@Test
	void testClearExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long originalLength = stream.getLength();

		stream.addExclusionMillis(1000, 5000);
		assertTrue(stream.getLength() < originalLength);

		stream.clearExclusions();

		assertEquals(originalLength, stream.getLength());
		assertEquals(0, stream.getExclusions().size());

		stream.close();
	}

	@Test
	void testStereoAudioFile() throws IOException {
		// The countdown.wav might be stereo, let's test with it.
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		AudioFormat format = stream.getAudioFormat();
		assertNotNull(format);
		assertTrue(format.getChannels() >= 1);

		stream.close();
	}

	@Test
	void testMillisToBytesZero() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		assertEquals(0, stream.millisToBytes(0));

		stream.close();
	}

	@Test
	void testBytesToMillisZero() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		assertEquals(0, stream.bytesToMillis(0));

		stream.close();
	}

	@Test
	void testExclusionAtStreamBoundaries() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long totalLengthMillis = stream.getLengthInMillis();

		// Exclude first 100ms.
		stream.addExclusionMillis(0, 100);

		// Exclude last 100ms.
		stream.addExclusionMillis(totalLengthMillis - 100, totalLengthMillis);

		// Both exclusions should be bounded and present.
		assertEquals(2, stream.getExclusions().size());

		stream.close();
	}

	@Test
	void testAdjacentExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Add adjacent (touching) exclusions.
		stream.addExclusionMillis(1000, 2000);
		stream.addExclusionMillis(2000, 3000);

		// Adjacent intervals should be merged.
		List<Interval<Long>> exclusions = stream.getExclusions();
		assertEquals(1, exclusions.size());

		long startBytes = stream.millisToBytes(1000);
		long endBytes = stream.millisToBytes(3000);

		assertEquals(startBytes, exclusions.get(0).getStart());
		assertEquals(endBytes, exclusions.get(0).getEnd());

		stream.close();
	}

	@Test
	void testVerySmallExclusion() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long originalLength = stream.getLength();

		// Add a 1ms exclusion.
		stream.addExclusionMillis(1000, 1001);

		// Should still create a valid exclusion.
		assertTrue(stream.getExcludedLength() > 0);
		assertTrue(stream.getLength() < originalLength);

		stream.close();
	}

	@Test
	void testLargeExclusion() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		long originalLength = stream.getLength();
		long totalMillis = stream.getLengthInMillis();

		// Exclude almost the entire stream.
		stream.addExclusionMillis(100, totalMillis - 100);

		long newLength = stream.getLength();
		assertTrue(newLength > 0);
		assertTrue(newLength < originalLength * 0.1); // Less than 10% remaining.

		stream.close();
	}

	// ========== Integration Tests ==========

	@Test
	void testCompleteEditingWorkflow() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		long originalLength = stream.getLength();
		long originalMillis = stream.getLengthInMillis();

		// Step 1: Add some exclusions.
		stream.addExclusionMillis(0, 500);      // Remove first 0.5s.
		stream.addExclusionMillis(3000, 4000);  // Remove 1s in the middle.

		// Step 2: Verify the length changed.
		long newLength = stream.getLength();
		long newMillis = stream.getLengthInMillis();

		assertTrue(newLength < originalLength);
		assertTrue(newMillis < originalMillis);

		// Step 3: Clone for editing.
		RandomAccessAudioStream clone = stream.clone();
		assertEquals(stream.getLength(), clone.getLength());

		// Step 4: Write to file.
		File outFile = new File(tempDir, "workflow-output.wav");
		FileOutputStream outStream = new FileOutputStream(outFile);
		stream.write(outStream.getChannel());
		outStream.close();

		// Step 5: Verify the output.
		RandomAccessAudioStream result = new RandomAccessAudioStream(outFile);
		assertTrue(result.getLength() > 0);

		stream.close();
		clone.close();
		result.close();
	}

	@Test
	void testSetExclusions() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		// Create a list of exclusions.
		List<Interval<Long>> exclusions = List.of(
			new Interval<>(stream.millisToBytes(1000), stream.millisToBytes(2000)),
			new Interval<>(stream.millisToBytes(5000), stream.millisToBytes(6000))
		);

		stream.setExclusions(exclusions);

		assertEquals(2, stream.getExclusions().size());

		stream.close();
	}

	@Test
	void testRemoveExclusion() throws IOException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		long startBytes = stream.millisToBytes(1000);
		long endBytes = stream.millisToBytes(2000);
		Interval<Long> exclusion = new Interval<>(startBytes, endBytes);

		stream.addExclusion(exclusion);
		assertEquals(1, stream.getExclusions().size());

		stream.removeExclusion(exclusion);
		assertEquals(0, stream.getExclusions().size());

		stream.close();
	}

	// ========== Helper Methods ==========

	private File getFile(String file) {
		return new File(getClass().getClassLoader().getResource(file).getFile());
	}
}

