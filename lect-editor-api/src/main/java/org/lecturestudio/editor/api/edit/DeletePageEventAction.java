/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import java.util.List;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * Removes a single action from the page on which it is located. The duration of
 * the recording remains untouched and no parts of the recording other than the
 * event-stream are changed.
 *
 * @author Alex Andres
 */
public class DeletePageEventAction extends RecordingAction {

	/**
	 * Constructor for a {@code RecordingAction} to be used by specific action
	 * implementations.
	 *
	 * @param recording  The recording on which to apply this action.
	 * @param action     The action to delete.
	 * @param pageNumber The page number on which the action is located.
	 */
	public DeletePageEventAction(Recording recording, PlaybackAction action,
			int pageNumber) {
		super(recording, createActions(recording, action, pageNumber));
	}

	private static List<EditAction> createActions(Recording recording,
			PlaybackAction action, int pageNumber) {
		RecordedEvents lectureEvents = recording.getRecordedEvents();
		DeleteEventAction deleteAction = new DeleteEventAction(lectureEvents,
				action, pageNumber);

		return List.of(deleteAction);
	}

	@Override
	protected void fireChangeEvent(Interval<Double> duration) {
		recording.fireChangeEvent(Content.EVENTS, duration);
	}
}
