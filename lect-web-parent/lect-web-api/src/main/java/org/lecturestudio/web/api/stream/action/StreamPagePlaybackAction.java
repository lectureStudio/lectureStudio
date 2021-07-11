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

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.ActionFactory;
import org.lecturestudio.core.recording.action.PlaybackAction;

public class StreamPagePlaybackAction extends StreamPageAction {

	private PlaybackAction action;


	public StreamPagePlaybackAction(Page page, PlaybackAction action) {
		super(page);

		this.action = action;
	}

	public StreamPagePlaybackAction(byte[] input) throws IOException {
		super(input);
	}

	public PlaybackAction getAction() {
		return action;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] actionData = action.toByteArray();

		ByteBuffer buffer = createBuffer(actionData.length + 12);
		buffer.putLong(getDocumentId());
		buffer.putInt(getPageNumber());
		buffer.put(actionData);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		setDocumentId(buffer.getLong());
		setPageNumber(buffer.getInt());

		int length = buffer.getInt();
		int type = buffer.get();
		int timestamp = buffer.getInt();

		byte[] actionData = null;
		int dataLength = length - 5;
		if (dataLength > 0) {
			actionData = new byte[dataLength];
			buffer.get(actionData);
		}

		action = ActionFactory.createAction(type, timestamp, actionData);
	}

	@Override
	public StreamActionType getType() {
		return StreamActionType.STREAM_PAGE_ACTION;
	}
}
