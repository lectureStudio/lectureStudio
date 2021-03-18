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
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class ContributorSkin extends SkinBase<Contributor> {

	/**
	 * Constructor for all ContributorSkin instances.
	 *
	 * @param control The control for which this Skin should attach to.
	 */
	public ContributorSkin(Contributor control) {
		super(control);

		initialize(control);
	}

	private void initialize(Contributor control) {
		GridPane pane = new GridPane();
		pane.getStyleClass().add("contributor-container");

		Circle iconClip = new Circle();

		SvgIcon icon = new SvgIcon();
		icon.getStyleClass().add("contributor-icon");

		Pane iconContainer = new Pane() {

			@Override
			protected void layoutChildren() {
				layoutInArea(icon, icon.snappedLeftInset(), icon.snappedTopInset(), getWidth(), getHeight(), -1, HPos.CENTER, VPos.CENTER);
			}

		};
		iconContainer.getStyleClass().add("contributor-icon-container");
		iconContainer.getChildren().add(icon);
		iconContainer.setClip(iconClip);
		iconContainer.widthProperty().addListener(observable -> {
			final double r = iconContainer.getWidth() / 2;

			iconClip.setRadius(r);
			iconClip.setCenterX(r);
			iconClip.setCenterY(r);
		});

		Label nameLabel = new Label(control.getName());
		nameLabel.getStyleClass().add("contributor-name");

		pane.add(iconContainer, 0, 0);
		pane.add(nameLabel, 1, 0);

		if (control.getFirm() != null) {
			Label firmLabel = new Label(control.getFirm());
			firmLabel.setWrapText(true);

			Pane firmPane = new Pane(firmLabel);
			firmPane.getStyleClass().add("contributor-firm");

			pane.add(firmPane, 0, 1, 2, 1);
		}

		getChildren().add(pane);
	}
}
