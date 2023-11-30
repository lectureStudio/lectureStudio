/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class ReplacePageEventsAction extends RecordedObjectAction<RecordedEvents> {

	private final Recording recording;

	private final int pageNumber;

	private final List<PlaybackAction> addActions;

	private final List<PlaybackAction> removeActions;

	private List<PlaybackAction> savedActions;


	public ReplacePageEventsAction(Recording recording,
			List<PlaybackAction> addActions, List<PlaybackAction> removeActions,
			int pageNumber) {
		super(recording.getRecordedEvents());

		this.recording = recording;
		this.pageNumber = pageNumber;
		this.addActions = addActions;
		this.removeActions = removeActions;
	}

	@Override
	public void undo() throws RecordingEditException {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		actions.clear();
		actions.addAll(savedActions);

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);
		List<PlaybackAction> actions = recordedPage.getPlaybackActions();

		savedActions = new ArrayList<>(actions);

		try {
			actions.removeAll(removeActions);

			int firstTimeStamp = addActions.get(0).getTimestamp();
			PlaybackAction insertAfterAction = actions.stream().reduce(null, (previous, action) -> {
				if (action.getTimestamp() < firstTimeStamp) {
					return action;
				}
				return previous;
			});

			if (insertAfterAction != null) {
				actions.addAll(actions.indexOf(insertAfterAction) + 1, addActions);
			}
			else {
				actions.addAll(0, addActions);
			}
		}
		catch (Throwable e) {
			throw new RecordingEditException(e);
		}

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}
}
