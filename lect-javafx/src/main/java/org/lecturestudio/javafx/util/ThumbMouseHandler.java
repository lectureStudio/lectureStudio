package org.lecturestudio.javafx.util;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;

public class ThumbMouseHandler implements EventHandler<MouseEvent> {

	protected final Slider slider;

	private final Orientation orientation;

	private double lastX;
	private double lastY;


	public ThumbMouseHandler(Slider slider, Orientation orientation) {
		this.slider = slider;
		this.orientation = orientation;
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
			event.consume();

			if (event.getClickCount() == 2) {
				slider.mouseDoubleClicked();
			}
		}
		else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
			event.consume();

			lastX = event.getX();
			lastY = event.getY();

			slider.mousePressed();
		}
		else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
			event.consume();

			slider.mouseReleased();
		}
		else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
			event.consume();

			if (orientation == Orientation.HORIZONTAL) {
				slider.moveByDelta(event.getX() - lastX);
			}
			else if (orientation == Orientation.VERTICAL) {
				slider.moveByDelta(event.getY() - lastY);
			}

			slider.mouseDragged();
		}
	}
}
