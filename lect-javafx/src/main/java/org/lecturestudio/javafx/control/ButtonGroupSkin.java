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

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

public class ButtonGroupSkin extends SkinBase<ButtonGroup> {

	private static final String LEFT_SEGMENT   = "left-pill";
	private static final String CENTER_SEGMENT = "center-pill";
	private static final String RIGHT_SEGMENT  = "right-pill";

	private HBox container;


	protected ButtonGroupSkin(ButtonGroup control) {
		super(control);

		initLayout();
	}

	private void initLayout() {
		container = new HBox();
		container.getStyleClass().add("container");

		getChildren().add(container);

		getSkinnable().getButtons().addListener((InvalidationListener) observable -> updateButtons());

		updateButtons();
	}

	private void updateButtons() {
		ObservableList<ButtonBase> buttons = getSkinnable().getButtons();

		container.getChildren().clear();

		for (int i = 0; i < buttons.size(); i++) {
			ButtonBase button = buttons.get(i);

			button.getStyleClass().removeAll(LEFT_SEGMENT, CENTER_SEGMENT, RIGHT_SEGMENT);

			container.getChildren().add(button);

			if (i == buttons.size() - 1) {
				if (i != 0) {
					button.getStyleClass().add(RIGHT_SEGMENT);
				}
			}
			else if (i == 0) {
				button.getStyleClass().add(LEFT_SEGMENT);
			}
			else {
				button.getStyleClass().add(CENTER_SEGMENT);
			}
		}
	}

}
