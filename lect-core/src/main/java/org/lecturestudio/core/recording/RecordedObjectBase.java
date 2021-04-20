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

package org.lecturestudio.core.recording;

import java.util.Objects;
import java.util.Stack;

import org.lecturestudio.core.recording.edit.EditableRecordedObject;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public abstract class RecordedObjectBase implements EditableRecordedObject, RecordedObject {

	protected Stack<RecordedObjectAction<?>> undoActions = new Stack<>();
	protected Stack<RecordedObjectAction<?>> redoActions = new Stack<>();


	@Override
	public void undo() throws RecordingEditException {
		if (!hasUndoActions()) {
			return;
		}

		RecordedObjectAction<?> aEditAction = undoActions.pop();
		aEditAction.undo();

		redoActions.push(aEditAction);
	}

	@Override
	public void redo() throws RecordingEditException {
		if (!hasRedoActions()) {
			return;
		}

		RecordedObjectAction<?> aEditAction = redoActions.pop();
		aEditAction.redo();

		undoActions.push(aEditAction);
	}

	@Override
	public void addEditAction(RecordedObjectAction<?> action) {
		undoActions.push(action);
		redoActions.clear();
	}

	public boolean hasUndoActions() {
		return !undoActions.empty();
	}

	public boolean hasRedoActions() {
		return !redoActions.empty();
	}

	public int getStateHash() {
		return Objects.hash(undoActions, redoActions);
	}
}
