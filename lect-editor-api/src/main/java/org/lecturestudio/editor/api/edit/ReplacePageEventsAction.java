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

/**
 * Action that replaces PlaybackActions for a specific page in a Recording.
 * <p>
 * This class handles the addition and removal of PlaybackAction elements for a given page
 * in a recording, maintaining the ability to undo and redo these changes. It preserves
 * the timestamp ordering of actions when inserting new playback actions.
 *
 * @author Alex Andres
 */
public class ReplacePageEventsAction extends RecordedObjectAction<RecordedEvents> {

	/** The recording to be modified. */
	private final Recording recording;

	/** The page number identifying the page in the recording whose events will be replaced. */
	private final int pageNumber;

	/** The list of PlaybackAction objects to add to the page. */
	private final List<PlaybackAction> addActions;

	/** The list of PlaybackAction objects to remove from the page. */
	private final List<PlaybackAction> removeActions;

	/** The type of change being made to the recording content. */
	private final Recording.Content changeType;

	/** Stores the original actions before modification for undo operations. */
	private List<PlaybackAction> savedActions;


	/**
	 * Constructs a new ReplacePageEventsAction that replaces page events in the specified recording.
	 *
	 * @param recording     The recording whose events will be modified.
	 * @param addActions    The list of PlaybackAction objects to add to the page.
	 * @param removeActions The list of PlaybackAction objects to remove from the page.
	 * @param pageNumber    The page number identifying the page in the recording whose events will be replaced.
	 *
	 * @throws IllegalArgumentException If both addActions and removeActions lists are empty.
	 */
	public ReplacePageEventsAction(Recording recording,
			List<PlaybackAction> addActions, List<PlaybackAction> removeActions,
			int pageNumber) {
		super(recording.getRecordedEvents());

		this.recording = recording;
		this.pageNumber = pageNumber;
		this.addActions = addActions;
		this.removeActions = removeActions;

		if (!addActions.isEmpty() && !removeActions.isEmpty()) {
			this.changeType = Content.EVENTS_CHANGED;
		}
		else if (!addActions.isEmpty()) {
			this.changeType = Content.EVENTS_ADDED;
		}
		else if (!removeActions.isEmpty()) {
			this.changeType = Content.EVENTS_REMOVED;
		}
		else {
			throw new IllegalArgumentException("At least one of addActions or removeActions must not be empty.");
		}
	}

	@Override
	public void undo() throws RecordingEditException {
		List<PlaybackAction> actions = getPageActions();

		actions.clear();
		actions.addAll(savedActions);

		// Notify listeners that the recording events have been changed after restoring the original actions.
		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();

		// Notify listeners that the recording events have been changed after redoing the action.
		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	@Override
	public void execute() throws RecordingEditException {
		List<PlaybackAction> actions = getPageActions();

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

		recording.fireChangeEvent(changeType);
	}

	private List<PlaybackAction> getPageActions() {
		RecordedEvents lecturePages = getRecordedObject();
		RecordedPage recordedPage = lecturePages.getRecordedPage(pageNumber);

		return recordedPage.getPlaybackActions();
	}
}
