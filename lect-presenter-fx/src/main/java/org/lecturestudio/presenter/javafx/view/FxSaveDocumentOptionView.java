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

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;

@FxmlView(name = "save-documents-option")
public class FxSaveDocumentOptionView extends HBox implements SaveDocumentOptionView {

	@FXML
	private CheckBox documentCheckBox;

	@FXML
	private Button saveDocumentButton;

	private Action selectAction;

	private Action deselectAction;


	public FxSaveDocumentOptionView() {
		super();
	}

	@Override
	public void select() {
		documentCheckBox.setSelected(true);
	}

	@Override
	public void deselect() {
		documentCheckBox.setSelected(false);
	}

	@Override
	public String getDocumentTitle() {
		return documentCheckBox.getText();
	}

	@Override
	public void setDocumentTitle(String docTitle) {
		documentCheckBox.setText(docTitle);
	}

	@Override
	public void setOnSaveDocument(Action action) {
		FxUtils.bindAction(saveDocumentButton, action);
	}

	@Override
	public void setOnSelectDocument(Action action) {
		this.selectAction = action;
	}

	@Override
	public void setOnDeselectDocument(Action action) {
		this.deselectAction = action;
	}

	public BooleanProperty selectedProperty() {
		return documentCheckBox.selectedProperty();
	}

	@FXML
	private void initialize() {
		documentCheckBox.selectedProperty().addListener(observable -> {
			if (documentCheckBox.isSelected()) {
				if (nonNull(selectAction)) {
					selectAction.execute();
				}
			}
			else {
				if (nonNull(deselectAction)) {
					deselectAction.execute();
				}
			}
		});
	}

}
