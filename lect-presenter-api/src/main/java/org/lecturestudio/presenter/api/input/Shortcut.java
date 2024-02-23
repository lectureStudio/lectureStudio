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

	APP_CLOSE						(KeyCode.Q, KeyEvent.CTRL_MASK, "shortcut.app.close"),
	APP_SHORTCUTS					(KeyCode.H, KeyEvent.SHIFT_MASK, "shortcut.app.help"),

	CLOSE_VIEW						(KeyCode.ESCAPE, "shortcut.view.close"),

	DOC_OPEN						(KeyCode.O, KeyEvent.CTRL_MASK, "shortcut.doc.open"),
	DOC_CLOSE						(KeyCode.F4, KeyEvent.CTRL_MASK, "shortcut.doc.close"),

	SLIDE_FIRST						(KeyCode.HOME, "shortcut.slide.first"),
	SLIDE_LAST						(KeyCode.END, "shortcut.slide.last"),
	SLIDE_NEXT_RIGHT				(KeyCode.RIGHT, "shortcut.slide.next"),
	SLIDE_NEXT_DOWN					(KeyCode.DOWN, "shortcut.slide.next"),
	SLIDE_NEXT_PAGE_DOWN			(KeyCode.PAGE_DOWN, "shortcut.slide.next"),
	SLIDE_NEXT_SPACE				(KeyCode.SPACE, "shortcut.slide.next"),
	SLIDE_NEXT_10					(KeyCode.RIGHT, KeyEvent.SHIFT_MASK, "shortcut.slide.skip_next_10"),
	SLIDE_PREVIOUS_LEFT				(KeyCode.LEFT, "shortcut.slide.previous"),
	SLIDE_PREVIOUS_UP				(KeyCode.UP, "shortcut.slide.previous"),
	SLIDE_PREVIOUS_BACK_SPACE		(KeyCode.BACK_SPACE, "shortcut.slide.previous"),
	SLIDE_PREVIOUS_PAGE_UP			(KeyCode.PAGE_UP, "shortcut.slide.previous"),
	SLIDE_PREVIOUS_10				(KeyCode.LEFT, KeyEvent.SHIFT_MASK, "shortcut.slide.skip_previous_10"),
	SLIDE_NEW						(KeyCode.F9, "shortcut.slide.new"),
	SLIDE_DELETE					(KeyCode.D, KeyEvent.CTRL_MASK, "shortcut.slide.delete"),

	SLIDE_OVERLAY_START				(KeyCode.UP, KeyEvent.SHIFT_MASK, "shortcut.overlay.start"),
	SLIDE_OVERLAY_END				(KeyCode.DOWN, KeyEvent.SHIFT_MASK, "shortcut.overlay.end"),
	SLIDE_OVERLAY_PREVIOUS			(KeyCode.PAGE_UP, KeyEvent.SHIFT_MASK, "shortcut.overlay.previous"),
	SLIDE_OVERLAY_NEXT				(KeyCode.PAGE_DOWN, KeyEvent.SHIFT_MASK, "shortcut.overlay.next"),

	COPY_OVERLAY					(KeyCode.V, KeyEvent.CTRL_MASK, "shortcut.annotations.copy"),
	COPY_OVERLAY_NEXT_PAGE_CTRL		(KeyCode.PAGE_DOWN, KeyEvent.CTRL_MASK, "shortcut.annotations.copy.to.next"),

	UNDO							(KeyCode.Z, KeyEvent.CTRL_MASK, "shortcut.tool.undo"),
	REDO							(KeyCode.Y, KeyEvent.CTRL_MASK, "shortcut.tool.redo"),

	ZOOM_RESTORE					(KeyCode.DIGIT1, "shortcut.tool.zoom.restore"),
	ZOOM_TOGGLE						(KeyCode.DIGIT9, KeyEvent.CTRL_MASK, "shortcut.tool.zoom.toggle"),
	ZOOM_PAN						(KeyCode.F11, "shortcut.tool.zoom.pan"),
	ERASE_ALL						(KeyCode.C, "shortcut.tool.clear"),
	ERASE_ALL_ESC					(KeyCode.ESCAPE, "shortcut.tool.clear"),
	PEN								(KeyCode.P, "shortcut.tool.pen"),
	PEN_3							(KeyCode.DIGIT3, "shortcut.tool.pen"),
	HIGHLIGHTER						(KeyCode.H, "shortcut.tool.highlighter"),
	HIGHLIGHTER_5					(KeyCode.DIGIT5, "shortcut.tool.highlighter"),
	POINTER							(KeyCode.L, "shortcut.tool.pointer"),
	POINTER_2						(KeyCode.DIGIT2, "shortcut.tool.pointer"),
	TEXT							(KeyCode.T, "shortcut.tool.text"),
	TEXT_7							(KeyCode.DIGIT7, "shortcut.tool.text"),
	TEXT_HIGHLIGHTER				(KeyCode.H, KeyEvent.CTRL_MASK, "shortcut.tool.text.highlighter"),
	TEXT_HIGHLIGHTER_6				(KeyCode.DIGIT6, KeyEvent.CTRL_MASK, "shortcut.tool.text.highlighter"),
	LINE							(KeyCode.I, "shortcut.tool.line"),
	ARROW							(KeyCode.W, "shortcut.tool.arrow"),
	RECTANGLE						(KeyCode.R, "shortcut.tool.rectangle"),
	ELLIPSE							(KeyCode.O, "shortcut.tool.ellipse"),
	ERASER							(KeyCode.E, "shortcut.tool.eraser"),
	ERASER_4						(KeyCode.DIGIT4, "shortcut.tool.eraser"),
	EXTEND_VIEW						(KeyCode.F7, "shortcut.view.extend"),
	WHITEBOARD						(KeyCode.F8, "shortcut.whiteboard.open"),

	SAVE_QUIZ						(KeyCode.ENTER, KeyEvent.CTRL_MASK, "shortcut.quiz.save"),

	PAUSE_RECORDING					(KeyCode.PAUSE, KeyEvent.CTRL_MASK, "shortcut.recording.pause.toggle"),
	PAUSE_RECORDING_P				(KeyCode.P, KeyEvent.CTRL_MASK, "shortcut.recording.pause.toggle"),

	BOOKMARK_NEW					(KeyCode.B, "shortcut.bookmark.new"),
	BOOKMARK_GOTO					(KeyCode.G, "shortcut.bookmark.goto"),
	BOOKMARK_SLIDE					(KeyCode.M, "shortcut.bookmark.current"),
	BOOKMARK_GOTO_LAST				(KeyCode.M, KeyEvent.SHIFT_MASK, "shortcut.bookmark.goto.last"),

	FULLSCREEN						(KeyCode.ENTER, KeyEvent.ALT_MASK, "shortcut.fullscreen"),

	SHOW_GRID						(KeyCode.G, KeyEvent.CTRL_MASK, "shortcut.grid.show"),
	SHOW_GRID_LECTURER				(KeyCode.Q, "shortcut.grid.show.lecturer"),

	TIMER_START						(KeyCode.S, "shortcut.timer.start.toggle"),
	TIMER_PAUSE						(KeyCode.PAUSE, "shortcut.timer.pause"),
	TIMER_RESET						(KeyCode.T, KeyEvent.CTRL_MASK, "shortcut.timer.reset"),

	COLOR_CUSTOM					(KeyCode.F1, "shortcut.color.custom"),
	COLOR_1							(KeyCode.F2, "shortcut.color.one"),
	COLOR_2							(KeyCode.F3, "shortcut.color.two"),
	COLOR_3							(KeyCode.F4, "shortcut.color.three"),
	COLOR_4							(KeyCode.F5, "shortcut.color.four"),
	COLOR_5							(KeyCode.F6, "shortcut.color.five");


	private final KeyEvent keyEvent;

	private final String description;


	Shortcut(KeyCode code, String description) {
		this.keyEvent = new KeyEvent(code.getCode());
		this.description = description;
	}

	Shortcut(KeyCode code, int modifiers, String description) {
		this.keyEvent = new KeyEvent(code.getCode(), modifiers);
		this.description = description;
	}

	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

	public String getDescription() {
		return description;
	}

	public boolean matches(KeyEvent event) {
		return keyEvent.equals(event);
	}
}
