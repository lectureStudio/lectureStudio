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

import java.util.stream.Stream;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.SaveQuizResultsView;

@FxmlView(name = "save-quiz-result")
public class FxSaveQuizResultsView extends ContentPane implements SaveQuizResultsView {

	@FXML
	private TextField pathTextField;

	@FXML
	private CheckBox csvCheckBox;

	@FXML
	private CheckBox pdfCheckBox;

	@FXML
	private Button closeButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button selectPathButton;


	public FxSaveQuizResultsView() {
		super();
	}

	@Override
	public void setSavePath(StringProperty path) {
		pathTextField.textProperty().bindBidirectional(new LectStringProperty(path));
	}

	@Override
	public void selectCsvOption(boolean select) {
		FxUtils.invoke(() -> csvCheckBox.setSelected(select));
	}

	@Override
	public void selectPdfOption(boolean select) {
		FxUtils.invoke(() -> pdfCheckBox.setSelected(select));
	}

	@Override
	public void setOnCsvSelection(ConsumerAction<Boolean> action) {
		FxUtils.bindAction(csvCheckBox, action);
	}

	@Override
	public void setOnPdfSelection(ConsumerAction<Boolean> action) {
		FxUtils.bindAction(pdfCheckBox, action);
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnSave(Action action) {
		FxUtils.bindAction(saveButton, action);
	}

	@Override
	public void setOnSelectPath(Action action) {
		FxUtils.bindAction(selectPathButton, action);
	}

	@FXML
	private void initialize() {
		CheckBox[] options = { csvCheckBox, pdfCheckBox };

		BooleanBinding noneSelected = Bindings.createBooleanBinding(() ->
				Stream.of(options).noneMatch(CheckBox::isSelected),
				Stream.of(options).map(CheckBox::selectedProperty).toArray(Observable[]::new));

		saveButton.disableProperty().bind(noneSelected);
	}

}
