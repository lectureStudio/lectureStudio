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

package org.lecturestudio.javafx.input;

import static java.util.Objects.isNull;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.stylus.StylusCursor;
import org.lecturestudio.stylus.StylusEvent;

public class StylusListener implements org.lecturestudio.stylus.StylusListener {

	private final StylusHandler handler;

	private final SlideView slideView;

	private final Rectangle2D viewBounds;

	private Image penCursorImage;

	private StylusCursor stylusCursor;


	public StylusListener(StylusHandler handler, SlideView slideView) {
		this.handler = handler;
		this.slideView = slideView;
		this.viewBounds = new Rectangle2D();

		slideView.canvasBoundsProperty().addListener(observable -> {
			Bounds canvasBounds = slideView.getCanvasBounds();

			viewBounds.setLocation(canvasBounds.getMinX(), canvasBounds.getMinY());
			viewBounds.setSize(canvasBounds.getWidth(), canvasBounds.getHeight());
		});
	}

	@Override
	public void onCursorChange(StylusEvent stylusEvent) {
		stylusCursor = stylusEvent.getCursor();

		handler.onCursorChange(stylusEvent, viewBounds);

		Cursor cursor;

		if (stylusEvent.getCursor() == StylusCursor.PEN || stylusEvent.getCursor() == StylusCursor.ERASER) {
			if (isNull(penCursorImage)) {
				ClassLoader classLoader = StylusListener.class.getClassLoader();

				penCursorImage = new Image(classLoader.getResourceAsStream("resources/gfx/pen-cursor.png"));
			}

			cursor = new ImageCursor(penCursorImage,
					penCursorImage.getWidth() / 2,
					penCursorImage.getHeight() / 2);
		}
		else {
			cursor = Cursor.DEFAULT;
		}

		slideView.setCursor(cursor);
	}

	@Override
	public void onCursorMove(StylusEvent stylusEvent) {
		if (stylusCursor != stylusEvent.getCursor()) {
			onCursorChange(stylusEvent);
		}

		handler.onCursorMove(stylusEvent, viewBounds);
	}

	@Override
	public void onButtonDown(StylusEvent stylusEvent) {
		if (stylusCursor != stylusEvent.getCursor()) {
			onCursorChange(stylusEvent);
		}

		Platform.runLater(() -> {
			slideView.setToolStarted(true);
			handler.onButtonDown(stylusEvent, viewBounds);
		});
	}

	@Override
	public void onButtonUp(StylusEvent stylusEvent) {
		if (stylusCursor != stylusEvent.getCursor()) {
			onCursorChange(stylusEvent);
		}
		Platform.runLater(() -> {
			handler.onButtonUp(stylusEvent, viewBounds);
			slideView.setToolStarted(false);
		});
	}

}