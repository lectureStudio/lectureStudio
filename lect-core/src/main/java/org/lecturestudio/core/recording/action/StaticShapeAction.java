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

package org.lecturestudio.core.recording.action;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.recording.RecordedObject;

public class StaticShapeAction implements RecordedObject, Cloneable {

	private PlaybackAction action;


	public StaticShapeAction(PlaybackAction action) {
		this.action = action;
	}

	public StaticShapeAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	public PlaybackAction getAction() {
		return action;
	}

	@Override
	public StaticShapeAction clone() {
		return new StaticShapeAction(action.clone());
	}

	@Override
	public byte[] toByteArray() throws IOException {
		return action.toByteArray();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		int length = buffer.getInt();
		int type = buffer.get();
		int timestamp = buffer.getInt();

		int dataLength = length - 5;

		if (dataLength > 0) {
			byte[] data = new byte[dataLength];
			buffer.get(data);

			action = ActionFactory.createAction(type, timestamp, data);
		}
	}

}
