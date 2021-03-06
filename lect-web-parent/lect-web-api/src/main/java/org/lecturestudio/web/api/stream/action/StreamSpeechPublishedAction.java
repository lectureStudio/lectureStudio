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
import java.util.Arrays;

public class StreamSpeechPublishedAction extends StreamAction {

	private BigInteger publisherId;


	public StreamSpeechPublishedAction(BigInteger publisherId) {
		this.publisherId = publisherId;
	}

	public StreamSpeechPublishedAction(byte[] input) {
		parseFrom(input);
	}

	public BigInteger getPublisherId() {
		return publisherId;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] payload = publisherId.toByteArray();

		ByteBuffer buffer = createBuffer(payload.length);
		buffer.put(payload);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) {
		ByteBuffer buffer = createBuffer(input);
		byte[] val = Arrays.copyOf(buffer.array(), buffer.limit());

		publisherId = new BigInteger(val);
	}

	@Override
	public StreamActionType getType() {
		return StreamActionType.STREAM_SPEECH_PUBLISHED;
	}
}
