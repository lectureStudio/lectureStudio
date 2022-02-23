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

import java.io.IOException;
import java.nio.ByteBuffer;

public class StreamRecordStateAction extends StreamStateAction {

	private boolean isRecorded;


	public StreamRecordStateAction(long courseId, boolean recorded) {
		super(courseId);

		this.isRecorded = recorded;
	}

	public StreamRecordStateAction(byte[] input) {
		super(input);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteBuffer buffer = createBuffer(9);
		buffer.putLong(getCourseId());
		buffer.put((byte) (isRecorded ? 1 : 0));

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) {
		ByteBuffer buffer = createBuffer(input);

		courseId = buffer.getLong();
		isRecorded = buffer.get() == 1;
	}

	@Override
	public StreamActionType getType() {
		return StreamActionType.STREAM_RECORDED;
	}
}
