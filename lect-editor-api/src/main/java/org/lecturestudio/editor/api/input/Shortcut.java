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

package org.lecturestudio.editor.api.input;

import org.lecturestudio.core.input.KeyCode;
import org.lecturestudio.core.input.KeyEvent;

public enum Shortcut {

	APP_CLOSE(KeyCode.Q, KeyEvent.CTRL_MASK),

	CLOSE_VIEW(KeyCode.ESCAPE),

	DOC_OPEN(KeyCode.O, KeyEvent.CTRL_MASK),
	DOC_CLOSE(KeyCode.F4, KeyEvent.CTRL_MASK),

	SLIDE_NEXT_DOWN(KeyCode.DOWN),
	SLIDE_NEXT_PAGE_DOWN(KeyCode.PAGE_DOWN),
	SLIDE_PREVIOUS_UP(KeyCode.UP),
	SLIDE_PREVIOUS_PAGE_UP(KeyCode.PAGE_UP),
	SLIDE_NEW(KeyCode.F9),
	SLIDE_DELETE(KeyCode.D, KeyEvent.CTRL_MASK),

	UNDO(KeyCode.Z, KeyEvent.CTRL_MASK),
	REDO(KeyCode.Y, KeyEvent.CTRL_MASK),
	CUT(KeyCode.X, KeyEvent.CTRL_MASK),


	SLIDE_MOVE_RIGHT(KeyCode.RIGHT),
	SLIDE_MOVE_LEFT(KeyCode.LEFT),

	PLAYBACK_PAUSE_PLAY_SPACE(KeyCode.SPACE),
	PLAYBACK_BEGINNING_POS_1(KeyCode.HOME),
	PLAYBACK_END_END(KeyCode.END);


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
