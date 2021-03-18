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

package org.lecturestudio.presenter.javafx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.beans.LectColorProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.WhiteboardSettingsView;

@FxmlView(name = "whiteboard-settings", presenter = org.lecturestudio.presenter.api.presenter.WhiteboardSettingsPresenter.class)
public class FxWhiteboardSettingsView extends GridPane implements WhiteboardSettingsView {

	@FXML
	private ColorPicker whiteboardColorPicker;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxWhiteboardSettingsView() {
		super();
	}

	@Override
	public void setBackgroundColor(ObjectProperty<Color> color) {
		whiteboardColorPicker.valueProperty().bindBidirectional(new LectColorProperty(color));
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

}
