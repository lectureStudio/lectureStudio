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

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.shape.EllipseShape;
import org.lecturestudio.core.model.shape.FormShape;
import org.lecturestudio.core.recording.action.EllipseAction;
import org.lecturestudio.core.recording.action.PlaybackAction;

/**
 * PaintTool for creating a line on a Page.
 *
 * @author Alex Andres
 */
public class EllipseTool extends FormTool {

	public EllipseTool(ToolContext context) {
		super(context);
	}

	public EllipseTool(ToolContext context, Integer shapeHandle) {
		super(context, shapeHandle);
	}

	@Override
	public ToolType getType() {
		return ToolType.ELLIPSE;
	}

	@Override
	public boolean supportsKeyEvent(KeyEvent event) {
		if (isNull(event)) {
			return false;
		}

		boolean fill = event.isAltDown();
		boolean keepRatio = event.isShiftDown();

		return fill || keepRatio;
	}

	@Override
	protected FormShape createShape() {
		return new EllipseShape(createStroke());
	}

	@Override
	protected PlaybackAction createPlaybackAction() {
		return new EllipseAction(shape.getHandle(), createStroke(),
				context.getKeyEvent());
	}
}
