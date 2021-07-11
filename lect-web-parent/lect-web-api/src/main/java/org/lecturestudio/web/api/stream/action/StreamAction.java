/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.action;

import java.nio.ByteBuffer;

import org.lecturestudio.core.recording.RecordedObject;

public abstract class StreamAction implements RecordedObject {


	abstract public StreamActionType getType();


	/**
	 * Creates a new {@code ByteBuffer} with the specified length of the payload
	 * and inserts the required action header parameters. The buffer will be of
	 * the size of the specified length + the length of the header.
	 *
	 * @param length The length of the payload of the specific action.
	 *
	 * @return A new {@code ByteBuffer} with pre-filled action header.
	 */
	protected ByteBuffer createBuffer(int length) {
		return createActionBuffer(length);
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified payload to read from
	 * and reads default action fields, if any present.
	 *
	 * @param input The action payload data.
	 *
	 * @return A new {@code ByteBuffer} to read specific action fields.
	 */
	protected ByteBuffer createBuffer(byte[] input) {
		return ByteBuffer.wrap(input);
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified length of the payload
	 * and inserts the required action header parameters. The buffer will be of
	 * the size of the specified length + the length of the header.
	 *
	 * @param length The length of the payload of the specific action.
	 *
	 * @return A new {@code ByteBuffer} with pre-filled action header.
	 */
	private ByteBuffer createActionBuffer(int length) {
		ByteBuffer buffer = ByteBuffer.allocate(length + 5);

		// Write header.
		buffer.putInt(length + 1);
		buffer.put((byte) getType().ordinal());

		return buffer;
	}

}
