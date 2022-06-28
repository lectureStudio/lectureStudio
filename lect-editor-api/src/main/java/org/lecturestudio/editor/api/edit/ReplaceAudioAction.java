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

import java.io.IOException;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

public class ReplaceAudioAction extends RecordedObjectAction<RecordedAudio> {

	private final RandomAccessAudioStream oldStream;
	private final RandomAccessAudioStream newStream;


	public ReplaceAudioAction(RecordedAudio audio, RandomAccessAudioStream newStream) {
		super(audio);

		this.newStream = newStream;
		this.oldStream = audio.getAudioStream();
	}

	@Override
	public void undo() throws RecordingEditException {
		try {
			getRecordedObject().setAudioStream(oldStream.clone());
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		try {
			getRecordedObject().setAudioStream(newStream.clone());
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

}
