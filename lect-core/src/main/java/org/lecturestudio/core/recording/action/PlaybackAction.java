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

package org.lecturestudio.core.recording.action;

import java.nio.ByteBuffer;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.recording.RecordedObject;

public abstract class PlaybackAction implements RecordedObject, Cloneable {

	public abstract ActionType getType();

	public abstract void execute(ToolController controller) throws Exception;

	private static final int KEY_EVENT_MASK = 1;

	private KeyEvent keyEvent;

	private int timestamp = 0;


	protected PlaybackAction() {
		
	}

	protected PlaybackAction(KeyEvent keyEvent) {
		setKeyEvent(keyEvent);
	}

	public void setKeyEvent(KeyEvent keyEvent) {
		this.keyEvent = keyEvent;
	}

	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

	public void setTimestamp(int time) {
		this.timestamp = time;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void shift(int time) {
		setTimestamp(getTimestamp() - time);
	}

	@Override
	public PlaybackAction clone() {
		PlaybackAction copy = null;

		try {
			copy = (PlaybackAction) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return copy;
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified length of
	 * the payload and inserts the required action header parameters.
	 * The buffer will be of the size of the specified length + the
	 * length of the header.
	 * 
	 * @param length The length of the payload of the specific action.
	 * 
	 * @return A new {@code ByteBuffer} with pre-filled action header.
	 */
	protected ByteBuffer createBuffer(int length) {
		int header = 0;

		if (keyEvent != null) {
			length += 9;

			// Set the flag in the header.
			header |= KEY_EVENT_MASK;
		}

		ByteBuffer buffer = createActionBuffer(length + 4);

		// Set header.
		buffer.putInt(header);

		if (keyEvent != null) {
			// KeyEvent: 9 bytes.
			buffer.putInt(keyEvent.getKeyCode());
			buffer.putInt(keyEvent.getModifiers());
			buffer.put((byte) keyEvent.getEventType().ordinal());
		}

		return buffer;
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified payload
	 * to read from and reads default action fields, if any present.
	 * 
	 * @param input The action payload data.
	 * 
	 * @return A new {@code ByteBuffer} to read specific action fields.
	 */
	protected ByteBuffer createBuffer(byte[] input) {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		int header = buffer.getInt();

		if ((header & KEY_EVENT_MASK) == KEY_EVENT_MASK) {
			// KeyEvent
			int keyCode = buffer.getInt();
			int modifiers = buffer.getInt();
			KeyEvent.EventType eventType = KeyEvent.EventType.values()[buffer.get()];

			keyEvent = new KeyEvent(keyCode, modifiers, eventType);
		}

		return buffer;
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified length of
	 * the payload and inserts the required action header parameters.
	 * The buffer will be of the size of the specified length + the
	 * length of the header.
	 * 
	 * @param length The length of the payload of the specific action.
	 * 
	 * @return A new {@code ByteBuffer} with pre-filled action header.
	 */
	private ByteBuffer createActionBuffer(int length) {
		ByteBuffer buffer = ByteBuffer.allocate(length + 9);

		// Write header.
		buffer.putInt(length + 5);
		buffer.put((byte) getType().ordinal());
		buffer.putInt(getTimestamp());

		return buffer;
	}

}
