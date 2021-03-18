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

import static java.util.Objects.isNull;

import java.util.List;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.action.CreateShapeAction;
import org.lecturestudio.core.model.shape.TextSelectionShape;
import org.lecturestudio.core.recording.action.TextSelectionAction;

public class TextSelectionTool extends Tool {

	private List<Rectangle2D> textBoxes;

	private Page page;

	private PenPoint2D startPoint;

	private TextSelectionAction action;

	private TextSelectionShape shape;


	public TextSelectionTool(ToolContext context) {
		super(context);
	}

	public TextSelectionTool(ToolContext context, List<Rectangle2D> textBoxes) {
		super(context);

		this.textBoxes = textBoxes;
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		this.startPoint = point;
		this.page = page;

		if (isNull(textBoxes)) {
			textBoxes = page.getTextPositions();
		}

		shape = createShape();

		page.addShape(shape);

		addSelection(point);
	}

	@Override
	public void execute(PenPoint2D point) {
		if (isNull(textBoxes)) {
			return;
		}

		addSelection(point);
	}

	@Override
	public void end(PenPoint2D point) {
		super.end(point);

		if (!shape.hasSelection()) {
			page.removeShape(shape);
		}
		else {
			fireToolEvent(new ShapePaintEvent(ToolEventType.END, shape, shape.getBounds()));
		}

		textBoxes = null;
		action = null;
	}

	@Override
	public ToolType getType() {
		return ToolType.TEXT_SELECTION;
	}

	private TextSelectionAction createPlaybackAction() {
		TextSelectionSettings settings = context.getPaintSettings(getType());

		return new TextSelectionAction(settings.getColor().derive(settings.getAlpha()));
	}

	private TextSelectionShape createShape() {
		TextSelectionSettings settings = context.getPaintSettings(getType());

		return new TextSelectionShape(settings.getColor().derive(settings.getAlpha()));
	}

	private void addSelection(PenPoint2D point) {
		// Execute only towards x-direction.
		PenPoint2D xPoint = point.clone();
		xPoint.set(point.getX(), startPoint.getY());

		for (Rectangle2D rect : textBoxes) {
			if (rect.contains(xPoint)) {
				Rectangle2D dirtyArea = shape.getBounds().clone();

				// Create an action only if there is at least one character selection.
				if (!shape.hasSelection()) {
					page.addAction(new CreateShapeAction(page, shape));
					dirtyArea = rect.clone();
				}

				shape.addPoint(point.clone());
				shape.addSelection(rect);

				dirtyArea.union(shape.getBounds());

				if (isNull(action)) {
					action = createPlaybackAction();

					recordAction(action);

					super.begin(point, page);

					fireToolEvent(new ShapePaintEvent(ToolEventType.BEGIN, shape, shape.getBounds()));
				}

				if (action.addSelection(rect)) {
					super.execute(point);

					fireToolEvent(new ShapePaintEvent(ToolEventType.EXECUTE, shape, dirtyArea));
				}
				break;
			}
		}
	}
}
