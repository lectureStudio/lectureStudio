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

package org.lecturestudio.media.recording;

import org.lecturestudio.core.bus.event.BusEvent;
import org.lecturestudio.core.recording.Recording;

public class RecordingEvent extends BusEvent {

	public enum Type { CREATED, CLOSED, SELECTED };

	private final Recording oldRecording;

	private final Recording recording;

	private final Type type;


	public RecordingEvent(Recording recording, Type type) {
		this(null, recording, type);
	}

	public RecordingEvent(Recording oldRecording, Recording recording, Type type) {
		this.oldRecording = oldRecording;
		this.recording = recording;
		this.type = type;
	}

	public Recording getOldRecording() {
		return oldRecording;
	}

	public Recording getRecording() {
		return recording;
	}

	public Type getType() {
		return type;
	}

	public boolean created() {
		return type == Type.CREATED;
	}

	public boolean closed() {
		return type == Type.CLOSED;
	}

	public boolean selected() {
		return type == Type.SELECTED;
	}
}
