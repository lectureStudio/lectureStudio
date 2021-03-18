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
import org.lecturestudio.core.model.action.CreateShapeAction;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.CloneAction;

public class CloneTool extends Tool {

	private List<Shape> selectedShapes;

	private PenPoint2D sourcePoint;


	public CloneTool(ToolContext context) {
		super(context);
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		recordAction(new CloneAction());

		super.begin(point, page);

		sourcePoint = point.clone();

		getSelectedShapes(page);

		if (hasSelectedShapes()) {
			if (hitSelected(point)) {
				List<Shape> cloned = new ArrayList<>();

				for (Shape shape : selectedShapes) {
					Shape clonedShape = shape.clone();

					cloned.add(clonedShape);
				}

				page.addAction(new CreateShapeAction(page, new ArrayList<>(cloned)));
			}
			else {
				removeSelection();
			}
		}
		else {
			Shape selectedShape = getTopLevelShape(point, page);

			if (selectedShape != null) {
				Shape clonedShape = selectedShape.clone();

				CreateShapeAction pageAction = new CreateShapeAction(page, clonedShape);
				page.addAction(pageAction);

				addSelection(clonedShape);
			}
		}
	}

	@Override
	public void execute(PenPoint2D point) {
		sourcePoint.subtract(point);

		moveShapes(sourcePoint);

		sourcePoint.set(point);

		super.execute(point);
	}

	@Override
	public ToolType getType() {
		return ToolType.CLONE;
	}

	private Shape getTopLevelShape(PenPoint2D point, Page page) {
		Shape shape = null;

		for (Shape s : page.getShapes()) {
			if (s.contains(point)) {
				shape = s;
			}
		}
		return shape;
	}

	private void getSelectedShapes(Page page) {
		selectedShapes = new ArrayList<>();

		for (Shape shape : page.getShapes()) {
			if (shape.isSelected()) {
				selectedShapes.add(shape);
			}
		}
	}

	private void moveShapes(PenPoint2D delta) {
		if (!hasSelectedShapes()) {
			return;
		}

		for (Shape shape : selectedShapes) {
			shape.moveByDelta(delta);
		}
	}

	private boolean hasSelectedShapes() {
		return !selectedShapes.isEmpty();
	}

	private void addSelection(Shape shape) {
		shape.setSelected(true);

		selectedShapes.add(shape);
	}

	private void removeSelection() {
		for (Shape shape : selectedShapes) {
			shape.setSelected(false);
		}

		selectedShapes.clear();
	}

	private boolean hitSelected(PenPoint2D point) {
		for (Shape shape : selectedShapes) {
			if (shape.contains(point)) {
				return true;
			}
		}

		return false;
	}

}
