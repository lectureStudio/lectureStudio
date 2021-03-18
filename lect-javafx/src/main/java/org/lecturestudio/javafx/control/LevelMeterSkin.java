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

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;

public class LevelMeterSkin extends SkinBase<LevelMeter> {

	private BorderPane pane;

	private Canvas canvas;


	protected LevelMeterSkin(LevelMeter control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		layoutInArea(pane, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
	}

	private void initLayout(LevelMeter control) {
		canvas = new Canvas();
		canvas.setCache(false);
		canvas.setCacheHint(CacheHint.SPEED);
		canvas.widthProperty().bind(control.widthProperty());
		canvas.heightProperty().bind(control.heightProperty());

		canvas.boundsInLocalProperty().addListener((observable, oldBounds, newBounds) -> {
			if (newBounds.getWidth() == 0 || newBounds.getHeight() == 0) {
				return;
			}

			paint();
		});

		control.levelProperty().addListener(observable -> Platform.runLater(this::paint));

		pane = new BorderPane();
		pane.getChildren().add(canvas);

		getChildren().addAll(pane);
	}

	private void paint() {
		Orientation orientation = getSkinnable().getOrientation();
		GraphicsContext context = canvas.getGraphicsContext2D();
		context.save();

		double width = canvas.getWidth();
		double height = canvas.getHeight();

		if (orientation == Orientation.HORIZONTAL) {
			paintHorizontally(context, width, height);
		}
		else if (orientation == Orientation.VERTICAL) {
			paintVertically(context, width, height);
		}

		context.restore();
	}

	private void paintHorizontally(GraphicsContext context, double width, double height) {
		// Create clipping shape.
		context.beginPath();

		for (int i = 0; i < width; i += 5) {
			context.rect(i, 0, 4, height);
		}

		context.closePath();
		context.clip();

		paintBar(context, width, height, 0, 0, snap(getSkinnable().getLevel() * width), height);
	}

	private void paintVertically(GraphicsContext context, double width, double height) {
		// Create clipping shape.
		context.beginPath();

		for (int i = 0; i < height; i += 5) {
			context.rect(0, i, width, 4);
		}

		context.closePath();
		context.clip();

		double levelHeight = snap(getSkinnable().getLevel() * height);

		paintBar(context, width, height, 0, height - levelHeight, width, levelHeight);
	}

	private void paintBar(GraphicsContext context, double width, double height, double levelX, double levelY, double levelWidth, double levelHeight) {
		// Fill background.
		context.setFill(getSkinnable().getBackgroundFill());
		context.fillRect(0, 0, width, height);

		// Create level clip.
		context.beginPath();
		context.rect(levelX, levelY, levelWidth, levelHeight);
		context.closePath();
		context.clip();

		// Paint level.
		context.setFill(getSkinnable().getLevelFill());
		context.fillRect(0, 0, width, height);
	}

	private double snap(double value) {
		return (int) value - 0.5;
	}

}
