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

package org.lecturestudio.core.recording.action;

import static java.util.Objects.nonNull;

import java.io.IOException;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.core.tool.StrokeSettings;
import org.lecturestudio.core.tool.ToolType;

public class ZoomAction extends BaseStrokeAction {

	public ZoomAction(Stroke stroke, KeyEvent keyEvent) {
		super(0, stroke, keyEvent);
	}

	public ZoomAction(byte[] input) throws IOException {
		super(input);
	}

	@Override
	public void execute(ToolController controller) throws Exception {
		StrokeSettings settings = controller.getPaintSettings(ToolType.RECTANGLE);

		if (nonNull(stroke)) {
			settings.setWidth(stroke.getWidth());
		}

		controller.selectZoomTool();
		controller.setKeyEvent(getKeyEvent());
	}

	@Override
	public ActionType getType() {
		return ActionType.ZOOM;
	}

	@Override
	public boolean hasHandle() {
		return false;
	}
}
