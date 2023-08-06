/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * Base class for specific {@code EditAction}s that maintains granular
 * sub-actions and the {@code Recording} on which the action is applied to.
 *
 * @author Alex Andres
 */
public abstract class RecordingAction implements EditAction {

	/**
	 * The recording on which to operate.
	 */
	protected final Recording recording;

	/**
	 * Sub-actions for convenient handling of granular operations.
	 */
	protected final List<EditAction> editActions;


	/**
	 * Constructor for a {@code RecordingAction} to be used by specific action
	 * implementations.
	 *
	 * @param recording The recording on which to apply this action.
	 * @param actions   The sub-actions to manage.
	 */
	protected RecordingAction(Recording recording, List<EditAction> actions) {
		this.recording = recording;
		this.editActions = actions;
	}

	@Override
	public void undo() throws RecordingEditException {
		var duration = getEditDuration();

		for (EditAction action : editActions) {
			action.undo();
		}

		fireChangeEvent(duration);
	}

	@Override
	public void redo() throws RecordingEditException {
		var duration = getEditDuration();

		for (EditAction action : editActions) {
			action.redo();
		}

		fireChangeEvent(duration);
	}

	@Override
	public void execute() throws RecordingEditException {
		var duration = getEditDuration();

		for (EditAction action : editActions) {
			action.execute();
		}

		fireChangeEvent(duration);
	}

	/**
	 * Returns the duration this action comprises. This method is meant to be
	 * overridden by sub-classes to provide specific duration. By default
	 * {@code null} is returned to indicate to ignore this value.
	 *
	 * @return The duration this action comprises.
	 */
	protected Interval<Double> getEditDuration() {
		return null;
	}

	protected void fireChangeEvent(Interval<Double> duration) {
		recording.fireChangeEvent(Content.ALL, duration);
	}
}
