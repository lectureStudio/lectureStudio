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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedDocument;

public class InsertDocumentAction extends RecordingInsertAction<RecordedDocument> {

	private final boolean split;

	private final int startIndex;

	private byte[] docStream;


	public InsertDocumentAction(RecordedDocument recordedObject,
			RecordedDocument document,
			boolean split,
			int startTime,
			int startIndex) {
		super(recordedObject, document, startTime);

		this.split = split;
		this.startIndex = startIndex;
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
		if (startIndex < 0) {
			throw new RecordingEditException("Invalid insert position");
		}

		Document doc = getRecordedObject().getDocument();
		Document newDoc;

		try {
			docStream = getRecordedObject().toByteArray();

			newDoc = new Document();
			newDoc.setTitle(doc.getTitle());

			// Copy pages from the current document.
			for (int i = 0; i < startIndex; i++) {
				newDoc.createPage(doc.getPage(i));
			}

			if (split) {
				newDoc.createPage(doc.getPage(startIndex));
			}

			// Copy pages from the document to insert.
			for (Page page : objectToInsert.getDocument().getPages()) {
				newDoc.createPage(page);
			}

			// Copy pages from the current document.
			for (int i = startIndex; i < doc.getPageCount(); i++) {
				newDoc.createPage(doc.getPage(i));
			}

			// Serialize and load new document.
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			newDoc.toOutputStream(stream);
			stream.close();

			getRecordedObject().parseFrom(stream.toByteArray());

			newDoc.close();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}
}
