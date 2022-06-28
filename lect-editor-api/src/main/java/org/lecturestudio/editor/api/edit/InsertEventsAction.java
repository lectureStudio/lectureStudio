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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;

public class InsertEventsAction extends RecordingInsertAction<RecordedEvents> {

	private final boolean split;

	private final int duration;

	private final int startIndex;

	private byte[] eventStream;


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

		try {
			eventStream = getRecordedObject().toByteArray();
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
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
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}
}
