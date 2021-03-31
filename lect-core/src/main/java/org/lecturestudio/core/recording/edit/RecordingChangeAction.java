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

import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingEditException;

public class RecordingChangeAction implements EditAction {

	private final Recording recording;


	public RecordingChangeAction(Recording recording) {
		this.recording = recording;
	}

	@Override
	public void undo() throws RecordingEditException {
		recording.undo();
	}

	@Override
	public void redo() throws RecordingEditException {
		recording.redo();
	}

	@Override
	public void execute() throws RecordingEditException {

	}
}
