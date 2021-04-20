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

package org.lecturestudio.core.recording.edit;

import org.lecturestudio.core.recording.RecordingEditException;

/**
 * Defines an editing action on a {@code Recording}. An editing action can undo
 * and redo the changes it exerts on the recording. EditActions can be added to
 * the {@code RecordingEditManager} that will take over the execution of the
 * action.
 *
 * @author Alex Andres
 *
 * @see org.lecturestudio.core.recording.Recording
 * @see RecordingEditManager
 */
public interface EditAction {

	/**
	 * Undoes the changes applied by this action.
	 *
	 * @throws RecordingEditException If the edits cannot be undone.
	 */
	void undo() throws RecordingEditException;

	/**
	 * Redoes the changes applied by this action.
	 *
	 * @throws RecordingEditException If the edits cannot be redone.
	 */
	void redo() throws RecordingEditException;

	/**
	 * Executes and applies the changes defined by this action.
	 *
	 * @throws RecordingEditException If the edits cannot be executed.
	 */
	void execute() throws RecordingEditException;

}
