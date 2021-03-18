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
import static java.util.Objects.nonNull;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.event.CellButtonActionEvent;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.SelectQuizView;
import org.lecturestudio.presenter.javafx.view.model.QuizTableItem;
import org.lecturestudio.web.api.model.quiz.Quiz;

@FxmlView(name = "select-quiz")
public class FxSelectQuizView extends ContentPane implements SelectQuizView {

	private final ObservableList<QuizTableItem> quizItems;

	private final ObjectProperty<Quiz> selectedQuiz;

	private ConsumerAction<Quiz> deleteQuizAction;

	private ConsumerAction<Quiz> editQuizAction;

	@FXML
	private TableView<QuizTableItem> quizTableView;

	@FXML
	private Button closeButton;

	@FXML
	private Button startQuizButton;


	public FxSelectQuizView() {
		super();

		quizItems = FXCollections.observableArrayList();
		selectedQuiz = new SimpleObjectProperty<>();
	}

	@Override
	public void removeQuiz(Quiz quiz) {
		FxUtils.invoke(() -> quizItems.remove(new QuizTableItem(quiz)));
	}

	@Override
	public void selectQuiz(Quiz quiz) {
		FxUtils.invoke(() -> {
			selectedQuiz.set(quiz);

			if (nonNull(quiz)) {
				quizTableView.getSelectionModel().select(new QuizTableItem(quiz));
			}
		});
	}

	@Override
	public void setQuizzes(List<Quiz> quizList) {
		if (isNull(quizList)) {
			return;
		}

		// Replace generic quizzes.
		quizItems.removeIf(QuizTableItem::isGeneric);

		for (Quiz quiz : quizList) {
			quizItems.add(new QuizTableItem(quiz));
		}
	}

	@Override
	public void setDocumentQuizzes(List<Quiz> quizList) {
		if (isNull(quizList)) {
			return;
		}

		// Replace document quizzes.
		quizItems.removeIf(item -> !item.isGeneric());

		for (Quiz quiz : quizList) {
			quizItems.add(new QuizTableItem(quiz));
		}
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnDeleteQuiz(ConsumerAction<Quiz> action) {
		this.deleteQuizAction = action;
	}

	@Override
	public void setOnEditQuiz(ConsumerAction<Quiz> action) {
		this.editQuizAction = action;
	}

	@Override
	public void setOnStartQuiz(ConsumerAction<Quiz> action) {
		startQuizButton.setOnAction(event -> executeAction(action, selectedQuiz.get()));
	}

	@FXML
	private void onDeleteQuiz(CellButtonActionEvent event) {
		QuizTableItem item = (QuizTableItem) event.getCellItem();
		Quiz quiz = item.getQuiz();

		executeAction(deleteQuizAction, quiz);
	}

	@FXML
	private void onEditQuiz(CellButtonActionEvent event) {
		QuizTableItem item = (QuizTableItem) event.getCellItem();
		Quiz quiz = item.getQuiz();

		executeAction(editQuizAction, quiz);
	}

	@FXML
	private void initialize() {
		quizTableView.setItems(quizItems);
		quizTableView.getSelectionModel().selectedItemProperty().addListener(observable -> {
			QuizTableItem selectedItem = quizTableView.getSelectionModel().getSelectedItem();

			if (nonNull(selectedItem)) {
				selectedQuiz.set(selectedItem.getQuiz());
			}
		});

		startQuizButton.disableProperty().bind(selectedQuiz.isNull());
	}

}
