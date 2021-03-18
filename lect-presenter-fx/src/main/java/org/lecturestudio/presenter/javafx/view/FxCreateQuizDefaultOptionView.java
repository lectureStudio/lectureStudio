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

import static java.util.Objects.isNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;

import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.CreateQuizDefaultOptionView;

@FxmlView(name = "quiz-default-option")
public class FxCreateQuizDefaultOptionView extends FxCreateQuizOptionView implements CreateQuizDefaultOptionView {

	@FXML
	private TextField optionTextField;


	public FxCreateQuizDefaultOptionView() {
		super();
	}

	@Override
	public void focus() {
		FxUtils.invoke(() -> optionTextField.requestFocus());
	}

	@Override
	public String getOptionText() {
		return optionTextField.getText();
	}

	@Override
	public void setOptionText(String text) {
		FxUtils.invoke(() -> optionTextField.setText(text));
	}

	@Override
	void setOptionTooltip(Tooltip tooltip) {
		optionTextField.setTooltip(tooltip);
	}

	@FXML
	private void initialize() {
		if (isNull(optionTextField)) {
			return;
		}

		optionTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::upDownKeyHandler);
		optionTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::enterKeyHandler);
		optionTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::tabKeyHandler);
	}

}
