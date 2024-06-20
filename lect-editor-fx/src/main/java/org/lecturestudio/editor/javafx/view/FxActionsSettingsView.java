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

package org.lecturestudio.editor.javafx.view;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.converter.NumberStringConverter;

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.view.ActionsSettingsView;
import org.lecturestudio.javafx.beans.LectIntegerProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "actions-settings", presenter = org.lecturestudio.editor.api.presenter.ActionsSettingsPresenter.class)
public class FxActionsSettingsView extends GridPane implements ActionsSettingsView {

	@FXML
	private ResourceBundle resources;

	@FXML
	private TextField uniteThresholdField;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxActionsSettingsView() {
		super();
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@Override
	public void bindUniteThreshold(IntegerProperty property) {
		uniteThresholdField.textProperty().bindBidirectional(new LectIntegerProperty(property),
				new NumberStringConverter());
	}
}
