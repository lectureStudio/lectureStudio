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
import java.util.ListIterator;

import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.ZoomAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class DeleteCompositeEventsAction extends RecordedObjectAction<RecordedEvents> {

	private final List<PlaybackAction> actions;

	private final int pageNumber;

	private List<PlaybackAction> savedActions;


	public DeleteCompositeEventsAction(RecordedEvents lectureObject,
									   List<PlaybackAction> actions, int pageNumber) {
		super(lectureObject);

		this.actions = actions;
		this.pageNumber = pageNumber;
	}

	@Override
	public void execute() throws RecordingEditException {
		List<PlaybackAction> pageActions = getPageActions();

		savedActions = new ArrayList<>(pageActions);

		for (PlaybackAction action : actions) {
			int actionIndex = pageActions.indexOf(action);
			if (actionIndex < 0) {
				throw new RecordingEditException("RecordedPage does not contain any action to delete");
			}

			ListIterator<PlaybackAction> iterator = pageActions.listIterator(actionIndex);

			deleteAction(action, iterator);

			if (action instanceof ZoomAction) {
				deleteZoomAction(iterator);
			}
		}
	}

	@Override
	public void undo() {
		List<PlaybackAction> actions = getPageActions();

		actions.clear();
		actions.addAll(savedActions);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	private List<PlaybackAction> getPageActions() {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);

		return recordedPage.getPlaybackActions();
	}

	private void deleteAction(PlaybackAction action,
			ListIterator<PlaybackAction> iterator) {
		while (iterator.hasNext()) {
			var iterAction = iterator.next();
			var actionType = iterAction.getType();

			if (!iterAction.equals(action)
					&& iterAction.hasHandle()
					&& action.hasHandle()
					&& iterAction.getHandle() != action.getHandle()) {
				// End the deletion, if and only if both actions contain handles,
				// which do not match.
				break;
			}

			iterator.remove();

			if (actionType == ActionType.TOOL_END || actionType == ActionType.ZOOM_OUT) {
				break;
			}
		}
	}

	private void deleteZoomAction(ListIterator<PlaybackAction> iterator) {
		while (iterator.hasNext()) {
			var action = iterator.next();
			var actionType = action.getType();

			switch (actionType) {
				case ZOOM -> {
					// Ran into a separate zoom action, keep it intact.
					return;
				}
				case ZOOM_OUT -> {
					// Done here removing connected actions.
					iterator.remove();
					return;
				}
				case PANNING -> {
					// Remove pan action belonging to the zoomed state action.
					iterator.remove();
					deleteAction(action, iterator);
				}
			}
		}
	}
}
