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

package org.lecturestudio.core.tool;

import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.UndoAction;

public class UndoTool extends SimpleTool {

	public UndoTool(ToolContext context) {
		super(context);
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		recordAction(new UndoAction());

		List<Shape> shapes = page.getUndoActions().peek().getShapes();

		page.undo();

		fireToolEvent(new ShapeModifyEvent(shapes));
	}

	@Override
	public ToolType getType() {
		return ToolType.UNDO;
	}

}
