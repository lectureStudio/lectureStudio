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

public enum ActionType {

	/*
	 * Tool actions
	 */
	TOOL_BEGIN,
	TOOL_EXECUTE,
	TOOL_END,

	/*
	 * Stroke actions
	 */
	PEN, HIGHLIGHTER, POINTER,

	/*
	 * Form actions
	 */
	ARROW, LINE, RECTANGLE, ELLIPSE,

	/*
	 * Text actions
	 */
	TEXT, TEXT_CHANGE, TEXT_FONT_CHANGE, TEXT_LOCATION_CHANGE, TEXT_REMOVE, TEXT_SELECTION,

	LATEX, LATEX_FONT_CHANGE,

	/*
	 * Rearrangement actions
	 */
	UNDO, REDO, CLONE, SELECT, SELECT_GROUP, RUBBER, DELETE_ALL,

	/*
	 * Zoom actions
	 */
	PANNING, EXTEND_VIEW, ZOOM, ZOOM_OUT,

	/*
	 * Atomic actions
	 */
	NEXT_PAGE, KEY,

	STATIC,

	/*
	 * Network related actions
	 */
	STREAM_START,

	PAGE, PAGE_CREATED, PAGE_REMOVED,

	DOCUMENT_CREATED, DOCUMENT_CLOSED, DOCUMENT_SELECTED;

}
