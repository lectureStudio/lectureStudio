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
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.PanningAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * Tool that translates the shown area of the page.
 *
 * @author Alex Andres
 * @author Tobias
 */
public class PanningTool extends Tool {

	private Point2D start;

	private Point2D delta;

	private Page page;


	public PanningTool(ToolContext context) {
		super(context);
	}

	@Override
	public void begin(PenPoint2D point, Page page) {
		recordAction(new PanningAction());

		super.begin(point, page);

		this.page = page;

		start = point;
		delta = null;
	}

	@Override
	public void execute(PenPoint2D point) {
		delta = new Point2D(start.getX() - point.getX(), start.getY() - point.getY());

		// Only temporary while in action.
		translate(false);

		super.execute(point);
	}

	@Override
	public void end(PenPoint2D point) {
		// Make the translation permanent.
		translate(true);

		super.end(point);
	}

	@Override
	public ToolType getType() {
		return ToolType.PANNING;
	}

	/**
	 * Translate the current shown area on the User and Presentation view on the
	 * given Page by delta. This can be temporary or permanent (so that the next
	 * translation would be relative to this translation instead of current
	 * shown area).
	 *
	 * @param permanent True, to apply permanent translation.
	 */
	private void translate(boolean permanent) {
		// Do translation only on Presentation- and User-view.
		setParameter(ViewType.User, permanent);
		setParameter(ViewType.Presentation, permanent);
	}

	private void setParameter(ViewType viewType, boolean permanent) {
		PresentationParameterProvider ppp = context.getPresentationParameterProvider(viewType);
		PresentationParameter param = ppp.getParameter(page);

		if (!permanent) { // Temporary translation.
			param.setTranslation(true);
			param.setTranslation(delta);
		}
		else { // Permanent translation -> adjust pageRect.
			Rectangle2D rect = param.getPageRect();
			rect = new Rectangle2D(rect.getX() + delta.getX(),
					rect.getY() + delta.getY(), rect.getWidth(),
					rect.getHeight());

			param.setTranslation(false);
			param.setPageRect(rect);
		}
	}
}
