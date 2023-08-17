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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StreamSpeechPublishedAction extends StreamAction {

	private BigInteger publisherId;

	private String displayName;


	public StreamSpeechPublishedAction(BigInteger publisherId, String displayName) {
		this.publisherId = publisherId;
		this.displayName = displayName;
	}

	public StreamSpeechPublishedAction(byte[] input) {
		parseFrom(input);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		String idStr = publisherId.toString();
		byte[] idBytes = idStr.getBytes(StandardCharsets.UTF_8);
		byte[] nameBytes = displayName.getBytes(StandardCharsets.UTF_8);

		ByteBuffer buffer = createBuffer(idBytes.length + nameBytes.length + 8);
		buffer.putInt(idBytes.length);
		buffer.put(idBytes);
		buffer.putInt(nameBytes.length);
		buffer.put(nameBytes);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) {
		ByteBuffer buffer = createBuffer(input);

		int length = buffer.getInt();
		byte[] idBytes = new byte[length];
		buffer.get(idBytes);

		length = buffer.getInt();
		byte[] nameBytes = new byte[length];
		buffer.get(nameBytes);

		publisherId = new BigInteger(new String(idBytes, StandardCharsets.UTF_8));
		displayName = new String(idBytes, StandardCharsets.UTF_8);
	}

	@Override
	public StreamActionType getType() {
		return StreamActionType.STREAM_SPEECH_PUBLISHED;
	}
}
