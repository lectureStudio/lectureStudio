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

package org.lecturestudio.core.input;

public class KeyEvent {

	/** Enum with the types of the key event. */
	public enum EventType { PRESSED, RELEASED, TYPED }
	
	/** The Shift key modifier constant. */
	public static final int SHIFT_MASK = 1 << 1;

	/** The Control key modifier constant. */
	public static final int CTRL_MASK = 1 << 2;

	/** The Alt key modifier constant. */
	public static final int ALT_MASK = 1 << 3;
	
	/** The AltGraph key modifier constant. */
    public static final int ALT_GRAPH_MASK = 1 << 4;

	/** The event type. */
    private final EventType eventType;

    /** The key code. */
    private final int keyCode;

    /** The modifiers. */
	private final int modifiers;

	/**
	 * Creates a new instance of {@link KeyEvent} with the specified key code.
	 * (Calls {@link #KeyEvent(int, int, EventType)} with {@code 0} as modifiers and
	 * {@code EventType.PRESSED} as event type.)
	 *
	 * @param keyCode The key code.
	 */
	public KeyEvent(int keyCode) {
		this(keyCode, 0, EventType.PRESSED);
	}

	/**
	 * Creates a new instance of {@link KeyEvent} with the specified key code and modifiers.
	 * (Calls {@link #KeyEvent(int, int, EventType)} with {@code EventType.PRESSED} as event type.)
	 *
	 * @param keyCode The key code.
	 * @param modifiers The modifiers.
	 */
	public KeyEvent(int keyCode, int modifiers) {
		this(keyCode, modifiers, EventType.PRESSED);
	}

	/**
	 * Creates a new instance of {@link KeyEvent} with the specified modifiers and event type.
	 * (Calls {@link #KeyEvent(int, int, EventType)} with {@code 0} as key code.)
	 *
	 * @param modifiers The modifiers.
	 * @param eventType The event type.
	 */
	public KeyEvent(int modifiers, EventType eventType) {
		this(0, modifiers, eventType);
	}

	/**
	 * Creates a new instance of {@link KeyEvent} with the specified key code, modifiers and event type.
	 *
	 * @param keyCode The key code.
	 * @param modifiers The modifiers.
	 * @param eventType The event type.
	 */
	public KeyEvent(int keyCode, int modifiers, EventType eventType) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
		this.eventType = eventType;
	}

	/**
	 * Get the key code.
	 *
	 * @return The key code.
	 */
	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * Get the modifiers.
	 *
	 * @return The modifiers.
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Get the event type.
	 *
	 * @return The event type.
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * Indicates whether the key was released.
	 *
	 * @return {@code true} if the event type equals {@code EventType.RELEASED}, otherwise {@code false}.
	 */
	public boolean isReleased() {
		return eventType == EventType.RELEASED;
	}
	
	/**
	 * Indicates whether the Shift key is down.
	 */
	public boolean isShiftDown() {
		return (modifiers & SHIFT_MASK) != 0;
	}

	/**
	 * Indicates whether the Control key is down.
	 */
	public boolean isControlDown() {
		return (modifiers & CTRL_MASK) != 0;
	}

	/**
	 * Indicates whether the Alt key is down.
	 */
	public boolean isAltDown() {
		return (modifiers & ALT_MASK) != 0;
	}

	/**
	 * Indicates whether the AltGraph key is down.
	 */
	public boolean isAltGraphDown() {
		return (modifiers & ALT_GRAPH_MASK) != 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + keyCode;
		result = prime * result + modifiers;
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		KeyEvent other = (KeyEvent) obj;

		return eventType == other.eventType && keyCode == other.keyCode && modifiers == other.modifiers;
	}

	@Override
	public String toString() {
		return "KeyCode: " + keyCode + ", " +
				"EventType: " + eventType + ", " +
				"Shift: " + isShiftDown() + ", " +
				"Control: " + isControlDown() + ", " +
				"Alt: " + isAltDown() + ", " +
				"AltGraph: " + isAltGraphDown();
	}
}
