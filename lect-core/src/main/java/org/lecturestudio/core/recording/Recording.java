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

package org.lecturestudio.core.recording;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.edit.DeleteAudioAction;
import org.lecturestudio.core.recording.edit.DeleteDocumentAction;
import org.lecturestudio.core.recording.edit.DeletePageAction;
import org.lecturestudio.core.recording.edit.DeleteEventsAction;
import org.lecturestudio.core.recording.edit.ShiftEventsAction;
import org.lecturestudio.core.recording.edit.EditHeaderAction;
import org.lecturestudio.core.recording.edit.InsertAudioAction;
import org.lecturestudio.core.recording.edit.InsertDocumentAction;
import org.lecturestudio.core.recording.edit.InsertEventsAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Recording {

	private static final Logger LOG = LogManager.getLogger(Recording.class);

	public static final int FORMAT_VERSION = 3;

	public enum Content {
		ALL, HEADER, AUDIO, DOCUMENT, EVENTS
	}

	private final List<RecordingChangeListener> listeners = new ArrayList<>();

	private RecordingHeader header;

	private RecordedAudio audio;

	private RecordedDocument document;

	private RecordedEvents events;


	public Recording() {
		header = new RecordingHeader();
	}

	public RecordingHeader getRecordingHeader() {
		return header;
	}

	public void setRecordingHeader(RecordingHeader header) {
		this.header = header;

		fireChangeEvent(Content.HEADER);
	}

	public RecordedAudio getRecordedAudio() {
		return audio;
	}

	public void setRecordedAudio(RecordedAudio audio) {
		this.audio = audio;

		fireChangeEvent(Content.AUDIO);
	}

	public RecordedEvents getRecordedEvents() {
		return events;
	}

	public void setRecordedEvents(RecordedEvents actions) {
		this.events = actions;

		fireChangeEvent(Content.EVENTS);
	}

	public RecordedDocument getRecordedDocument() {
		return document;
	}

	public void setRecordedDocument(RecordedDocument document) {
		this.document = document;

		fireChangeEvent(Content.DOCUMENT);
	}

	public Map<Integer, Integer> getPageChangeEvents() {
		List<RecordedPage> pages = getRecordedEvents().getRecordedPages();
		Iterator<RecordedPage> pageIterator = pages.iterator();

		Map<Integer, Integer> timetable = new LinkedHashMap<>();

		while (pageIterator.hasNext()) {
			RecordedPage recPage = pageIterator.next();
			timetable.put(recPage.getNumber(), recPage.getTimestamp());
		}

		return timetable;
	}

	public void shiftEvents(int millis) {
		ShiftEventsAction shiftAction = new ShiftEventsAction(getRecordedEvents(), millis);
		shiftAction.execute();

		getRecordedEvents().addEditAction(shiftAction);

		fireChangeEvent(Content.EVENTS);
	}

	public void close() {
		if (nonNull(getRecordedDocument().getDocument())) {
			getRecordedDocument().getDocument().close();
		}
	}

	public void cut(int startTime, int endTime) throws RecordingEditException {
		LOG.debug("Cut recording at: {}-{} ms", startTime, endTime);

		final long duration = getRecordedAudio().getAudioStream().getLengthInMillis();
		final List<RecordedPage> recPages = getRecordedEvents().getRecordedPages();
		final Interval<Integer> editInterval = new Interval<>(startTime, endTime);
		final EditHeaderAction editHeader = new EditHeaderAction(getRecordingHeader(), -editInterval.lengthLong());
		final DeleteEventsAction editActions = new DeleteEventsAction(getRecordedEvents(), editInterval);
		final DeleteDocumentAction editDocument = new DeleteDocumentAction(getRecordedDocument());
		final DeleteAudioAction editAudio = new DeleteAudioAction(getRecordedAudio(), editInterval);

		final Map<Integer, Integer> timetable = getPageChangeEvents();

		for (Integer number : timetable.keySet()) {
			Integer timestamp = timetable.get(number);
			Integer nextTimestamp = timetable.get(number + 1);
			boolean containsNext = nonNull(nextTimestamp) && editInterval.contains(nextTimestamp);
			boolean isLastPage = number == (recPages.size() - 1);

			// Interval contains page select event
			if (editInterval.contains(timestamp)) {
				boolean multiplePages = recPages.size() > 1;
				boolean reachedEnd = endTime == duration;

				if (containsNext || (isLastPage && multiplePages && reachedEnd)) {
					LOG.debug("Remove page: {}", number);

					/*
					 * Interval encloses the whole page content.
					 * Remove complete page.
					 */
					editActions.removeRecordedPage(number);

					// Remove document page.
					editDocument.removePage(number);
				}
				else {
					LOG.debug("Shift page: {}", number);

					/*
					 * Reached last page in the interval.
					 * Remove actions on the left side and move page to
					 * interval-start.
					 */
					Interval<Integer> interval = new Interval<>(timestamp, editInterval.getEnd());
					editActions.changeRecordedPage(number, interval);
					editActions.setShiftPage(number);

					// Done.
					break;
				}
			}
			else if (containsNext) {
				LOG.debug("Remove actions on the right side of page: {}", number);

				/*
				 * Remove actions on the right side of the page.
				 */
				Interval<Integer> interval = new Interval<>(editInterval.getStart(), nextTimestamp);
				editActions.changeRecordedPage(number, interval);
			}
			else if (timestamp < editInterval.getStart() && (isLastPage || editInterval.getEnd() < nextTimestamp)) {
				LOG.debug("Remove actions from the middle of page: {}", number);

				/*
				 * Remove actions from the middle of the page.
				 */
				editActions.changeRecordedPage(number, editInterval);

				// Done.
				break;
			}
		}

		// Perform changes.
		editHeader.execute();
		editActions.execute();
		editDocument.execute();
		editAudio.execute();

		getRecordingHeader().addEditAction(editHeader);
		getRecordedEvents().addEditAction(editActions);
		getRecordedDocument().addEditAction(editDocument);
		getRecordedAudio().addEditAction(editAudio);

		fireChangeEvent(Content.ALL);
	}

	public void deletePage(int pageNumber) throws RecordingEditException {
		Interval<Integer> pageDuration = getPageDuration(pageNumber);

		EditHeaderAction editHeader = new EditHeaderAction(getRecordingHeader(), -pageDuration.lengthLong());
		DeletePageAction deletePageAction = new DeletePageAction(getRecordedDocument(), pageNumber);
		DeleteEventsAction deleteActions = new DeleteEventsAction(getRecordedEvents(), pageDuration);
		DeleteAudioAction deleteAudio = new DeleteAudioAction(getRecordedAudio(), pageDuration);

		deleteActions.removeRecordedPage(pageNumber);

		editHeader.execute();
		deletePageAction.execute();
		deleteActions.execute();
		deleteAudio.execute();

		getRecordingHeader().addEditAction(editHeader);
		getRecordedEvents().addEditAction(deleteActions);
		getRecordedDocument().addEditAction(deletePageAction);
		getRecordedAudio().addEditAction(deleteAudio);

		fireChangeEvent(Content.ALL);
	}

	public void insert(Recording recording, double start) throws RecordingEditException {
		// Snap to page margin in milliseconds.
		int snapToPageMargin = 250;

		RecordingHeader header = getRecordingHeader();
		RecordedAudio audio = getRecordedAudio();
		RecordedDocument doc = getRecordedDocument();
		RecordedEvents events = getRecordedEvents();

		RecordedAudio insAudio = recording.getRecordedAudio();
		RecordedDocument insDoc = recording.getRecordedDocument();
		RecordedEvents insEvents = recording.getRecordedEvents();

		long duration = audio.getAudioStream().getLengthInMillis();
		int insertDuration = (int) insAudio.getAudioStream().getLengthInMillis();
		int time = (int) (start * audio.getAudioStream().getLengthInMillis());
		int startIndex = getPageIndex(time, snapToPageMargin);
		time = findInsertTime(startIndex, time, snapToPageMargin);

		RecordedPage startPage = events.getRecordedPages().get(startIndex);
		boolean end = Math.abs(duration - time) < snapToPageMargin;
		boolean split = startPage.getTimestamp() != time && !end;
		startIndex = end ? startIndex + 1 : startIndex;

		EditHeaderAction editHeader = new EditHeaderAction(header, insertDuration);
		InsertEventsAction eventsAction = new InsertEventsAction(events, insEvents, split, time, startIndex, insertDuration);
		InsertDocumentAction documentAction = new InsertDocumentAction(doc, insDoc, split, time, startIndex);
		InsertAudioAction audioAction = new InsertAudioAction(audio, insAudio, time);

		editHeader.execute();
		eventsAction.execute();
		documentAction.execute();
		audioAction.execute();

		header.addEditAction(editHeader);
		events.addEditAction(eventsAction);
		doc.addEditAction(documentAction);
		audio.addEditAction(audioAction);

		fireChangeEvent(Content.ALL);
	}

	public void undo() throws RecordingEditException {
		if (!hasUndoActions()) {
			return;
		}

		if (getRecordedEvents().hasUndoActions()) {
			getRecordedEvents().undo();
		}
		if (getRecordedDocument().hasUndoActions()) {
			getRecordedDocument().undo();
		}
		if (getRecordedAudio().hasUndoActions()) {
			getRecordedAudio().undo();
		}
		if (getRecordingHeader().hasUndoActions()) {
			getRecordingHeader().undo();
		}

		fireChangeEvent(Content.ALL);
	}

	public void redo() throws RecordingEditException {
		if (!hasRedoActions()) {
			return;
		}

		if (getRecordedEvents().hasRedoActions()) {
			getRecordedEvents().redo();
		}
		if (getRecordedDocument().hasRedoActions()) {
			getRecordedDocument().redo();
		}
		if (getRecordedAudio().hasRedoActions()) {
			getRecordedAudio().redo();
		}
		if (getRecordingHeader().hasRedoActions()) {
			getRecordingHeader().redo();
		}

		fireChangeEvent(Content.ALL);
	}

	public boolean hasUndoActions() {
		return getRecordingHeader().hasUndoActions() ||
				getRecordedEvents().hasUndoActions() ||
				getRecordedDocument().hasUndoActions() ||
				getRecordedAudio().hasUndoActions();
	}

	public boolean hasRedoActions() {
		return getRecordingHeader().hasRedoActions() ||
				getRecordedEvents().hasRedoActions() ||
				getRecordedDocument().hasRedoActions() ||
				getRecordedAudio().hasRedoActions();
	}

	public int getStateHash() {
		return Objects.hash(getRecordingHeader().getStateHash(),
				getRecordedEvents().getStateHash(),
				getRecordedDocument().getStateHash(),
				getRecordedAudio().getStateHash());
	}

	public void addRecordingChangeListener(RecordingChangeListener listener) {
		listeners.add(listener);
	}

	public void removeRecordingChangeListener(RecordingChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChangeEvent(Content contentType) {
		RecordingChangeEvent event = new RecordingChangeEvent(this, contentType);

		for (RecordingChangeListener listener : listeners) {
			listener.recordingChanged(event);
		}
	}

	/**
	 * Find the insert point in the event stream.
	 *
	 * @return the recorded page index at which to insert other recorded pages.
	 */
	public int getPageIndex(int startTime, int snapToPageMargin) {
		List<RecordedPage> recPages = getRecordedEvents().getRecordedPages();
		Interval<Integer> pageInterval = new Interval<>();

		RecordedPage lastPage = new RecordedPage();
		lastPage.setTimestamp(Integer.MAX_VALUE);

		for (int i = 0; i < recPages.size(); i++) {
			RecordedPage page = recPages.get(i);
			RecordedPage nextPage = i < recPages.size() - 1 ? recPages.get(i + 1) : lastPage;

			pageInterval.set(page.getTimestamp(), nextPage.getTimestamp());

			if (Math.abs(nextPage.getTimestamp() - startTime) < snapToPageMargin) {
				return i + 1;
			}
			else if (pageInterval.contains(startTime)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Find the insertion time with the possibility to snap to pages.
	 *
	 * @return the insertion time.
	 */
	private int findInsertTime(int startIndex, int startTime, int snapToPageMargin) {
		List<RecordedPage> recPages = getRecordedEvents().getRecordedPages();
		RecordedPage startPage = recPages.get(startIndex);

		if (Math.abs(startPage.getTimestamp() - startTime) < snapToPageMargin) {
			return startPage.getTimestamp();
		}

		return startTime;
	}

	private Interval<Integer> getPageDuration(int pageNumber) {
		List<RecordedPage> recPages = getRecordedEvents().getRecordedPages();
		RecordedPage page = recPages.get(pageNumber);

		RecordedPage lastPage = new RecordedPage();
		lastPage.setTimestamp((int) getRecordedAudio().getAudioStream().getLengthInMillis());

		RecordedPage nextPage = pageNumber < recPages.size() - 1 ? recPages.get(pageNumber + 1) : lastPage;

		Interval<Integer> pageInterval = new Interval<>();
		pageInterval.set(page.getTimestamp(), nextPage.getTimestamp());

		return pageInterval;
	}
}
