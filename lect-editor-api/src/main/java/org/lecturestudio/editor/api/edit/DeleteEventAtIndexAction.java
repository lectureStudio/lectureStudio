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

import java.util.List;

import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class DeleteEventAtIndexAction extends RecordedObjectAction<RecordedEvents> {

	private final PlaybackAction action;

	private PlaybackAction refAction;

	private final int pageNumber;

	private int actionIndex;

	private int refIndex;


	public DeleteEventAtIndexAction(RecordedEvents lectureObject,
			PlaybackAction action, int pageNumber) {
		super(lectureObject);

		RecordedPage recordedPage = lectureObject.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		this.action = action;
		this.actionIndex = actions.indexOf(action);
		this.refAction = actions.get(actionIndex - 1);
		this.pageNumber = pageNumber;
	}

	@Override
	public void execute() {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		if (actionIndex < 0) {
			throw new IllegalArgumentException(
					"RecordedPage does not contain the action to delete");
		}

		if (!actions.remove(action)) {
			System.out.println("Action could not be removed");
		}
	}

	@Override
	public void undo() {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		int actionIndex = actions.indexOf(refAction);
		if (actionIndex < 0) {
			System.out.println("Action could not be removed");
			return;
		}

		actions.add(actionIndex + 1, action);
	}

	@Override
	public void redo() {
		execute();
	}

}
