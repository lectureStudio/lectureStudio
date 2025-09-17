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
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.ExtendViewAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

import static java.util.Objects.nonNull;

/**
 * Toggles the Extended-Mode on the current page on all views.
 *
 * @author Alex Andres
 */
public class ExtendViewTool extends SimpleTool {

	/**
	 * The rectangle defining the boundaries for the extended view area.
	 */
	private final Rectangle2D rect;


	/**
	 * Constructs a new ExtendViewTool instance.
	 *
	 * @param context The tool context providing access to presentation parameters
	 *                and other required resources.
	 */
	public ExtendViewTool(ToolContext context) {
		this(context, null);
	}

	/**
	 * Constructs a new ExtendViewTool instance.
	 *
	 * @param context The tool context providing access to presentation parameters
	 *                and other required resources.
	 * @param rect    The rectangle defining the boundaries for the extended view area.
	 */
	public ExtendViewTool(ToolContext context, Rectangle2D rect) {
		super(context);

		this.rect = rect;
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		PresentationParameterProvider ppp = context.getPresentationParameterProvider(ViewType.User);
		PresentationParameter para = ppp.getParameter(page);

		extendView(ViewType.User, page);

		recordAction(new ExtendViewAction(para.getPageRect().clone()));

		extendView(ViewType.Presentation, page);
		extendView(ViewType.Preview, page);
	}

	@Override
	public ToolType getType() {
		return ToolType.EXTEND_VIEW;
	}

	private void extendView(ViewType viewType, Page page) {
		PresentationParameterProvider ppp = context.getPresentationParameterProvider(viewType);
		PresentationParameter para = ppp.getParameter(page);

		if (nonNull(rect)) {
			para.setExtendedMode(!para.isExtended(), rect);
		}
		else {
			para.setExtendedMode(!para.isExtended());
		}
	}
}
