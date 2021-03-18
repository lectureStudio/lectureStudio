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

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;

public class SplashSkin extends SkinBase<Splash> {

	private ImageView imageView;

	private Label versionLabel;


	protected SplashSkin(Splash control) {
		super(control);

		initLayout(control);
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + imageView.minWidth(height) + rightInset;
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + imageView.minHeight(width) + bottomInset;
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + imageView.prefWidth(height) + rightInset;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + imageView.prefHeight(width) + bottomInset;
	}

	@Override
	protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return getSkinnable().prefWidth(height);
	}

	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return getSkinnable().prefHeight(width);
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		Pos versionPos = versionLabel.getAlignment();

		layoutInArea(imageView, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
		layoutInArea(versionLabel, contentX, contentY, contentWidth, contentHeight, -1, versionPos.getHpos(), versionPos.getVpos());
	}

	private void initLayout(Splash control) {
		imageView = new ImageView();
		imageView.getStyleClass().add("splash-image");

		versionLabel = new Label();
		versionLabel.textProperty().bind(control.versionProperty());
		versionLabel.getStyleClass().add("version-label");

		getChildren().addAll(imageView, versionLabel);
	}

}
