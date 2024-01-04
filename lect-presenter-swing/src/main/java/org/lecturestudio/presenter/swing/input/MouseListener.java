/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.swing.input;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.swing.components.SlideView;

public class MouseListener extends MouseAdapter {

	private final ToolController toolController;

	private final PenPoint2D point;

	private final Rectangle2D viewBounds;

	private PresentationParameter parameter;

	private boolean inExecute;

	private boolean inRubberMode;


	public MouseListener(SlideView slideView, ToolController toolController) {
		this.toolController = toolController;
		this.viewBounds = new Rectangle2D();
		this.point = new PenPoint2D();

		slideView.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle canvasBounds = slideView.getCanvasBounds();

				viewBounds.setLocation(canvasBounds.getMinX(), canvasBounds.getMinY());
				viewBounds.setSize(canvasBounds.getWidth(), canvasBounds.getHeight());
			}
		});
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (inExecute) {
			return;
		}

		inExecute = true;

		getPoint(e);

		if (SwingUtilities.isRightMouseButton(e)) {
			toolController.selectRubberTool();
			inRubberMode = true;
		}

		toolController.beginToolAction(getPageLoc(point, viewBounds));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		inExecute = false;

		getPoint(e);

		toolController.endToolAction(getPageLoc(point, viewBounds));

		if (inRubberMode && SwingUtilities.isRightMouseButton(e)) {
			toolController.selectPreviousTool();
			inRubberMode = false;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!inExecute) {
			return;
		}

		getPoint(e);

		toolController.executeToolAction(getPageLoc(point, viewBounds));
	}

	public void setPresentationParameter(PresentationParameter parameter) {
		this.parameter = parameter;
	}

	private void getPoint(MouseEvent event) {
		point.set(event.getX(), event.getY());
		point.setPressure(1.0);
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
