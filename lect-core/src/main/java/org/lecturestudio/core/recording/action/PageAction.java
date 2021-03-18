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

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.DocumentType;

public class PageAction extends PlaybackAction {

	private DocumentType type;

	private int documentId;

	private int pageNumber;


	public PageAction(DocumentType type, int documentId, int pageNumber) {
		this.type = type;
		this.documentId = documentId;
		this.pageNumber = pageNumber;
	}

	public PageAction(byte[] input) throws IOException {
		parseFrom(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		controller.selectPage(pageNumber);
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteBuffer buffer = createBuffer(9);

		buffer.put((byte) type.ordinal());
		buffer.putInt(documentId);
		buffer.putInt(pageNumber);

		return buffer.array();
	}

	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		type = DocumentType.values()[buffer.get()];
		documentId = buffer.getInt();
		pageNumber = buffer.getInt();
	}

	@Override
	public ActionType getType() {
		return ActionType.PAGE;
	}

}
