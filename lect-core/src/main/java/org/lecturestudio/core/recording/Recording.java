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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.edit.RecordingEditManager;

/**
 * Represents a lecture recording consisting of audio, document, and event data.
 * This class provides functionality for managing recorded content, including
 * undo/redo operations, state tracking, and event notifications.
 * <p>
 * Recording manages multiple components:
 * - Audio data
 * - Document content
 * - Event timeline
 * - Recording header metadata
 * <p>
 * The class implements observer pattern through {@link RecordingChangeListener}.
 *
 * @author Alex Andres
 */
public class Recording {

	/** The format version of the recording, used for compatibility checks. */
	public static final int FORMAT_VERSION = 3;

	/**
	 * Enumeration of content types that can be modified in a recording.
	 * Used to specify which part of the recording has changed when firing change events.
	 */
	public enum Content {
		ALL, HEADER, AUDIO, DOCUMENT, EVENTS_ADDED, EVENTS_CHANGED, EVENTS_REMOVED
	}

	/** List of listeners that get notified when the recording changes. */
	private final List<RecordingChangeListener> listeners = new ArrayList<>();

	/** Manager for recording edit operations with undo/redo functionality. */
	private final RecordingEditManager editManager = new RecordingEditManager();

	/** The source file from which this recording was loaded. */
	private File sourceFile;

	/** Contains metadata about the recording such as title, presenter, date, etc. */
	private RecordingHeader header;

	/** Contains the recorded audio data. */
	private RecordedAudio audio;

	/** Contains the recorded document data and associated state. */
	private RecordedDocument document;

	/** Contains the recorded events data like annotations and actions. */
	private RecordedEvents events;


	/**
	 * Creates a new Recording instance with a default RecordingHeader.
	 * The other recording components (audio, document, events) remain uninitialized
	 * until explicitly set.
	 */
	public Recording() {
		header = new RecordingHeader();
	}

	/**
	 * Creates a new Recording instance by copying data from an existing recording.
	 * This constructor performs a deep copy of the source recording, including its document,
	 * audio, events, and header information.
	 *
	 * @param recording the source recording to copy data from.
	 *
	 * @throws IOException if an error occurs while copying the audio, document, or events data.
	 */
	public Recording(Recording recording) throws IOException {
		setRecordedDocument(new RecordedDocument(recording.getRecordedDocument().toByteArray()));
		setRecordedAudio(new RecordedAudio(recording.getRecordedAudio().getAudioStream().clone()));
		setRecordedEvents(new RecordedEvents(recording.getRecordedEvents().toByteArray()));
		setRecordingHeader(recording.getRecordingHeader().clone());
	}

	/**
	 * Gets the file from which this recording was loaded.
	 *
	 * @return the source file of this recording.
	 */
	public File getSourceFile() {
		return sourceFile;
	}

	/**
	 * Sets the file from which this recording was loaded.
	 *
	 * @param file the source file to set.
	 */
	public void setSourceFile(File file) {
		this.sourceFile = file;
	}

	/**
	 * Gets the edit manager that handles undo/redo operations for this recording.
	 *
	 * @return the recording edit manager.
	 */
	public RecordingEditManager getEditManager() {
		return editManager;
	}

	/**
	 * Gets the metadata header for this recording.
	 *
	 * @return the recording header containing metadata.
	 */
	public RecordingHeader getRecordingHeader() {
		return header;
	}

	/**
	 * Sets the metadata header for this recording and notifies listeners of the change.
	 *
	 * @param header the recording header to set.
	 */
	public void setRecordingHeader(RecordingHeader header) {
		this.header = header;

		fireChangeEvent(Content.HEADER);
	}

	/**
	 * Gets the audio component of this recording.
	 *
	 * @return the recorded audio data.
	 */
	public RecordedAudio getRecordedAudio() {
		return audio;
	}

