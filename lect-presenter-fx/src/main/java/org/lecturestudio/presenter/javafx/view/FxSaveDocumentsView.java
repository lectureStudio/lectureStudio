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

import static java.util.Objects.nonNull;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;

@FxmlView(name = "save-documents")
public class FxSaveDocumentsView extends ContentPane implements SaveDocumentsView {

	private final IntegerProperty anySelected;

	@FXML
	private Pane individualContainer;

	@FXML
	private TextField pathTextField;

	@FXML
	private Button closeButton;

	@FXML
	private Button selectPathButton;

	@FXML
	private Button saveMergedButton;


	public FxSaveDocumentsView() {
		super();

		anySelected = new SimpleIntegerProperty();
	}

	@Override
	public void addDocumentOptionView(SaveDocumentOptionView optionView) {
		if (nonNull(optionView)) {
			FxSaveDocumentOptionView optionNode = (FxSaveDocumentOptionView) optionView;
			optionNode.selectedProperty().addListener((observable, oldValue, newValue) -> {
				anySelected.set(newValue ? anySelected.get() + 1 : anySelected.get() - 1);
			});

			FxUtils.invoke(() -> {
				individualContainer.getChildren().add(optionNode);
			});
		}
	}

	@Override
	public void setSavePath(StringProperty path) {
		pathTextField.textProperty().bindBidirectional(new LectStringProperty(path));
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnMerge(Action action) {
		FxUtils.bindAction(saveMergedButton, action);
	}

	@Override
	public void setOnSelectPath(Action action) {
		FxUtils.bindAction(selectPathButton, action);
	}

	@FXML
	private void initialize() {
		saveMergedButton.disableProperty().bind(anySelected.lessThanOrEqualTo(0));
	}

}
