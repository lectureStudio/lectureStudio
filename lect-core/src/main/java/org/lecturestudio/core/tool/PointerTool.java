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
import org.lecturestudio.core.model.shape.PointerShape;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.PointerAction;

public class PointerTool extends StrokeTool<PointerShape> {

	private Page page;


	public PointerTool(ToolContext context) {
		super(context);
	}

	@Override
	public ToolType getType() {
		return ToolType.POINTER;
	}

	@Override
	protected void beginInternal(PenPoint2D point, Page page) {
		this.page = page;

		shape.setPoint(point);

		page.addShape(shape);
	}

	@Override
	protected void executeInternal(PenPoint2D point) {
		shape.setPoint(point);
	}

	@Override
	protected void endInternal(PenPoint2D point) {
		page.removeShape(shape);
	}

	@Override
	protected PlaybackAction createPlaybackAction() {
		Stroke actionStroke = createStroke();
		actionStroke.scale(context.getPageScale());

		return new PointerAction(actionStroke, context.getKeyEvent());
	}

	@Override
	protected PointerShape createShape() {
		Stroke stroke = createStroke();
		stroke.scale(3);
		stroke.scale(context.getPageScale());

		return new PointerShape(stroke);
	}
}