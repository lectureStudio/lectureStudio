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

package org.lecturestudio.swing;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 * Enumeration of all shortcuts used in the application. Each shortcut is
 * represented by a description text and an int value.
 * 
 * @author Alex
 * 
 */
public enum Shortcut {

	DOC_CLOSE(KeyStroke.getKeyStroke("ctrl F4")),
	DOC_OPEN(KeyStroke.getKeyStroke("ctrl O")),
	APP_CLOSE(KeyStroke.getKeyStroke("ctrl Q")),
	NEXT_SLIDE(KeyStroke.getKeyStroke("RIGHT")),
	NEXT_SLIDE_DOWN(KeyStroke.getKeyStroke("DOWN")),
	NEXT_SLIDE_PAGE_DOWN(KeyStroke.getKeyStroke("PAGE_DOWN")),
	NEXT_SLIDE_SPACE(KeyStroke.getKeyStroke("SPACE")),
	PREVIOUS_SLIDE(KeyStroke.getKeyStroke("LEFT")),
	PREVIOUS_SLIDE_UP(KeyStroke.getKeyStroke("UP")),
	PREVIOUS_SLIDE_PAGE_UP(KeyStroke.getKeyStroke("PAGE_UP")),
	COPY_OVERLAY(KeyStroke.getKeyStroke("ctrl V")),
	COPY_OVERLAY_NEXT_PAGE_CTRL(KeyStroke.getKeyStroke("ctrl PAGE_DOWN")),
	COPY_OVERLAY_NEXT_PAGE_SHIFT(KeyStroke.getKeyStroke("shift PAGE_DOWN")),
	UNDO(KeyStroke.getKeyStroke("ctrl Z")),
	REDO(KeyStroke.getKeyStroke("ctrl Y")),
	CUT(KeyStroke.getKeyStroke("ctrl X")),
	ZOOM_RESTORE(KeyStroke.getKeyStroke("ctrl 0")),
	ERASE_ALL(KeyStroke.getKeyStroke("ESCAPE")),
	FULLSCREEN(KeyStroke.getKeyStroke("alt ENTER")),
	DELETE_SLIDE(KeyStroke.getKeyStroke("ctrl D")),
	SHOW_GRID(KeyStroke.getKeyStroke("ctrl G")),
    SHOW_GRID_LECTURER(KeyStroke.getKeyStroke("Q")),
	TEXT(KeyStroke.getKeyStroke("T")),
	PEN(KeyStroke.getKeyStroke("P")),
	HIGHLIGHTER(KeyStroke.getKeyStroke("H")),
	POINTER(KeyStroke.getKeyStroke("Z")),
	ERASER(KeyStroke.getKeyStroke("E")),
	COLOR_CUSTOM(KeyStroke.getKeyStroke("F1")),
	COLOR_1(KeyStroke.getKeyStroke("F2")),
	COLOR_2(KeyStroke.getKeyStroke("F3")),
	COLOR_3(KeyStroke.getKeyStroke("F4")),
	COLOR_4(KeyStroke.getKeyStroke("F5")),
	COLOR_5(KeyStroke.getKeyStroke("F6")),
	EXTEND_VIEW(KeyStroke.getKeyStroke("F7")),
	WHITEBOARD(KeyStroke.getKeyStroke("F8")),
	NEW_SLIDE(KeyStroke.getKeyStroke("F9")),
	DRAG_SLIDE(KeyStroke.getKeyStroke("F11")),
	TOGGLE_ZOOM(KeyStroke.getKeyStroke("ctrl 9")),
	SAVE_QUIZ(KeyStroke.getKeyStroke("ctrl ENTER")),
    NEW_BOOKMARK(KeyStroke.getKeyStroke("B")),
    GOTO_BOOKMARK(KeyStroke.getKeyStroke("G")),
	PAUSE_RECORDING(KeyStroke.getKeyStroke("PAUSE")),
	PAUSE_RECORDING_P(KeyStroke.getKeyStroke("ctrl P")),
	;

	private final KeyStroke keyStroke;


	/**
	 * Shortcut constructor which creates a shortcut with a description text and
	 * an accelerator.
	 * 
	 * @param keyStroke
	 */
	Shortcut(KeyStroke keyStroke) {
		this.keyStroke = keyStroke;
	}

	/**
	 * Returns the text of the shortcut.
	 * 
	 * @return the text of the shortcut
	 */
	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

	public boolean matches(KeyEvent e) {
		return (keyStroke == null) ? false : keyStroke.equals(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiersEx()));
	}

}
