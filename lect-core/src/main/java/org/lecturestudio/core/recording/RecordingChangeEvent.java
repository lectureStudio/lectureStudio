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

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.recording.Recording.Content;

/**
 * Represents an event that signals changes in a recording file.
 * This class is used to track modifications to recording content and duration.
 *
 * @author Alex Andres
 */
public class RecordingChangeEvent {

	/** The recording file that was changed. */
	private final Recording file;

	/** The type of content that was modified in the recording. */
	private final Content contentType;

	/** The time interval that was affected by the change. */
	private Interval<Double> duration;


	/**
	 * Creates a new recording change event for the specified recording and content type.
	 *
	 * @param file        The recording file that changed.
	 * @param contentType The type of content that was modified.
	 */
	public RecordingChangeEvent(Recording file, Content contentType) {
		this.file = file;
		this.contentType = contentType;
	}

	/**
	 * Gets the recording associated with this change event.
	 *
	 * @return The recording that was modified.
	 */
	public Recording getRecording() {
		return file;
	}

	/**
	 * Gets the type of content that was modified.
	 *
	 * @return The content type that was affected.
	 */
	public Content getContentType() {
		return contentType;
	}

	/**
	 * Gets the duration or time interval that was affected by the change.
	 *
	 * @return The duration of the change, or null if not set.
	 */
	public Interval<Double> getDuration() {
		return duration;
	}

	/**
	 * Sets the duration or time interval that was affected by the change.
	 *
	 * @param duration The time interval to set.
	 */
	public void setDuration(Interval<Double> duration) {
		this.duration = duration;
	}
}
