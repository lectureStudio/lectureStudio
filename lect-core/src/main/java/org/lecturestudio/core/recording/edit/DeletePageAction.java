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

package org.lecturestudio.core.recording.edit;

import java.io.IOException;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.recording.RecordingEditException;

public class DeletePageAction extends RecordingEditAction<RecordedDocument> {

	private final int pageNumber;

	private byte[] docStream;


	public DeletePageAction(RecordedDocument lectureObject, int pageNumber) {
		super(lectureObject);

		this.pageNumber = pageNumber;
	}

	@Override
	public void undo() throws RecordingEditException {
		try {
			getRecordedObject().parseFrom(docStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		Document document = getRecordedObject().getDocument();

		try {
			docStream = getRecordedObject().toByteArray();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}

		document.removePage(document.getPage(pageNumber));

		// Serialize and load changed document.
		try {
			getRecordedObject().parseFrom(getRecordedObject().toByteArray());
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}
}
