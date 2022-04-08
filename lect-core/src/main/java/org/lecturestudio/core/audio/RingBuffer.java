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

package org.lecturestudio.core.audio;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.source.AudioSource;

/**
 * Audio ring-buffer implementation that acts both as an audio sink and as an
 * audio source. One can write and read the buffer at the same time.
 *
 * @author Alex Andres
 */
public class RingBuffer implements AudioSink, AudioSource {

	/** Internal data storage. **/
	private final ByteBuffer buffer;

	/** The audio format of audio samples in this buffer. **/
	private AudioFormat audioFormat;

	/** Position from where to start reading from the buffer. **/
	private int readPointer;

	/** Position from where to start writing into the buffer. **/
	private int writePointer;

	/** The number of bytes written to the buffer. */
	private int bytesToWrite;

	/** The number of bytes read from the buffer. */
	private int bytesToRead;


	/**
	 * Creates a {@link RingBuffer} with a default buffer size of 1024 bytes.
	 */
	public RingBuffer() {
		this(1024);
	}

	/**
	 * Creates a {@link RingBuffer} with the specified buffer size.
	 *
	 * @param capacity buffer size in bytes.
	 */
	public RingBuffer(int capacity) {
		if (capacity < 2) {
			throw new IllegalArgumentException("Buffer should have at least the capacity of 2 bytes.");
		}

		this.buffer = ByteBuffer.allocateDirect(capacity);

		clear();
	}

	/**
	 * Resets the read and write pointers. The internal buffer remains unaffected.
	 */
	public synchronized void clear() {
		readPointer = writePointer = 0;
		bytesToRead = 0;
		bytesToWrite = buffer.capacity();
	}

	/**
	 * Return the readable amount of bytes in buffer. Note: It is not
	 * necessarily valid when data is written to the buffer or read from the
	 * buffer. Another thread might have filled the buffer or emptied it in the
	 * meantime.
	 *
	 * @return currently available bytes to read.
	 */
	public int available() {
		return bytesToRead;
	}

	/**
	 * Write as much data as possible to the buffer.
	 *
	 * @param data The data to be written.
	 *
	 * @return the number of bytes actually written.
	 */
	public synchronized int write(byte[] data) {
		return write(data, 0, data.length);
	}

	/**
	 * Write as much data as possible to the buffer.
	 *
	 * @param data   The array holding data to be written.
	 * @param offset The offset where to start in the array.
	 * @param length The number of bytes to write, starting from the offset.
	 *
	 * @return the number of bytes actually written.
	 */
	public synchronized int write(byte[] data, int offset, int length) {
		if (offset < 0 || length < 0 || length > data.length - offset) {
			throw new IndexOutOfBoundsException();
		}

		if (bytesToWrite == 0) {
			return 0;
		}

		if (bytesToWrite < length) {
			length = bytesToWrite;
		}

		buffer.position(writePointer);
		int partLength = buffer.capacity() - writePointer;

		if (partLength > length) {
			buffer.put(data, offset, length);
			writePointer += length;
		}
		else {
			buffer.put(data, offset, partLength);
			buffer.position(0);
			buffer.put(data, offset + partLength, length - partLength);
			writePointer = length - partLength;
		}

		bytesToRead += length;
		bytesToWrite -= length;

		return length;
	}

	/**
	 * Read as much data as possible from the buffer.
	 *
	 * @param data Where to store the data.
	 *
	 * @return the number of bytes read.
	 */
	public synchronized int read(byte[] data) {
		return read(data, 0, data.length);
	}

	/**
	 * Read as much data as possible from the buffer.
	 *
	 * @param data   Where to store the read data.
	 * @param offset The offset where to start in the array.
	 * @param length The number of bytes to read.
	 *
	 * @return the number of bytes read.
	 */
	public synchronized int read(byte[] data, int offset, int length) {
		if (offset < 0 || length < 0 || length > data.length - offset) {
			throw new IndexOutOfBoundsException();
		}

		if (bytesToRead == 0) {
			return 0;
		}

		if (bytesToRead < length) {
			length = bytesToRead;
		}

		buffer.position(readPointer);
		int partLength = buffer.capacity() - readPointer;

		if (partLength > length) {
			buffer.get(data, offset, length);
			readPointer += length;
		}
		else {
			buffer.get(data, offset, partLength);
			buffer.position(0);
			buffer.get(data, partLength, length - partLength);
			readPointer = length - partLength;
		}

		bytesToRead -= length;
		bytesToWrite += length;

		return length;
	}

	@Override
	public long skip(long n) {
		return 0;
	}

	@Override
	public int seekMs(int timeMs) {
		return 0;
	}

	@Override
	public long getInputSize() {
		return 0;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	@Override
	public void setAudioFormat(AudioFormat format) {
		this.audioFormat = format;
	}

	@Override
	public void open() {

	}

	@Override
	public void close() throws IOException {
		clear();
	}

	@Override
	public void reset() {
		clear();
	}

	@Override
	public synchronized String toString() {
		StringBuilder str = new StringBuilder();
		String tail;

		buffer.position(0);

		for (int i = 0; i < buffer.capacity(); i++) {
			tail = ((i % 79) == 0) && i != 0 ? "\n" : "";
			str.append(buffer.get()).append(" ").append(tail);
		}

		return str.toString();
	}

}
