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

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.action.DeleteShapeAction;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.RubberActionExt;

/**
 * PaintTool that deletes paintings near invocation positions.
 *
 * @author Alex Andres
 * @author Tobias
 */
public class RubberTool extends Tool {

	private Page page;


	public RubberTool(ToolContext context) {
		super(context);
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		this.page = page;
	}

	@Override
	public void execute(PenPoint2D point) {
		List<Shape> toDelete = new ArrayList<>();

		for (Shape shape : page.getShapes()) {
			if (shape.contains(point)) {
				recordAction(new RubberActionExt(shape.getHandle()));

				toDelete.add(shape);
			}
		}

		if (!toDelete.isEmpty()) {
			page.addAction(new DeleteShapeAction(page, toDelete));

			fireToolEvent(new ShapeModifyEvent(toDelete));
		}
	}

	@Override
	public void end(PenPoint2D point) {
		// Do nothing on purpose.
	}

	@Override
	public ToolType getType() {
		return ToolType.RUBBER;
	}

}