	/**
	 * Sets the audio component of this recording and notifies listeners of the change.
	 *
	 * @param audio the recorded audio data to set.
	 */
	public void setRecordedAudio(RecordedAudio audio) {
		this.audio = audio;

		fireChangeEvent(Content.AUDIO);
	}

	/**
	 * Gets the events component of this recording.
	 *
	 * @return the recorded events' data.
	 */
	public RecordedEvents getRecordedEvents() {
		return events;
	}

	/**
	 * Sets the events component of this recording and notifies listeners of the change.
	 *
	 * @param actions the recorded events data to set.
	 */
	public void setRecordedEvents(RecordedEvents actions) {
		this.events = actions;

		fireChangeEvent(Content.EVENTS_REMOVED);
	}

	/**
	 * Gets the document component of this recording.
	 *
	 * @return the recorded document data.
	 */
	public RecordedDocument getRecordedDocument() {
		return document;
	}

	/**
	 * Sets the document component of this recording and notifies listeners of the change.
	 *
	 * @param document the recorded document data to set.
	 */
	public void setRecordedDocument(RecordedDocument document) {
		this.document = document;

		fireChangeEvent(Content.DOCUMENT);
	}

	/**
	 * Closes the document resources associated with this recording.
	 * This should be called when the recording is no longer needed.
	 */
	public void close() {
		if (nonNull(getRecordedDocument().getDocument())) {
			getRecordedDocument().getDocument().close();
		}
	}

	/**
	 * Undoes the last edit operation across all recording components.
	 * This method checks and executes undo operations for events, document, audio,
	 * and header components if available.
	 *
	 * @throws RecordingEditException if an error occurs during the undo operation.
	 */
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

	/**
	 * Redoes the last undone edit operation across all recording components.
	 * This method checks and executes redo operations for events, document, audio,
	 * and header components if available.
	 *
	 * @throws RecordingEditException if an error occurs during the redo operation.
	 */
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

	/**
	 * Checks if there are any undo operations available across all recording components.
	 *
	 * @return true if at least one part has an undo action available, false otherwise.
	 */
	public boolean hasUndoActions() {
		return getRecordingHeader().hasUndoActions() ||
				getRecordedEvents().hasUndoActions() ||
				getRecordedDocument().hasUndoActions() ||
				getRecordedAudio().hasUndoActions();
	}

	/**
	 * Checks if there are any redo operations available across all recording components.
	 *
	 * @return true if at least one part has a redo action available, false otherwise.
	 */
	public boolean hasRedoActions() {
		return getRecordingHeader().hasRedoActions() ||
				getRecordedEvents().hasRedoActions() ||
				getRecordedDocument().hasRedoActions() ||
				getRecordedAudio().hasRedoActions();
	}

	/**
	 * Generates a hash code representing the current state of the recording.
	 * This hash combines the state hashes of all recording components.
	 *
	 * @return an integer hash code representing the current state.
	 */
	public int getStateHash() {
		return Objects.hash(getRecordingHeader().getStateHash(),
				getRecordedEvents().getStateHash(),
				getRecordedDocument().getStateHash(),
				getRecordedAudio().getStateHash());
	}

	/**
	 * Registers a listener to be notified of changes to this recording.
	 *
	 * @param listener the listener to add.
	 */
	public void addRecordingChangeListener(RecordingChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a previously registered recording change listener.
	 *
	 * @param listener the listener to remove.
	 */
	public void removeRecordingChangeListener(RecordingChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Do not run this method in the UI Thread.
	 * Running it in the UI Thread might lead to freezes of the UI.
	 */
	public void fireChangeEvent(Content contentType) {
		fireChangeEvent(contentType, null);
	}

	/**
	 * Do not run this method in the UI Thread.
	 * Running it in the UI Thread might lead to freezes of the UI.
	 */
	public void fireChangeEvent(Content contentType, Interval<Double> duration) {
		RecordingChangeEvent event = new RecordingChangeEvent(this, contentType);
		event.setDuration(duration);

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
}
