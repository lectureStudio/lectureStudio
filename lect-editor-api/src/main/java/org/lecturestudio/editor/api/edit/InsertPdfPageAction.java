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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.recording.*;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * Inserts a new PDF page in the recording at the selected time position. This action
 * affects the recorded pages and events. Audio and the duration of the recording
 * remain unchanged.
 *
 * @author Alex Andres
 */
public class InsertPdfPageAction implements EditAction {

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
	 * The time position at which to insert the new page.
	 */
	private final double timePos;

	/**
	 * The previous recorded page state.
	 */
	private final List<RecordedPage> backupPages = new ArrayList<>();

	/**
	 * The previous document state.
	 */
	private byte[] docStream;


	/**
	 * Creates a new {@code ReplaceAllPagesAction} with the provided parameters.
	 *
	 * @param recording The recording on which to operate.
	 * @param newDoc    The document containing the new page.
	 * @param timePos   The time position at which to insert the new page.
	 */
	public InsertPdfPageAction(Recording recording, Document newDoc, double timePos) {
		this.recording = recording;
		this.recordedDocument = recording.getRecordedDocument();
		this.newDoc = newDoc;
		this.timePos = timePos;
	}

	@Override
	public void undo() throws RecordingEditException {
		restoreDocument();
		restorePages();

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		Document document = recordedDocument.getDocument();

		// Create backup for recorded pages and the document.
		backupPages();
		backupDocument();

		// Insert page into the document.
		int insertIndex = document.getCurrentPageNumber() + 1;

		updateDocument(insertIndex);
		updatePages(insertIndex);

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	private void updateDocument(int insertIndex) throws RecordingEditException {
		Document document = recordedDocument.getDocument();

		try {
			document.createPage(newDoc.getCurrentPage(), insertIndex);

			recordedDocument.parseFrom(recordedDocument.toByteArray());
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	private void backupDocument() throws RecordingEditException {
		try {
			docStream = recordedDocument.toByteArray();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	private void restoreDocument() throws RecordingEditException {
		try {
			recordedDocument.parseFrom(docStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	private void updatePages(int insertIndex) throws RecordingEditException {
		RecordedAudio audio = recording.getRecordedAudio();
		List<RecordedPage> recPages = recording.getRecordedEvents().getRecordedPages();

		// Register the new page.
		int timeMs = (int) (timePos * audio.getAudioStream().getLengthInMillis());
		int startIndex = recording.getPageIndex(timeMs, 0);

		RecordedPage startPage = recPages.get(startIndex);

		// Create a new page with events occurred after the insertion point.
		RecordedPage newRecPage = new RecordedPage();
		newRecPage.setNumber(insertIndex);
		newRecPage.setTimestamp(timeMs);

		Iterator<PlaybackAction> iter = startPage.getPlaybackActions().iterator();
		while (iter.hasNext()) {
			PlaybackAction action = iter.next();

			if (action.getTimestamp() > timeMs) {
				newRecPage.addPlaybackAction(action.clone());
				// Remove action from the starting page.
				iter.remove();
			}
		}

		// Shift page numbers after the insertion point to the right.
		for (int i = insertIndex; i < recPages.size(); i++) {
			RecordedPage page = recPages.get(i);
			page.setNumber(page.getNumber() + 1);
		}

		recPages.add(insertIndex, newRecPage);
	}

	private void backupPages() {
		List<RecordedPage> recPages = recording.getRecordedEvents().getRecordedPages();

		backupPages.clear();
		for (RecordedPage page : recPages) {
			backupPages.add(page.clone());
		}
	}

	private void restorePages() {
		List<RecordedPage> recPages = recording.getRecordedEvents().getRecordedPages();
		recPages.clear();
		recPages.addAll(backupPages);
	}
}
