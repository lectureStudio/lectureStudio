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

package org.lecturestudio.editor.api.edit;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;

/**
 * Action for inserting recorded events into an existing recording.
 * This class handles the insertion logic including splitting pages if necessary
 * and maintaining proper timing of events.
 *
 * @author Alex Andres
 */
public class InsertEventsAction extends RecordingInsertAction<RecordedEvents> {

	/** Flag indicating whether to split the page at the insertion point. */
	private final boolean split;

	/** Duration of the inserted events in milliseconds. */
	private final int duration;

	/** The index of the page where events will be inserted. */
	private final int startIndex;

	/** Serialized byte representation of the original events before modification. */
	private byte[] eventStream;

	/** Serialized byte representation of the events to be inserted. */
	private byte[] insertEventStream;


	/**
	 * Creates a new action for inserting events into a recording.
	 *
	 * @param recordedObject The target recording where events will be inserted.
	 * @param events         The events to insert into the recording.
	 * @param split          Flag indicating whether to split the page at the insertion point.
	 * @param startTime      The timestamp at which to insert the events.
	 * @param startIndex     The index of the page where events will be inserted.
	 * @param duration       The duration of the inserted events in milliseconds.
	 */
	public InsertEventsAction(RecordedEvents recordedObject,
			RecordedEvents events,
			boolean split,
			int startTime,
			int startIndex,
			int duration) {
		super(recordedObject, events, startTime);

		this.split = split;
		this.duration = duration;
		this.startIndex = startIndex;
	}

	@Override
	public void execute() throws RecordingEditException {
		if (startIndex < 0) {
			throw new RecordingEditException("Invalid insert position");
		}

		if (isNull(eventStream)) {
			try {
				eventStream = getRecordedObject().toByteArray();
			}
			catch (IOException e) {
				throw new RecordingEditException(e);
			}
		}
		if (isNull(insertEventStream)) {
			try {
				insertEventStream = objectToInsert.toByteArray();
			}
			catch (IOException e) {
				throw new RecordingEditException(e);
			}
		}

		List<RecordedPage> recPages = getRecordedObject().getRecordedPages();
		int insertIndex = startIndex;

		if (split) {
			// Insert events in the middle of the chosen page. Need to split the
			// page.
			RecordedPage startPage = recPages.get(startIndex);
			insertIndex++;

			// Create a new page with events occurred after the insertion point.
			RecordedPage splitPage = new RecordedPage();
			splitPage.setNumber(insertIndex);
			splitPage.setTimestamp(startTime);

			for (StaticShapeAction action : startPage.getStaticActions()) {
				splitPage.addStaticAction(action.clone());
			}

			Iterator<PlaybackAction> iter = startPage.getPlaybackActions().iterator();
			while (iter.hasNext()) {
				PlaybackAction action = iter.next();

				if (action.getTimestamp() > startTime) {
					splitPage.addPlaybackAction(action.clone());
					// Remove action from the starting page.
					iter.remove();
				}
			}

			// Shift pages after the insertion point to the right.
			for (int i = insertIndex; i < recPages.size(); i++) {
				RecordedPage page = recPages.get(i);
				page.setNumber(page.getNumber() + 1);
			}

			recPages.add(insertIndex, splitPage);
		}

		// Shift events in the current recording.
		Interval<Integer> shiftInterval = new Interval<>(startTime, startTime + duration);
		int insertPageCount = objectToInsert.getRecordedPages().size();

		for (int i = insertIndex; i < recPages.size(); i++) {
			RecordedPage page = recPages.get(i);

			page.shiftRight(shiftInterval);
			page.setNumber(page.getNumber() + insertPageCount);
		}

		// Shift events in the inserted recording.
		shiftInterval = new Interval<>(0, startTime);

		for (RecordedPage page : objectToInsert.getRecordedPages()) {
			page.shiftRight(shiftInterval);
			page.setNumber(page.getNumber() + insertIndex);
		}

		// Insert events.
		recPages.addAll(insertIndex, objectToInsert.getRecordedPages());
	}

	@Override
	public void undo() throws RecordingEditException {
		try {
			getRecordedObject().parseFrom(eventStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}

		try {
			objectToInsert.parseFrom(insertEventStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}
}
