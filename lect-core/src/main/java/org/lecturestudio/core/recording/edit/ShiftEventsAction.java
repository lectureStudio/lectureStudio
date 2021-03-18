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

import java.util.List;

import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;

public class ShiftEventsAction extends RecordingEditAction<RecordedEvents> {

	private final int millis;


	public ShiftEventsAction(RecordedEvents lectureObject, int millis) {
		super(lectureObject);

		this.millis = millis;
	}

	@Override
	public void undo() {
		int millis = this.millis * -1;

		RecordedEvents lectureEvents = getRecordedObject();
		List<RecordedPage> pages = lectureEvents.getRecordedPages();

		for (RecordedPage recPage : pages) {
			recPage.setTimestamp(recPage.getTimestamp() + millis);

			for (PlaybackAction action : recPage.getPlaybackActions()) {
				action.setTimestamp(action.getTimestamp() + millis);
			}
		}
	}

	@Override
	public void redo() {
		execute();
	}

	@Override
	public void execute() {
		RecordedEvents lectureEvents = getRecordedObject();
		List<RecordedPage> pages = lectureEvents.getRecordedPages();

		for (RecordedPage recPage : pages) {
			recPage.setTimestamp(recPage.getTimestamp() + millis);

			for (PlaybackAction action : recPage.getPlaybackActions()) {
				action.setTimestamp(action.getTimestamp() + millis);
			}
		}
	}

}
