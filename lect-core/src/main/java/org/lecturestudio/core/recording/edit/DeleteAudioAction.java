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

import java.io.IOException;

import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.util.AudioUtils;

public class DeleteAudioAction extends RecordingEditAction<RecordedAudio> {

	private final Interval<Long> cutInterval;


	public DeleteAudioAction(RecordedAudio lectureObject, Interval<Integer> interval) {
		super(lectureObject);

		cutInterval = getCutInterval(interval);
	}

	@Override
	public void undo() throws RecordingEditException {
		RecordedAudio recordedAudio = getRecordedObject();
		RandomAccessAudioStream audioStream = recordedAudio.getAudioStream().clone();
		audioStream.removeExclusion(cutInterval);

		try {
			recordedAudio.setAudioStream(audioStream);
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
		RandomAccessAudioStream stream = getRecordedObject().getAudioStream();
		RandomAccessAudioStream audioStream = stream.clone();
		audioStream.addExclusion(cutInterval);

		try {
			getRecordedObject().setAudioStream(audioStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}

	private Interval<Long> getCutInterval(Interval<Integer> interval) {
		RandomAccessAudioStream stream = getRecordedObject().getAudioStream();
		AudioFormat audioFormat = stream.getAudioFormat();

		long startBytePosition = AudioUtils.getAudioBytePosition(audioFormat, interval.getStart());
		long endBytePosition = AudioUtils.getAudioBytePosition(audioFormat, interval.getEnd());

		// Handle padding created by previous exclusions.
		long padding = stream.getPadding(startBytePosition);

		return new Interval<>(padding + startBytePosition, padding + endBytePosition);
	}
}
