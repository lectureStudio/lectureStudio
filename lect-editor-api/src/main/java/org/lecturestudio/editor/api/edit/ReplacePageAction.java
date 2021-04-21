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

package org.lecturestudio.editor.api.edit;

import java.io.IOException;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * A {@code ReplacePageAction} replaces a page in the recording. This action
 * only affects the recorded pages. Audio, events and the duration of the
 * recording remain unchanged.
 *
 * @author Alex Andres
 */
public class ReplacePageAction implements EditAction {

	/**
	 * The recording on which to operate.
	 */
	private final Recording recording;

	/**
	 * The recorded document on which to operate.
	 */
	private final RecordedDocument recordedDocument;

	/**
	 * The document containing the new page.
	 */
	private final Document newDoc;

	/**
	 * The page number to replace.
	 */
	private final int pageNumber;

	/**
	 * The previous document state stored in a byte stream.
	 */
	private byte[] docStream;


	/**
	 * Creates a new {@code ReplacePageAction} with the provided parameters.
	 *
	 * @param recording The recording on which to operate.
	 * @param newDoc    The document containing the new page.
	 */
	public ReplacePageAction(Recording recording, Document newDoc) {
		this.recording = recording;
		this.recordedDocument = recording.getRecordedDocument();
		this.newDoc = newDoc;
		this.pageNumber = recordedDocument.getDocument().getCurrentPageNumber();
	}

	@Override
	public void undo() throws RecordingEditException {
		try {
			recordedDocument.parseFrom(docStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}

		recording.fireChangeEvent(Content.DOCUMENT);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		Document document = recordedDocument.getDocument();

		try {
			docStream = recordedDocument.toByteArray();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}

		Page page = document.getPage(pageNumber);

		// Serialize and load changed document.
		try {
			document.replacePage(page, newDoc.getCurrentPage());

			recordedDocument.parseFrom(recordedDocument.toByteArray());
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}

		recording.fireChangeEvent(Content.DOCUMENT);
	}
}
