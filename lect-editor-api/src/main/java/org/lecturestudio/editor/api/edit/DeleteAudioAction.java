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
import java.util.List;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;

/**
 * An edit action that deletes (excludes) a portion of audio from a recording.
 * <p>
 * The interval is specified in virtual time coordinates (milliseconds in the
 * already-edited timeline). The action properly converts to physical byte
 * positions accounting for any previous exclusions.
 */
public class DeleteAudioAction extends RecordedObjectAction<RecordedAudio> {

	/** The time interval to delete (in virtual milliseconds). */
	private final Interval<Integer> interval;

	/** Saved exclusions for undo (in physical byte positions). */
	private final List<Interval<Long>> savedExclusions;


	/**
	 * Creates a new delete audio action.
	 *
	 * @param lectureObject The recorded audio to modify.
	 * @param interval      The time interval to delete (in virtual milliseconds).
	 */
	public DeleteAudioAction(RecordedAudio lectureObject, Interval<Integer> interval) {
		super(lectureObject);

		// Save current exclusions for undo (getExclusions() returns a deep copy)
		this.savedExclusions = getRecordedObject().getAudioStream().getExclusions();
		this.interval = interval;
	}

	@Override
	public void undo() throws RecordingEditException {
		RecordedAudio recordedAudio = getRecordedObject();
		RandomAccessAudioStream audioStream = recordedAudio.getAudioStream().clone();

		// Restore the saved exclusions
		audioStream.setExclusions(savedExclusions);

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

		// Convert virtual time interval to physical byte positions and add exclusion.
		// The new API handles coordinate translation properly.
		long virtualStartMillis = interval.getStart();
		long virtualEndMillis = interval.getEnd();

		// Convert virtual milliseconds to physical byte positions
		long physicalStart = audioStream.virtualMillisToPhysicalBytes(virtualStartMillis);
		long physicalEnd = audioStream.virtualMillisToPhysicalBytes(virtualEndMillis);

		audioStream.addExclusion(new Interval<>(physicalStart, physicalEnd));

		try {
			getRecordedObject().setAudioStream(audioStream);
		}
		catch (IOException e) {
			throw new RecordingEditException(e);
		}
	}
}
