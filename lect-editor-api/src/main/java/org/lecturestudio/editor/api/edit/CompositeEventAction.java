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
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class CompositeEventAction extends RecordedObjectAction<RecordedEvents> {

	private final Recording recording;

	private final List<RecordedObjectAction<RecordedEvents>> actions;


	public CompositeEventAction(RecordedEvents recordedObject,
			Recording recording) {
		this(recordedObject, recording, new ArrayList<>());
	}

	public CompositeEventAction(RecordedEvents recordedObject,
			Recording recording,
			List<RecordedObjectAction<RecordedEvents>> actions) {
		super(recordedObject);

		this.recording = recording;
		this.actions = actions;
	}

	@Override
	public void undo() throws RecordingEditException {
		try {
			for (var action : actions) {
				action.undo();
			}
		}
		catch (Throwable e) {
			throw new RecordingEditException(e);
		}

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	@Override
	public void redo() throws RecordingEditException {
		try {
			for (var action : actions) {
				action.redo();
			}
		}
		catch (Throwable e) {
			throw new RecordingEditException(e);
		}

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	@Override
	public void execute() throws RecordingEditException {
		try {
			for (var action : actions) {
				action.execute();
			}
		}
		catch (Throwable e) {
			throw new RecordingEditException(e);
		}

		recording.fireChangeEvent(Content.EVENTS_CHANGED);
	}

	public void addAction(RecordedObjectAction<RecordedEvents> action) {
		actions.add(action);
	}
}
