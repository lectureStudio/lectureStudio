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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.util.AudioUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DynamicInputStreamTest {

	private static byte[] data;


	@BeforeAll
	static void setUp() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);

		for (int i = 0; i < 51; i++) {
			w.write(i);
		}

		w.flush();

		data = baos.toByteArray();
	}

	@Test
	void testEmptyIntervals() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));

		byte[] data = new byte[8];
		int read = stream.read(data);

		assertEquals(8, read);
		assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 }, data);

		stream.close();
	}

	@Test
	void testBeginEndExclusion() throws IOException, UnsupportedAudioFileException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		AudioFormat audioFormat = stream.getAudioFormat();

		long streamLength = stream.getLength();

		// Calculate excluded length in bytes.
		long start = Math.max(AudioUtils.getAudioBytePosition(audioFormat, 0), 0);
		long end = Math.min(AudioUtils.getAudioBytePosition(audioFormat, 1300), streamLength);

		long excludeLength = end - start;

		start = Math.max(AudioUtils.getAudioBytePosition(audioFormat, 20500), 0);
		end = Math.min(AudioUtils.getAudioBytePosition(audioFormat, Long.MAX_VALUE), streamLength);

		excludeLength += (end - start);

		// Exclude parts of the audio stream.
		stream.addExclusionMillis(0, 1300);
		stream.addExclusionMillis(20500, Long.MAX_VALUE);

		assertEquals(excludeLength, stream.getExcludedLength());

		// Write edited audio file.
		File outFile = new File("8khz-mono-edited.wav");
		outFile.deleteOnExit();

		FileOutputStream outStream = new FileOutputStream(outFile);
		stream.write(outStream.getChannel());
		outStream.close();
		stream.close();

		// Get edited audio file length.
		RandomAccessAudioStream copyStream = new RandomAccessAudioStream(outFile);

		assertEquals(streamLength - excludeLength - 1, copyStream.getLength() - 44);

		copyStream.close();

		outFile.delete();
	}

	@Test
	void testBeginMidExclusion() throws IOException, UnsupportedAudioFileException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("countdown.wav"));

		AudioFormat audioFormat = stream.getAudioFormat();

		long streamLength = stream.getLength();

		// Calculate excluded length in bytes.
		long start = Math.max(AudioUtils.getAudioBytePosition(audioFormat, 0), 0);
		long end = Math.min(AudioUtils.getAudioBytePosition(audioFormat, 800), streamLength);

		long excludeLength = end - start;

		start = Math.max(AudioUtils.getAudioBytePosition(audioFormat, 4600), 0);
		end = Math.min(AudioUtils.getAudioBytePosition(audioFormat, 6800), streamLength);

		excludeLength += (end - start);

		// Exclude parts of the audio stream.
		stream.addExclusionMillis(0, 800);
		stream.addExclusionMillis(4600, 6800);

		assertEquals(excludeLength, stream.getExcludedLength());

		// Write edited audio file.
		File outFile = new File("countdown-edited.wav");
		outFile.deleteOnExit();

		FileOutputStream outStream = new FileOutputStream(outFile);
		stream.write(outStream.getChannel());
		outStream.close();
		stream.close();

		// Get edited audio file length.
		RandomAccessAudioStream copyStream = new RandomAccessAudioStream(outFile);

		assertEquals(streamLength - excludeLength, copyStream.getLength() - 44);

		copyStream.close();

		outFile.delete();
	}

	@Test
	void testLength() throws IOException, UnsupportedAudioFileException {
		RandomAccessAudioStream stream = new RandomAccessAudioStream(getFile("8khz-mono.wav"));

		AudioFormat format = stream.getAudioFormat();
		long length = stream.getLength();

		long start = 5000;
		long end = 10000;
		long millis = end - start;

		stream.addExclusionMillis(start, end);

		long startByte = AudioUtils.getAudioBytePosition(format, start);
		long endByte = AudioUtils.getAudioBytePosition(format, end);

		long expected = length - (AudioUtils.getBytesPerSecond(format) * millis / 1000);
		long actual = length - (endByte - startByte);

		assertEquals(expected, actual);
		assertEquals(expected, stream.getLength());

		stream.close();
	}

	@Test
	void testReadValue() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));
		stream.addExclusion(new Interval<>(0L, 8L));
		stream.addExclusion(new Interval<>(15L, 19L));
		stream.addExclusion(new Interval<>(21L, 22L));

		assertEquals(9, stream.read());
		assertEquals(10, stream.read());

		long skipped = stream.skip(5);
		assertEquals(5, skipped);

		assertEquals(23, stream.read());
		assertEquals(24, stream.read());

		stream.reset();

		skipped = stream.skip(12);
		assertEquals(12, skipped);

		assertEquals(28, stream.read());

		stream.close();
	}

	@Test
	void testStreamStartExclusion() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));
		stream.addExclusion(new Interval<>(0L, 8L));

		byte[] data = new byte[8];
		int read = stream.read(data);

		assertEquals(8, read);
		assertArrayEquals(new byte[] { 9, 10, 11, 12, 13, 14, 15, 16 }, data);

		stream.close();
	}

	@Test
	void testStreamMiddleExclusion() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));
		stream.addExclusion(new Interval<>(4L, 5L));

		byte[] data = new byte[8];
		int read = stream.read(data);

		assertEquals(8, read);
		assertArrayEquals(new byte[] { 0, 1, 2, 3, 6, 7, 8, 9 }, data);

		stream.close();
	}

	@Test
	void testStreamEndExclusion() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));
		stream.addExclusion(new Interval<>(40L, 50L));

		long skipped = stream.skip(35);
		assertEquals(35, skipped);

		byte[] data = new byte[8];
		int read = stream.read(data);

		assertEquals(4, read);
		assertArrayEquals(new byte[] { 35, 36, 37, 38, 39, 0, 0, 0 }, data);

		stream.close();
	}

	@Test
	void testAllExclusions() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));
		stream.addExclusion(new Interval<>(0L, 1L));	// case 1
		stream.addExclusion(new Interval<>(4L, 5L));	// case 2
		stream.addExclusion(new Interval<>(8L, 13L));	// case 3

		byte[] data = new byte[8];
		int read = stream.read(data);

		assertEquals(8, read);
		assertArrayEquals(new byte[] { 2, 3, 6, 7, 14, 15, 16, 17 }, data);

		stream.close();
	}

	@Test
	void testReset() throws IOException {
		DynamicInputStream stream = new DynamicInputStream(new ByteArrayInputStream(data));
		stream.addExclusion(new Interval<>(0L, 1L));
		stream.addExclusion(new Interval<>(4L, 5L));
		stream.addExclusion(new Interval<>(8L, 13L));

		byte[] data = new byte[8];
		int read = stream.read(data);

		assertEquals(8, read);

		stream.reset();
		stream.read(data);

		assertArrayEquals(new byte[] { 2, 3, 6, 7, 14, 15, 16, 17 }, data);

		stream.close();
	}

	private File getFile(String file) {
		return new File(getClass().getClassLoader().getResource(file).getFile());
	}
}