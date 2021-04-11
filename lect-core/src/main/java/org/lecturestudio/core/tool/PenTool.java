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

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.action.CreateShapeAction;
import org.lecturestudio.core.model.shape.StrokeShape;
import org.lecturestudio.core.recording.action.PenAction;
import org.lecturestudio.core.recording.action.PlaybackAction;

/**
 * PaintTool that draws a Line with a given Stroke on the current Page.
 *
 * @author Alex Andres
 */
public class PenTool extends StrokeTool<StrokeShape> {

	public PenTool(ToolContext context) {
		super(context);
	}

	@Override
	public ToolType getType() {
		return ToolType.PEN;
	}

	@Override
	protected void beginInternal(PenPoint2D point, Page page) {
		shape.addPoint(point.clone());

		page.addAction(new CreateShapeAction(page, shape));
	}

	@Override
	protected void executeInternal(PenPoint2D point) {
		shape.addPoint(point.clone());
	}

	@Override
	protected PlaybackAction createPlaybackAction() {
		return new PenAction(createStroke(), context.getKeyEvent());
	}

	@Override
	protected StrokeShape createShape() {
		Stroke shapeStroke = createStroke();
		shapeStroke.scale(context.getPageScale());

		return new StrokeShape(shapeStroke);
	}
}
