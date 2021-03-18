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

import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.control.ExtButton;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.CreateQuizOptionView;

@FxmlView(name = "quiz-option")
@DefaultProperty("content")
public abstract class FxCreateQuizOptionView extends HBox implements CreateQuizOptionView {

	private Action moveUpAction;

	private Action moveDownAction;

	private Action enterKeyAction;

	private Action tabKeyAction;

	@FXML
	private HBox content;

	@FXML
	private ExtButton removeButton;

	@FXML
	private ExtButton moveUpButton;

	@FXML
	private ExtButton moveDownButton;


	abstract void setOptionTooltip(Tooltip tooltip);


	public ObservableList<Node> getContent() {
		return content.getChildren();
	}

	@Override
	public void setOnRemove(Action action) {
		FxUtils.bindAction(removeButton, action);
	}

	@Override
	public void setOnMoveUp(Action action) {
		this.moveUpAction = action;

		FxUtils.bindAction(moveUpButton, action);
	}

	@Override
	public void setOnMoveDown(Action action) {
		this.moveDownAction = action;

		FxUtils.bindAction(moveDownButton, action);
	}

	@Override
	public void setOnEnterKey(Action action) {
		this.enterKeyAction = action;
	}

	@Override
	public void setOnTabKey(Action action) {
		this.tabKeyAction = action;
	}

	protected void upDownKeyHandler(KeyEvent event) {
		switch (event.getCode()) {
			case UP:
				executeAction(moveUpAction);
				event.consume();
				break;

			case DOWN:
				executeAction(moveDownAction);
				event.consume();
				break;

			default:
				break;
		}
	}

	protected void tabKeyHandler(KeyEvent event) {
		if (event.isShortcutDown()) {
			return;
		}
		if (event.getCode() == KeyCode.TAB) {
			executeAction(tabKeyAction);
		}
	}

	protected void enterKeyHandler(KeyEvent event) {
		if (event.isShortcutDown()) {
			return;
		}
		if (event.getCode() == KeyCode.ENTER) {
			executeAction(enterKeyAction);
			event.consume();
		}
	}

}
