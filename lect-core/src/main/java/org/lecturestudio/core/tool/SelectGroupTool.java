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
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.SelectShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.recording.action.SelectGroupAction;

public class SelectGroupTool extends Tool {

	private enum Mode {
		Select,
		Move
	}



	private Mode mode;

	private Page page;

	private SelectShape shape;

	private List<Shape> selectedShapes;

	private PenPoint2D sourcePoint;


	public SelectGroupTool(ToolContext context) {
		super(context);
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		recordAction(new SelectGroupAction());

		super.begin(point, page);

		this.page = page;
		this.sourcePoint = point.clone();

		shape = new SelectShape(createStroke());
		shape.setStartPoint(point.clone());

		page.addShape(shape);

		fireToolEvent(new ShapePaintEvent(ToolEventType.BEGIN, shape, shape.getBounds()));

		getSelectedShapes(page);

		if (hasSelectedShapes()) {
			if (hitSelected(point)) {
				mode = Mode.Move;
			}
			else {
				removeSelection();

				mode = Mode.Select;
			}
		}
		else {
			mode = Mode.Select;
		}
	}

	@Override
	public void execute(PenPoint2D point) {
		if (mode == Mode.Select) {
			Rectangle2D dirtyArea = shape.getBounds().clone();

			shape.setEndPoint(point);

			dirtyArea.union(shape.getBounds());

			fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape, dirtyArea));

			selectGroup(shape.getBounds());
		}
		else if (mode == Mode.Move) {
			sourcePoint.subtract(point);

			moveShapes(sourcePoint);

			sourcePoint.set(point);
		}

		super.execute(point);
	}

	@Override
	public void end(PenPoint2D point) {
		page.removeShape(shape);

		super.end(point);

		fireToolEvent(new ShapePaintEvent(ToolEventType.END, shape, shape.getBounds()));
	}

	@Override
	public ToolType getType() {
		return ToolType.SELECT_GROUP;
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
		for (Shape shape : selectedShapes) {
			shape.moveByDelta(delta);
		}

		if (!selectedShapes.isEmpty()) {
			fireToolEvent(new ShapeModifyEvent(selectedShapes));
		}
	}

	private boolean hasSelectedShapes() {
		return !selectedShapes.isEmpty();
	}

	private void addSelection(Shape shape) {
		shape.setSelected(true);

		selectedShapes.add(shape);
	}

	private void selectGroup(Rectangle2D rect) {
		removeSelection();

		for (Shape shape : page.getShapes()) {
			if (shape.intersects(rect)) {
				addSelection(shape);
			}
		}

		if (!selectedShapes.isEmpty()) {
			fireToolEvent(new ShapeModifyEvent(selectedShapes));
		}
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

	private Stroke createStroke() {
		StrokeSettings settings = context.getPaintSettings(ToolType.RECTANGLE);

		Stroke stroke = new Stroke();
		stroke.setColor(settings.getColor().derive(settings.getAlpha()));
		stroke.setWidth(settings.getWidth());

		return stroke;
	}
}
