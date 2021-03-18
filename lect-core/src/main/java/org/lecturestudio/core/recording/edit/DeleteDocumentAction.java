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
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedDocument;

public class DeleteDocumentAction extends RecordingEditAction<RecordedDocument> {

	private final List<Integer> pages = new ArrayList<>();

	private byte[] docStream;


	public DeleteDocumentAction(RecordedDocument lectureObject) {
		super(lectureObject);
	}

	public void removePage(int pageNumber) {
		pages.add(pageNumber);
	}

	@Override
	public void undo() throws RecordingEditException {
		if (pages.isEmpty()) {
			return;
		}

		try {
			getRecordedObject().parseFrom(docStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	@Override
	public void redo() throws RecordingEditException {
		if (pages.isEmpty()) {
			return;
		}

		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		Document document = getDocument();
		List<Page> removeList = new ArrayList<>();

		try {
			docStream = getRecordedObject().toByteArray();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}

		for (int pageNumber : pages) {
			removeList.add(document.getPage(pageNumber));
		}
		for (Page page : removeList) {
			document.removePage(page);
		}

		// Serialize and load changed document.
		try {
			getRecordedObject().parseFrom(getRecordedObject().toByteArray());
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	private Document getDocument() {
		return getRecordedObject().getDocument();
	}

}
