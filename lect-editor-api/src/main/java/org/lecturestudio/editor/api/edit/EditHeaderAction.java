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

import org.lecturestudio.core.recording.RecordingHeader;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class EditHeaderAction extends RecordedObjectAction<RecordingHeader> {

	private final long durationDelta;


	public EditHeaderAction(RecordingHeader header, long durationDelta) {
		super(header);

		this.durationDelta = durationDelta;
	}

	@Override
	public void undo() {
		RecordingHeader header = getRecordedObject();
		header.setDuration(header.getDuration() - durationDelta);
	}

	@Override
	public void redo() {
		execute();
	}

	@Override
	public void execute() {
		RecordingHeader header = getRecordedObject();
		header.setDuration(header.getDuration() + durationDelta);
	}

}
