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

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class DeleteEventAction extends RecordedObjectAction<RecordedEvents> {

	private final List<PlaybackAction> removedActions = new ArrayList<>();

	private final PlaybackAction action;

	private final int pageNumber;

	private int actionIndex;


	public DeleteEventAction(RecordedEvents lectureObject, PlaybackAction action,
							 int pageNumber) {
		super(lectureObject);

		this.action = action;
		this.pageNumber = pageNumber;
	}

	@Override
	public void execute() {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		actionIndex = actions.indexOf(action);
		ActionType initialType = action.getType();

		if (actionIndex < 0) {
			throw new IllegalArgumentException("RecordedPage does not contain the event to delete");
		}

		var iter = actions.listIterator(actionIndex);

		while (iter.hasNext()) {
			var iterAction = iter.next();
			var actionType = iterAction.getType();

			if (actionType != ActionType.TOOL_BEGIN
					&& actionType != ActionType.TOOL_EXECUTE
					&& actionType != ActionType.TOOL_END
					&& !iterAction.equals(action)
					&& actionType != initialType) {
				break;
			}

			iter.remove();

			removedActions.add(iterAction);

			if (actionType == ActionType.TOOL_END) {
				break;
			}
		}
	}

	@Override
	public void undo() {
		if (actionIndex < 0) {
			return;
		}

		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);

		recordedPage.getPlaybackActions().addAll(actionIndex, removedActions);

		removedActions.clear();
	}

	@Override
	public void redo() {
		execute();
	}

}
