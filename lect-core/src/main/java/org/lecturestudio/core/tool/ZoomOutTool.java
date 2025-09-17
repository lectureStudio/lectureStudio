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
import org.lecturestudio.core.recording.action.ZoomOutAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * A tool that allows the user to zoom out and reset the view to its default size.
 * This tool handles the reset operation for different view types when activated.
 *
 * @author Alex Andres
 */
public class ZoomOutTool extends SimpleTool {

	/**
	 * Constructs a new ZoomOutTool with the specified tool context.
	 *
	 * @param context The tool context providing access to the application resources.
	 */
	public ZoomOutTool(ToolContext context) {
		super(context);
	}

	/**
	 * Begins the zoom-out operation at the specified point on the given page.
	 * Resets the view for the User view type, records the action, then resets
	 * the Presentation and Preview view types.
	 *
	 * @param point The point where the tool action began.
	 * @param page  The page on which the tool action is performed.
	 */
	@Override
	public void begin(PenPoint2D point, Page page) {
		resetView(ViewType.User, page);

		recordAction(new ZoomOutAction());

		resetView(ViewType.Presentation, page);
		resetView(ViewType.Preview, page);
	}

	/**
	 * Returns the type of this tool.
	 *
	 * @return The tool-type ZOOM_OUT.
	 */
	@Override
	public ToolType getType() {
		return ToolType.ZOOM_OUT;
	}

	/**
	 * Resets the view for the specified view type and page.
	 * Retrieves the presentation parameters for the given view type and page,
	 * then resets the page rectangle to its default state.
	 *
	 * @param viewType The type of view to reset.
	 * @param page     The page for which to reset the view.
	 */
	private void resetView(ViewType viewType, Page page) {
		PresentationParameterProvider ppp = context.getPresentationParameterProvider(viewType);
		PresentationParameter para = ppp.getParameter(page);
		para.resetPageRect();
	}
}
