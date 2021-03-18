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

import javafx.animation.FadeTransition;
import javafx.scene.Cursor;
import javafx.util.Duration;

public class ClearableTextFieldSkin extends CustomTextFieldSkin {

	private FadeTransition fadeTransition;


	protected ClearableTextFieldSkin(ClearableTextField control) {
		super(control);

		initLayout(control);
	}

	private void initLayout(ClearableTextField control) {
		SvgIcon clearButton = new SvgIcon();
		clearButton.getStyleClass().addAll("clear-button");
		clearButton.setCursor(Cursor.DEFAULT);
		clearButton.setOpacity(0);
		clearButton.setOnMouseReleased(e -> control.clear());
		clearButton.visibleProperty().bind(control.editableProperty());

		fadeTransition = new FadeTransition(Duration.millis(350), clearButton);

		control.setRightNode(clearButton);

		registerChangeListener(control.textProperty(), o -> {
			String text = (String) o.getValue();

			boolean isTextEmpty = text == null || text.isEmpty();
			boolean isButtonVisible = fadeTransition.getNode().getOpacity() > 0;

			if (isTextEmpty && isButtonVisible) {
				setButtonVisible(false);
			}
			else if (!isTextEmpty && !isButtonVisible) {
				setButtonVisible(true);
			}
		});
	}

	private void setButtonVisible( boolean visible ) {
		fadeTransition.setFromValue(visible ? 0 : 1);
		fadeTransition.setToValue(visible ? 1 : 0);
		fadeTransition.play();
	}
}
