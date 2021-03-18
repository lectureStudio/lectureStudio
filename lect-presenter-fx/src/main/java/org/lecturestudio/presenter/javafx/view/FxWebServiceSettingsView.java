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

import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.event.CellButtonActionEvent;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.WebServiceSettingsView;
import org.lecturestudio.presenter.javafx.view.model.QuizRegexTableItem;
import org.lecturestudio.web.api.filter.RegexRule;

@FxmlView(name = "webservice-settings", presenter = org.lecturestudio.presenter.api.presenter.WebServiceSettingsPresenter.class)
public class FxWebServiceSettingsView extends GridPane implements WebServiceSettingsView {

	@FXML
	private TextField lectureTitleTextField;

	@FXML
	private TextField lectureTitleShortTextField;

	@FXML
	private TableView<QuizRegexTableItem> quizRegexTableView;

	@FXML
	private ToggleGroup ipPositionGroup;

	@FXML
	private Hyperlink addQuizRegexButton;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;

	private ConsumerAction<RegexRule> deleteQuizRegexAction;


	public FxWebServiceSettingsView() {
		super();
	}

	@Override
	public void setClassroomName(StringProperty name) {
		lectureTitleTextField.textProperty().bindBidirectional(new LectStringProperty(name));
	}

	@Override
	public void setClassroomShortName(StringProperty shortName) {
		lectureTitleShortTextField.textProperty().bindBidirectional(new LectStringProperty(shortName));
	}

	@Override
	public void setOnAddQuizRegex(Action action) {
		FxUtils.bindAction(addQuizRegexButton, action);
	}

	@Override
	public void setOnDeleteQuizRegex(ConsumerAction<RegexRule> action) {
		this.deleteQuizRegexAction = action;
	}

	@Override
	public void setQuizRegexRules(List<RegexRule> regexRules) {
		FxUtils.invoke(() -> {
			quizRegexTableView.getItems().clear();

			for (RegexRule rule : regexRules) {
				quizRegexTableView.getItems().add(new QuizRegexTableItem(rule));
			}
		});
	}

	@Override
	public void setDisplayIpPosition(ObjectProperty<Position> position) {
		// Select config value.
		ipPositionGroup.selectToggle(getIpPosToggle(position.get()));

		// Observe UI.
		ipPositionGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
			Position pos = Position.CENTER;

			if (nonNull(newToggle) && nonNull(newToggle.getUserData())) {
				pos = Position.valueOf((String) newToggle.getUserData());
			}

			position.set(pos);
		});

		// Observe config property.
		position.addListener((observable, oldValue, newValue) -> {
			ipPositionGroup.selectToggle(getIpPosToggle(newValue));
		});
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@FXML
	private void onDeleteQuizRegex(CellButtonActionEvent event) {
		QuizRegexTableItem item = (QuizRegexTableItem) event.getCellItem();
		RegexRule regexRule = item.getRegexRule();

		executeAction(deleteQuizRegexAction, regexRule);
	}

	@FXML
	private void initialize() {
		// Set table column resize policy.
		ObservableList<TableColumn<QuizRegexTableItem, ?>> columns = quizRegexTableView.getColumns();

		// Set table column edit policy.
		@SuppressWarnings("unchecked")
		TableColumn<QuizRegexTableItem, String> regexColumn = (TableColumn<QuizRegexTableItem, String>) columns.get(0);
		regexColumn.setOnEditCommit(event -> {
			QuizRegexTableItem item = event.getTableView().getItems().get(event.getTablePosition().getRow());
			item.setQuizRegex(event.getNewValue());
		});
	}

	private Toggle getIpPosToggle(Position pos) {
		return ipPositionGroup.getToggles().stream()
				.filter(t -> {
					Object userData = t.getUserData();
					return nonNull(userData) && Position.valueOf((String) userData) == pos;
				})
				.findFirst()
				.orElse(ipPositionGroup.getToggles().get(0));
	}

}
