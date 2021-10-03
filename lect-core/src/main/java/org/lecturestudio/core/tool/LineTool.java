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
import org.lecturestudio.core.model.shape.FormShape;
import org.lecturestudio.core.model.shape.LineShape;
import org.lecturestudio.core.recording.action.LineAction;
import org.lecturestudio.core.recording.action.PlaybackAction;

/**
 * PaintTool for creating a line on a Page.
 *
 * @author Alex Andres
 */
public class LineTool extends FormTool {

	public LineTool(ToolContext context) {
		super(context);
	}

	public LineTool(ToolContext context, Integer shapeHandle) {
		super(context, shapeHandle);
	}

	@Override
	public ToolType getType() {
		return ToolType.LINE;
	}

	@Override
	public boolean supportsKeyEvent(KeyEvent event) {
		if (isNull(event)) {
			return false;
		}

		return event.isAltDown();
	}

	@Override
	protected FormShape createShape() {
		return new LineShape(createStroke());
	}

	@Override
	protected PlaybackAction createPlaybackAction() {
		return new LineAction(shape.getHandle(), createStroke(),
				context.getKeyEvent());
	}
}
