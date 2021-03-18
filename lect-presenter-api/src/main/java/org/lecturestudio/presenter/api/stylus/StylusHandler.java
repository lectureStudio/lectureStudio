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

package org.lecturestudio.presenter.api.stylus;

import org.lecturestudio.stylus.StylusAxesData;
import org.lecturestudio.stylus.StylusButton;
import org.lecturestudio.stylus.StylusCursor;
import org.lecturestudio.stylus.StylusEvent;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.PresentationParameter;

public class StylusHandler {

	private final ToolController toolController;

	private final PenPoint2D point;

	private PresentationParameter parameter;

	private boolean inExecute;

	private boolean inRubberMode;


	public StylusHandler(ToolController toolController) {
		this.toolController = toolController;
		this.point = new PenPoint2D();
	}

	public void setPresentationParameter(PresentationParameter parameter) {
		this.parameter = parameter;
	}

	public void onCursorChange(StylusEvent stylusEvent, Rectangle2D viewBounds) {
		// Nothing to do, for now.
	}

	public void onCursorMove(StylusEvent stylusEvent, Rectangle2D viewBounds) {
		if (!inExecute) {
			return;
		}

		getPoint(stylusEvent);

		toolController.executeToolAction(getPageLoc(point, viewBounds));
	}

	public void onButtonDown(StylusEvent stylusEvent, Rectangle2D viewBounds) {
		if (inExecute) {
			return;
		}

		inExecute = true;

		getPoint(stylusEvent);

		if (stylusEvent.getButton() == StylusButton.RIGHT || stylusEvent.getCursor() == StylusCursor.ERASER) {
			toolController.selectRubberTool();
			inRubberMode = true;
		}

		toolController.beginToolAction(getPageLoc(point, viewBounds));
	}

	public void onButtonUp(StylusEvent stylusEvent, Rectangle2D viewBounds) {
		inExecute = false;

		getPoint(stylusEvent);

		toolController.endToolAction(getPageLoc(point, viewBounds));

		if (inRubberMode && (stylusEvent.getButton() == StylusButton.RIGHT || stylusEvent.getCursor() == StylusCursor.ERASER)) {
			toolController.selectPreviousTool();
			inRubberMode = false;
		}
	}

	private void getPoint(StylusEvent stylusEvent) {
		StylusAxesData axesData = stylusEvent.getAxesData();

		point.set(axesData.getX(), axesData.getY());

		if (stylusEvent.getCursor() == StylusCursor.MOUSE) {
			point.setPressure(1.0);
		}
		else {
			point.setPressure(axesData.getPressure());
		}
	}

	private PenPoint2D getPageLoc(PenPoint2D point, Rectangle2D viewBounds) {
		Rectangle2D pageRect = parameter.getPageRect();

		double x_rel = point.getX() / viewBounds.getWidth() * pageRect.getWidth();
		double y_rel = point.getY() / viewBounds.getWidth() * pageRect.getWidth();

		double x = pageRect.getX() + x_rel;
		double y = pageRect.getY() + y_rel;

		return new PenPoint2D(x, y, point.getPressure());
	}

}
