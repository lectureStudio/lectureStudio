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

import org.lecturestudio.core.recording.RecordedPage;

public class StreamPageActionsAction extends StreamAction {

	private long documentId;

	private RecordedPage recordedPage;


	public StreamPageActionsAction(long documentId, RecordedPage recordedPage) {
		this.documentId = documentId;
		this.recordedPage = recordedPage;
	}

	public StreamPageActionsAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	public RecordedPage getRecordedPage() {
		return recordedPage;
	}

	public long getDocumentId() {
		return documentId;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		byte[] pageData = recordedPage.toByteArray();

		ByteBuffer buffer = createBuffer(pageData.length + 8);
		buffer.putLong(documentId);
		buffer.put(pageData);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		documentId = buffer.getLong();

		int pageSize = buffer.getInt();
		byte[] pageData = new byte[pageSize];

		buffer.get(pageData);

		recordedPage = new RecordedPage(pageData);
	}

	@Override
	public StreamActionType getType() {
		return StreamActionType.STREAM_PAGE_ACTIONS;
	}
}
