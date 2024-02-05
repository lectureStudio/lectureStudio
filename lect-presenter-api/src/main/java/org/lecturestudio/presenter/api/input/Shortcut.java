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

package org.lecturestudio.presenter.api.input;

import org.lecturestudio.core.input.KeyCode;
import org.lecturestudio.core.input.KeyEvent;

public enum Shortcut {

	APP_CLOSE						(KeyCode.Q, KeyEvent.CTRL_MASK),

	CLOSE_VIEW						(KeyCode.ESCAPE),

	DOC_OPEN						(KeyCode.O, KeyEvent.CTRL_MASK),
	DOC_CLOSE						(KeyCode.F4, KeyEvent.CTRL_MASK),

	SLIDE_FIRST						(KeyCode.HOME),
	SLIDE_LAST						(KeyCode.END),

	SLIDE_NEXT_RIGHT				(KeyCode.RIGHT),
	SLIDE_NEXT_DOWN					(KeyCode.DOWN),
	SLIDE_NEXT_PAGE_DOWN			(KeyCode.PAGE_DOWN),
	SLIDE_NEXT_SPACE				(KeyCode.SPACE),
	SLIDE_NEXT_10					(KeyCode.RIGHT, KeyEvent.SHIFT_MASK),
	SLIDE_PREVIOUS_LEFT				(KeyCode.LEFT),
	SLIDE_PREVIOUS_UP				(KeyCode.UP),
	SLIDE_PREVIOUS_BACK_SPACE		(KeyCode.BACK_SPACE),
	SLIDE_PREVIOUS_PAGE_UP			(KeyCode.PAGE_UP),
	SLIDE_PREVIOUS_10				(KeyCode.LEFT, KeyEvent.SHIFT_MASK),
	SLIDE_NEW						(KeyCode.F9),
	SLIDE_DELETE					(KeyCode.D, KeyEvent.CTRL_MASK),
	SLIDE_PAN						(KeyCode.F11),

	SLIDE_OVERLAY_START				(KeyCode.UP, KeyEvent.SHIFT_MASK),
	SLIDE_OVERLAY_END				(KeyCode.DOWN, KeyEvent.SHIFT_MASK),
	SLIDE_OVERLAY_PREVIOUS			(KeyCode.PAGE_UP, KeyEvent.SHIFT_MASK),
	SLIDE_OVERLAY_NEXT				(KeyCode.PAGE_DOWN, KeyEvent.SHIFT_MASK),

	COPY_OVERLAY					(KeyCode.V, KeyEvent.CTRL_MASK),
	COPY_OVERLAY_NEXT_PAGE_CTRL		(KeyCode.PAGE_DOWN, KeyEvent.CTRL_MASK),

	UNDO							(KeyCode.Z, KeyEvent.CTRL_MASK),
	REDO							(KeyCode.Y, KeyEvent.CTRL_MASK),
	CUT								(KeyCode.X, KeyEvent.CTRL_MASK),

	ZOOM_RESTORE					(KeyCode.SOFTKEY_0, KeyEvent.CTRL_MASK),
	ZOOM_TOGGLE						(KeyCode.SOFTKEY_9, KeyEvent.CTRL_MASK),
	ERASE_ALL						(KeyCode.ESCAPE),
	FULLSCREEN						(KeyCode.ENTER, KeyEvent.ALT_MASK),
	SHOW_GRID						(KeyCode.G, KeyEvent.CTRL_MASK),
	SHOW_GRID_LECTURER				(KeyCode.Q),
	TEXT							(KeyCode.T),
	PEN								(KeyCode.P),
	HIGHLIGHTER						(KeyCode.H),
	POINTER							(KeyCode.Z),
	ERASER							(KeyCode.E),
	EXTEND_VIEW						(KeyCode.F7),
	WHITEBOARD						(KeyCode.F8),

	SAVE_QUIZ						(KeyCode.ENTER, KeyEvent.CTRL_MASK),

	PAUSE_RECORDING					(KeyCode.PAUSE, KeyEvent.CTRL_MASK),
	PAUSE_RECORDING_P				(KeyCode.P, KeyEvent.CTRL_MASK),

	BOOKMARK_NEW					(KeyCode.B),
	BOOKMARK_GOTO					(KeyCode.G),
	BOOKMARK_SLIDE					(KeyCode.M),
	BOOKMARK_GOTO_LAST				(KeyCode.M, KeyEvent.SHIFT_MASK),

	TIMER_START						(KeyCode.S),
	TIMER_PAUSE						(KeyCode.PAUSE),
	TIMER_RESET						(KeyCode.T, KeyEvent.CTRL_MASK),

	COLOR_CUSTOM					(KeyCode.F1),
	COLOR_1							(KeyCode.F2),
	COLOR_2							(KeyCode.F3),
	COLOR_3							(KeyCode.F4),
	COLOR_4							(KeyCode.F5),
	COLOR_5							(KeyCode.F6);


	private final KeyEvent keyEvent;


	Shortcut(KeyCode code) {
		this.keyEvent = new KeyEvent(code.getCode());
	}

	Shortcut(KeyCode code, int modifiers) {
		this.keyEvent = new KeyEvent(code.getCode(), modifiers);
	}

	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

	public boolean match(KeyEvent event) {
		return keyEvent.equals(event);
	}
}
