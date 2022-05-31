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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.view.VideoSettingsView;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.util.PathValidator;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "video-settings", presenter = org.lecturestudio.editor.api.presenter.VideoSettingsPresenter.class)
public class FxVideoSettingsView extends GridPane implements VideoSettingsView {

	@FXML
	private ResourceBundle resources;

	@FXML
	private TextField targetPathField;

	@FXML
	private Button browseButton;

	@FXML
	private Label freeSpaceLabel;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxVideoSettingsView() {
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
	public void bindTargetDirectory(StringProperty property) {
		targetPathField.textProperty().bindBidirectional(new LectStringProperty(property));
	}

	@Override
	public void setFreeDiskSpace(String value) {
		String text = MessageFormat.format(resources.getString("video.settings.available.space"), value);

		FxUtils.invoke(() -> freeSpaceLabel.setText(text));
	}

	@Override
	public void setOnSelectTargetDirectory(Action action) {
		FxUtils.bindAction(browseButton, action);
	}

	@FXML
	private void initialize() {
		PathValidator pathValidator = new PathValidator();
		pathValidator.bind(targetPathField);
	}
}
