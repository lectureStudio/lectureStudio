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

package org.lecturestudio.javafx.control;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;

public class CameraViewSkin extends SkinBase<CameraView> {

	private static final Logger LOG = LogManager.getLogger(CameraViewSkin.class);

	/** Represents the current camera capturing state. */
	private final AtomicBoolean started = new AtomicBoolean(false);

	/** Camera image painter. */
	private CameraCanvas canvas;

	/** User-defined capture rectangle. */
	private Rectangle captureRect;


	protected CameraViewSkin(CameraView control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		layoutInArea(canvas, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
	}

	private void initLayout(CameraView control) {
		MouseHandler rectMouseHandler = new MouseHandler();

		canvas = new CameraCanvas();

		captureRect = new Rectangle();
		captureRect.setFill(Color.TRANSPARENT);
		captureRect.setStroke(Color.RED);
		captureRect.setStrokeWidth(2);
		// Add mouse event handlers in order to move this rectangle.
		captureRect.setOnMousePressed(rectMouseHandler);
		captureRect.setOnMouseDragged(rectMouseHandler);

		control.captureProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				startCapture();
			}
			else {
				stopCapture();
			}
		});
		control.cameraFormatProperty().addListener((observable, oldFormat, newFormat) -> setCameraFormat(newFormat));
		control.captureRectProperty().addListener((observable, oldRect, newRect) -> setCaptureRect(oldRect, newRect));
		control.layoutBoundsProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
				if (newValue.getWidth() == 0 || newValue.getHeight() == 0) {
					return;
				}
				if (isNull(control.getCameraFormat())) {
					return;
				}

				control.layoutBoundsProperty().removeListener(this);

				// Initial update.
				Platform.runLater(() -> {
					setCameraFormat(control.getCameraFormat());
					setCaptureRect(null, control.getCaptureRect());
				});
			}
		});

		getChildren().add(canvas);
	}

	/**
	 * Start camera capturing.
	 */
	private void startCapture() {
		if (started.compareAndSet(false, true)) {
			try {
				int imageWidth = (int) canvas.getWidth();
				int imageHeight = (int) canvas.getHeight();

				Camera camera = getSkinnable().getCamera();
				camera.setImageSize(new Dimension2D(imageWidth, imageHeight));
				camera.setImageConsumer(image -> canvas.setImage(image));
				camera.open();
			}
			catch (Exception e) {
				LOG.error("Camera capture error.", e);
			}
		}
	}

	/**
	 * Stop camera capturing.
	 */
	private void stopCapture() {
		if (started.compareAndSet(true, false)) {
			try {
				Camera camera = getSkinnable().getCamera();
				camera.close();
			}
			catch (Exception e) {
				LOG.error("Camera capture error.", e);
			}

			canvas.clearImage();
		}
	}

	private void setCameraFormat(CameraFormat format) {
		CameraView camView = getSkinnable();

		double width = camView.getWidth();
		double height = camView.getHeight();

		if (width < 1 || height < 1) {
			return;
		}

		// Set correct aspect ratio according to capture format.
		height = format.getHeight() / (double) format.getWidth() * width;

		camView.setMinSize(width, height);
		camView.setMaxSize(width, height);
		camView.setPrefSize(width, height);
	}

	private void setCaptureRect(Rectangle2D oldRect, Rectangle2D newRect) {
		if (nonNull(newRect)) {
			if (isNull(oldRect)) {
				getChildren().add(captureRect);
			}

			updateCaptureRect(newRect);
		}
		else {
			getChildren().remove(captureRect);
		}
	}

	private void updateCaptureRect(Rectangle2D newRect) {
		CameraFormat captureFormat = getSkinnable().getCameraFormat();

		double width = getSkinnable().getPrefWidth();
		double height = getSkinnable().getPrefHeight();

		// Transform rectangle size to local UI component size.
		double sx = width / captureFormat.getWidth();
		double sy = height / captureFormat.getHeight();

		double x = newRect.getX() * sx;
		double y = newRect.getY() * sy;
		double w = newRect.getWidth() * sx;
		double h = newRect.getHeight() * sy;

		if (x < 0)	x = 0;
		if (y < 0)	y = 0;
		if (x + w > width)	x = width - w;
		if (y + h > height)	y = height - h;

		// In case the provided capture rectangle is out of bounds with the capture format.
		if (x < 0)	x = 0;
		if (y < 0)	y = 0;
		if (x + w > width)	w = width;
		if (y + h > height)	h = height;

		captureRect.relocate(x, y);
		captureRect.setWidth(w - captureRect.getStrokeWidth());
		captureRect.setHeight(h - captureRect.getStrokeWidth());
	}

	private void relocateCaptureRect(double x, double y) {
		// Bounds restriction.
		double w = getSkinnable().getWidth();
		double h = getSkinnable().getHeight();
		
		double rectWidth = captureRect.getWidth() + captureRect.getStrokeWidth();
		double rectHeight = captureRect.getHeight() + captureRect.getStrokeWidth();

		if (x < 0)	x = 0;
		if (y < 0)	y = 0;
		if (x + rectWidth > w)		x = w - rectWidth;
		if (y + rectHeight > h)		y = h - rectHeight;

		captureRect.relocate(x, y);

		setCaptureViewLocation(x, y);
	}

	private void setCaptureViewLocation(double x, double y) {
		CameraFormat captureFormat = getSkinnable().getCameraFormat();
		Rectangle2D cropRect = getSkinnable().getCaptureRect();

		double maxWidth = captureFormat.getWidth();
		double maxHeight = captureFormat.getHeight();

		double sx = maxWidth / getSkinnable().getWidth();
		double sy = maxHeight / getSkinnable().getHeight();

		x = x * sx;
		y = y * sy;

		// Bounds restriction.
		if (x < 0)	x = 0;
		if (y < 0)	y = 0;
		if (x + cropRect.getWidth() > maxWidth)		x = maxWidth - cropRect.getWidth();
		if (y + cropRect.getHeight() > maxHeight)	y = maxHeight - cropRect.getHeight();

		getSkinnable().setCaptureRect(new Rectangle2D(x, y, cropRect.getWidth(), cropRect.getHeight()));
	}


	private class MouseHandler implements EventHandler<MouseEvent> {

		private double lastX = 0;
		private double lastY = 0;


		@Override
		public void handle(MouseEvent event) {
			EventType<? extends MouseEvent> type = event.getEventType();

			if (type == MouseEvent.MOUSE_PRESSED) {
				lastX = event.getX();
				lastY = event.getY();
			}
			else if (type == MouseEvent.MOUSE_DRAGGED) {
				double x = event.getX() - lastX;
				double y = event.getY() - lastY;

				relocateCaptureRect(snapPositionX(captureRect.getLayoutX() + x), snapPositionY(captureRect.getLayoutY() + y));
			}
		}

	}

}
