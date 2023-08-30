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

import static java.util.Objects.nonNull;

import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.SelectAction;

public class SelectTool extends Tool {

	protected Page page;

	protected Shape selectedShape;

	protected PenPoint2D sourcePoint;


	public SelectTool(ToolContext context) {
		super(context);
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		recordAction(new SelectAction());

		super.begin(point, page);

		this.page = page;
		this.sourcePoint = point.clone();

		selectedShape = getTopLevelShape(point, page);

		if (nonNull(selectedShape)) {
			removeSelection();

			selectedShape.setSelected(true);

			fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
		}
		else {
			if (removeSelection()) {
				fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
			}
		}
	}

	@Override
	public void execute(PenPoint2D point) {
		if (nonNull(selectedShape)) {
			sourcePoint.subtract(point);

			selectedShape.moveByDelta(sourcePoint);

			sourcePoint.set(point);

			super.execute(point);

			fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
		}
	}

	@Override
	public void end(PenPoint2D point) {
		if (nonNull(selectedShape)) {
			selectedShape.setSelected(false);

			super.end(point);

			fireToolEvent(new ShapeModifyEvent(List.of(selectedShape)));
		}
	}

	@Override
	public ToolType getType() {
		return ToolType.SELECT;
	}

	protected Shape getTopLevelShape(PenPoint2D point, Page page) {
		Shape shape = null;

		for (Shape s : page.getShapes()) {
			if (s.contains(point)) {
				shape = s;
			}
		}

		return shape;
	}

	protected boolean removeSelection() {
		boolean removed = false;

		for (Shape shape : page.getShapes()) {
			if (shape.isSelected()) {
				shape.setSelected(false);

				removed = true;
			}
		}

		return removed;
	}

}
