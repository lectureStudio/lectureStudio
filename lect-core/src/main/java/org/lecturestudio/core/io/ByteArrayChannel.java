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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

public class ByteArrayChannel implements SeekableByteChannel {

	private static final int MAX_CAPACITY = Integer.MAX_VALUE - 8;

	/** The buffer where data is stored. */
	private byte[] buffer;

	/** The number of valid bytes in the buffer. */
	private int capacity;

	/** The current position in the buffer. */
	private int position;


	public ByteArrayChannel() {
		this(256);
	}

	public ByteArrayChannel(int initCapacity) {
		buffer = new byte[initCapacity];
	}

	public byte[] toByteArray() {
		return Arrays.copyOf(buffer, capacity);
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() throws IOException {
		// Nothing to do here.
	}

	@Override
	public synchronized int read(ByteBuffer dst) {
		int length = Math.min(capacity - position, dst.remaining());

		dst.put(buffer, position, length);

		position += length;

		return length;
	}

	@Override
	public synchronized int write(ByteBuffer src) {
		int srcPosition = src.position();
		int limit = src.limit();
		int length = (srcPosition <= limit ? limit - srcPosition : 0);

		byte[] data;

		if (src.hasArray()) {
			data = src.array();
		}
		else {
			data = new byte[length];
			src.get(data);
			srcPosition = 0;
		}

		ensureCapacity(length);

		System.arraycopy(data, srcPosition, buffer, position, length);

		// Update capacity only if the read/write pointer exceeds the current capacity.
		if (capacity - position <= 0) {
			capacity += length;
		}

		position += length;

		capacity = Math.max(position, capacity);

		return length;
	}

	@Override
	public synchronized long position() {
		return position;
	}

	@Override
	public synchronized SeekableByteChannel position(long newPosition) {
		if (newPosition < 0) {
			throw new IllegalArgumentException();
		}

		position = (int) newPosition;

		return this;
	}

	@Override
	public synchronized long size() {
		return capacity;
	}

	@Override
	public synchronized SeekableByteChannel truncate(long size) {
		if (size < 0) {
			throw new IllegalArgumentException();
		}

		if (size < capacity) {
			capacity = (int) size;

			if (position > size) {
				position = (int) size;
			}

			buffer = Arrays.copyOf(buffer, capacity);
		}

		return this;
	}

	private void ensureCapacity(int appendLength) {
		int minCapacity = buffer.length + appendLength;

		if (buffer.length - capacity < appendLength) {
			int oldCapacity = buffer.length;
			int newCapacity = oldCapacity << 1;

			// Under/Over-flow handling.
			if (newCapacity - minCapacity < 0) {
				newCapacity = minCapacity;
			}

			if (newCapacity - MAX_CAPACITY > 0) {
				newCapacity = MAX_CAPACITY;
			}

			buffer = Arrays.copyOf(buffer, newCapacity);
		}
	}

}
