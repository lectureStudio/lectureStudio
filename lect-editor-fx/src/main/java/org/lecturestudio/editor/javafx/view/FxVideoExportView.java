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

import static java.util.Objects.nonNull;

import java.util.ResourceBundle;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.presenter.VideoExportPresenter;
import org.lecturestudio.editor.api.view.VideoExportView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.util.FileNameValidator;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.util.PathValidator;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "video-export", presenter = VideoExportPresenter.class)
public class FxVideoExportView extends StackPane implements VideoExportView {

	private final IntegerProperty anySelected;

	@FXML
	private ResourceBundle resources;

	@FXML
	private TextField targetNameField;

	@FXML
	private Label targetNameErrorLabel;

	@FXML
	private TextField targetPathField;

	@FXML
	private Label targetPathErrorLabel;

	@FXML
	private CheckBox videoCheckbox;

	@FXML
	private CheckBox vectorPlayerCheckbox;

	@FXML
	private Button browseButton;

	@FXML
	private Button cancelButton;

	@FXML
	private Button createButton;


	public FxVideoExportView() {
		super();

		anySelected = new SimpleIntegerProperty();
	}

	@Override
	public void bindTargetName(StringProperty property) {
		targetNameField.textProperty().bindBidirectional(new LectStringProperty(property));
	}

	@Override
	public void bindTargetDirectory(StringProperty property) {
		targetPathField.textProperty().bindBidirectional(new LectStringProperty(property));
	}

	@Override
	public void bindVideo(BooleanProperty property) {
		videoCheckbox.selectedProperty().bindBidirectional(new LectObjectProperty<>(property));
	}

	@Override
	public void bindVectorPlayer(BooleanProperty property) {
		vectorPlayerCheckbox.selectedProperty().bindBidirectional(new LectBooleanProperty(property));
	}

	@Override
	public void setOnSelectTargetDirectory(Action action) {
		FxUtils.bindAction(browseButton, action);
	}

	@Override
	public void setOnCreate(Action action) {
		FxUtils.bindAction(createButton, action);
	}

	@Override
	public void setOnCancel(Action action) {
		FxUtils.bindAction(cancelButton, action);
	}

	@FXML
	private void initialize() {
		for (var node : lookupAll("CheckBox")) {
			CheckBox checkBox = (CheckBox) node;
			checkBox.selectedProperty()
					.addListener((observable, oldValue, newValue) -> {
						anySelected.set(newValue ?
								anySelected.get() + 1 :
								anySelected.get() - 1);
					});
		}

		FileNameValidator nameValidator = new FileNameValidator();
		nameValidator.bind(targetNameField);
		nameValidator.errorProperty().addListener(o -> {
			if (nonNull(nameValidator.getError())) {
				targetNameErrorLabel.setText(nameValidator.getError());
			}
		});

		PathValidator pathValidator = new PathValidator();
		pathValidator.bind(targetPathField);
		pathValidator.errorProperty().addListener(o -> {
			if (nonNull(pathValidator.getError())) {
				targetPathErrorLabel.setText(pathValidator.getError());
			}
		});

		createButton.disableProperty().bind(anySelected.lessThanOrEqualTo(0)
				.or(nameValidator.validProperty().not())
				.or(pathValidator.validProperty().not()));

		targetNameErrorLabel.managedProperty()
				.bind(nameValidator.errorProperty().isNotEmpty());
		targetPathErrorLabel.managedProperty()
				.bind(pathValidator.errorProperty().isNotEmpty());
	}
}
