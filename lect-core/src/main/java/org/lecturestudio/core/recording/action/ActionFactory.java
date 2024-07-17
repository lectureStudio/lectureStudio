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

import java.io.IOException;

public abstract class ActionFactory {

	public static PlaybackAction createAction(int actionType, int timestamp, byte[] input) throws IOException {
		PlaybackAction action = getAction(actionType, input);
		action.setTimestamp(timestamp);

		return action;
	}

	private static PlaybackAction getAction(int actionType, byte[] input) throws IOException {
		ActionType type = ActionType.values()[actionType];

		switch (type) {
			case DELETE_ALL:
				return new DeleteAllAction();
			case UNDO:
				return new UndoAction();
			case REDO:
				return new RedoAction();
			case KEY:
				return new KeyAction(input);
			case PEN:
				return new PenAction(input);
			case HIGHLIGHTER:
				return new HighlighterAction(input);
			case POINTER:
				return new PointerAction(input);
			case ARROW:
				return new ArrowAction(input);
			case LINE:
				return new LineAction(input);
			case RECTANGLE:
				return new RectangleAction(input);
			case ELLIPSE:
				return new EllipseAction(input);
			case CLONE:
				return new CloneAction(input);
			case SELECT:
				return new SelectAction(input);
			case SELECT_GROUP:
				return new SelectGroupAction(input);
			case LATEX:
				return new LatexAction(input);
			case LATEX_FONT_CHANGE:
				return new LatexFontChangeAction(input);
			case TEXT:
				return new TextAction(input);
			case TEXT_CHANGE:
				return new TextChangeAction(input);
			case TEXT_FONT_CHANGE:
				return new TextFontChangeAction(input);
			case TEXT_LOCATION_CHANGE:
				return new TextLocationChangeAction(input);
			case TEXT_REMOVE:
				return new TextRemoveAction(input);
			case TEXT_SELECTION:
				return new TextSelectionAction(input);
			case TEXT_SELECTION_EXT:
				return new TextSelectionExtAction(input);
			case TOOL_BEGIN:
				return new ToolBeginAction(input);
			case TOOL_EXECUTE:
				return new ToolExecuteAction(input);
			case TOOL_END:
				return new ToolEndAction(input);
			case PANNING:
				return new PanningAction(input);
			case EXTEND_VIEW:
				return new ExtendViewAction(input);
			case ZOOM:
				return new ZoomAction(input);
			case ZOOM_OUT:
				return new ZoomOutAction();
			case RUBBER:
				return new RubberAction(input);
			case RUBBER_EXT:
				return new RubberActionExt(input);
			case NEXT_PAGE:
				return new NextPageAction();
			case PAGE:
				return new PageAction(input);
			case SCREEN:
				return new ScreenAction(input);
			default:
				throw new IOException("Action not defined: " + type);
		}
	}

}
