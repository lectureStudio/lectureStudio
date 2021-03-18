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

package org.lecturestudio.presenter.javafx.input;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.stylus.StylusButton;
import org.lecturestudio.stylus.StylusCursor;
import org.lecturestudio.stylus.StylusEvent;

public class MouseListener implements EventHandler<MouseEvent> {

	private final StylusHandler handler;

	private final StylusCursor stylusCursor;

	private final Rectangle2D viewBounds;

	
	public MouseListener(StylusHandler handler, SlideView slideView) {
		this.handler = handler;
		this.stylusCursor = StylusCursor.MOUSE;
		this.viewBounds = new Rectangle2D();

		slideView.canvasBoundsProperty().addListener(observable -> {
			Bounds canvasBounds = slideView.getCanvasBounds();

			viewBounds.setLocation(canvasBounds.getMinX(), canvasBounds.getMinY());
			viewBounds.setSize(canvasBounds.getWidth(), canvasBounds.getHeight());
		});
	}
	
	@Override
	public void handle(MouseEvent event) {
		EventType<? extends MouseEvent> type = event.getEventType();
		
		if (type == MouseEvent.MOUSE_PRESSED) {
			onMousePressed(event);
		}
		else if (type == MouseEvent.MOUSE_RELEASED) {
			onMouseReleased(event);
		}
		else if (type == MouseEvent.MOUSE_MOVED) {
			onMouseMoved(event);
		}
		else if (type == MouseEvent.MOUSE_DRAGGED) {
			onMouseMoved(event);
		}

		event.consume();
	}
	
	private void onMousePressed(MouseEvent event) {
		StylusButton stylusButton = convertButton(event);
		double[] axes = new double[2];
		axes[0] = event.getX();
		axes[1] = event.getY();

		StylusEvent stylusEvent = new StylusEvent(stylusButton, stylusCursor, axes);

		handler.onButtonDown(stylusEvent, viewBounds);
	}

	private void onMouseReleased(MouseEvent event) {
		StylusButton stylusButton = convertButton(event);
		double[] axes = new double[2];
		axes[0] = event.getX();
		axes[1] = event.getY();

		StylusEvent stylusEvent = new StylusEvent(stylusButton, stylusCursor, axes);

		handler.onButtonUp(stylusEvent, viewBounds);
	}

	private void onMouseMoved(MouseEvent event) {
		StylusButton stylusButton = convertButton(event);
		double[] axes = new double[2];
		axes[0] = event.getX();
		axes[1] = event.getY();

		StylusEvent stylusEvent = new StylusEvent(stylusButton, stylusCursor, axes);

		handler.onCursorMove(stylusEvent, viewBounds);
	}

	private static StylusButton convertButton(MouseEvent event) {
		switch (event.getButton()) {
			case PRIMARY:
				return StylusButton.LEFT;
			case SECONDARY:
				return StylusButton.RIGHT;
			case MIDDLE:
				return StylusButton.MIDDLE;
			default:
				return StylusButton.NONE;
		}
	}
}
