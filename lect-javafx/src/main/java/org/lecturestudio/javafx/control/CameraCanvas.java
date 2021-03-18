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

import java.awt.image.BufferedImage;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class CameraCanvas extends Pane {

	/** Camera frame drawing canvas. */
	private Canvas canvas;

	/** Camera frame buffer image. */
	private WritableImage bufferImage;


	public CameraCanvas() {
		initialize();
	}

	public void setImage(BufferedImage image) {
		if (isNull(image)) {
			paintNoImage();
		}
		else {
			updateBufferImage(image);

			Platform.runLater(() -> {
				SwingFXUtils.toFXImage(image, bufferImage);

				GraphicsContext context = canvas.getGraphicsContext2D();
				context.drawImage(bufferImage, 0, 0, getWidth(), getHeight());
			});
		}
	}

	public void clearImage() {
		bufferImage = null;

		setImage(null);
	}

	private void updateBufferImage(BufferedImage image) {
		if (isNull(image)) {
			return;
		}

		int width = image.getWidth();
		int height = image.getHeight();
		int bufWidth;
		int bufHeight;

		if (isNull(bufferImage)) {
			bufWidth = 0;
			bufHeight = 0;
		}
		else {
			bufWidth = (int) bufferImage.getWidth();
			bufHeight = (int) bufferImage.getHeight();
		}

		if (bufWidth != width || bufHeight != height) {
			bufferImage = new WritableImage(width, height);
		}
	}

	private void initialize() {
		canvas = new Canvas();
		canvas.setCache(false);
		canvas.setCacheHint(CacheHint.SPEED);
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());

		layoutBoundsProperty().addListener((ChangeListener<Bounds>) (observable, oldBounds, newBounds) -> {
			if (newBounds.getWidth() == 0 || newBounds.getHeight() == 0) {
				return;
			}

			if (isNull(bufferImage)) {
				paintNoImage();
			}
		});

		getChildren().add(canvas);
	}

	private void paintNoImage() {
		double cx = (getWidth() - 70) / 2;
		double cy = (getHeight() - 40) / 2;

		GraphicsContext context = canvas.getGraphicsContext2D();

		context.setFill(Color.BLACK);
		context.fillRect(0, 0, getWidth(), getHeight());
		context.setFill(Color.LIGHTGREY);
		context.fillRoundRect(cx, cy, 70, 40, 10, 10);
		context.setFill(Color.WHITE);
		context.fillOval(cx + 5, cy + 5, 30, 30);
		context.setFill(Color.LIGHTGREY);
		context.fillOval(cx + 10, cy + 10, 20, 20);
		context.setFill(Color.WHITE);
		context.fillOval(cx + 12, cy + 12, 16, 16);
		context.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
		context.fillRect(cx + 63, cy + 25, 7, 2);
		context.fillRect(cx + 63, cy + 28, 7, 2);
		context.fillRect(cx + 63, cy + 31, 7, 2);

		context.setFill(Color.GRAY);
		context.setLineWidth(2);
		context.strokeLine(cx + 10, cy + 10, cx + 30, cy + 30);
		context.strokeLine(cx + 10, cy + 30, cx + 30, cy + 10);
	}

}
