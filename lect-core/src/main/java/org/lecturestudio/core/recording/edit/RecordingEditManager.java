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

package org.lecturestudio.core.recording.edit;

import java.util.Objects;
import java.util.Stack;

import org.lecturestudio.core.recording.RecordingEditException;

/**
 * The RecordingEditManager manages a list of {@link EditAction}s, providing a
 * way to undo or redo the appropriate recording edits.
 *
 * @author Alex Andres
 */
public class RecordingEditManager {

	private final Stack<EditAction> undoActions = new Stack<>();

	private final Stack<EditAction> redoActions = new Stack<>();


	/**
	 * Adds an EditAction to this RecordingEditManager. This removes all edits
	 * from the redo stack.
	 *
	 * @param action The edit action to add.
	 *
	 * @throws RecordingEditException If the edit action cannot be executed.
	 */
	public void addEditAction(EditAction action) throws RecordingEditException {
		action.execute();

		undoActions.push(action);

		// Clear the redo stack.
		redoActions.clear();
	}

	/**
	 * Undoes the appropriate recording edits.
	 *
	 * @throws RecordingEditException If the edits cannot be undone.
	 */
	public void undo() throws RecordingEditException {
		if (!hasUndoActions()) {
			return;
		}

		EditAction action = undoActions.pop();
		action.undo();

		redoActions.push(action);
	}

	/**
	 * Redoes the appropriate recording edits.
	 *
	 * @throws RecordingEditException If the edits cannot be redone.
	 */
	public void redo() throws RecordingEditException {
		if (!hasRedoActions()) {
			return;
		}

		EditAction action = redoActions.pop();
		action.redo();

		undoActions.push(action);
	}

	/**
	 * @return {@code true} if recording edits may be undone, otherwise {@code false}.
	 */
	public boolean hasUndoActions() {
		return !undoActions.empty();
	}

	/**
	 * @return {@code true} if recording edits may be redone, otherwise {@code false}.
	 */
	public boolean hasRedoActions() {
		return !redoActions.empty();
	}

	/**
	 * @return the hash value of the current edit state.
	 */
	public int getStateHash() {
		return Objects.hash(undoActions, redoActions);
	}
}
