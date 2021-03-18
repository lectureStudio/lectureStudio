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

import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.HTMLEditor;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.control.ExtButton;
import org.lecturestudio.javafx.control.ExtRadioButton;
import org.lecturestudio.javafx.internal.event.BasicEventDispatcher;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.CreateQuizOptionView;
import org.lecturestudio.presenter.api.view.CreateQuizView;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;

@FxmlView(name = "create-quiz")
public class FxCreateQuizView extends ContentPane implements CreateQuizView {

	private ConsumerAction<QuizType> quizTypeAction;

	private ObservableList<Document> documents;

	@FXML
	private Pane optionContainer;

	@FXML
	private HTMLEditor htmlEditor;

	@FXML
	private ComboBox<Document> docSetComboBox;

	@FXML
	private ExtRadioButton multipleTypeRadioButton;

	@FXML
	private ExtRadioButton singleTypeRadioButton;

	@FXML
	private ExtRadioButton numericTypeRadioButton;

	@FXML
	private Tooltip optionTooltip;

	@FXML
	private Tooltip lastOptionTooltip;

	@FXML
	private Hyperlink newOptionButton;

	@FXML
	private ExtButton closeButton;

	@FXML
	private ExtButton saveQuizButton;

	@FXML
	private ExtButton startQuizButton;


	public FxCreateQuizView() {
		super();
	}

	@Override
	public void clearOptions() {
		FxUtils.invoke(() -> optionContainer.getChildren().clear());
	}

	@Override
	public void addQuizOptionView(CreateQuizOptionView optionView) {
		if (Node.class.isAssignableFrom(optionView.getClass())) {
			FxUtils.invoke(() -> optionContainer.getChildren().add((Node) optionView));
		}
	}

	@Override
	public void removeQuizOptionView(CreateQuizOptionView optionView) {
		if (Node.class.isAssignableFrom(optionView.getClass())) {
			FxUtils.invoke(() -> optionContainer.getChildren().remove(optionView));
		}
	}

	@Override
	public void moveQuizOptionViewUp(CreateQuizOptionView optionView) {
		ObservableList<Node> children = optionContainer.getChildren();
		int index = children.indexOf(optionView);
		int newIndex = index - 1;

		if (index < 0 || newIndex < 0) {
			return;
		}

		children.remove(index);
		children.add(newIndex, (Node) optionView);
	}

	@Override
	public void moveQuizOptionViewDown(CreateQuizOptionView optionView) {
		ObservableList<Node> children = optionContainer.getChildren();
		int index = children.indexOf(optionView);
		int newIndex = index + 1;

		if (index < 0 || newIndex >= children.size()) {
			return;
		}

		children.remove(index);
		children.add(newIndex, (Node) optionView);
	}

	@Override
	public String getQuizText() {
		return htmlEditor.getHtmlText();
	}

	@Override
	public void setQuizText(String text) {
		htmlEditor.setHtmlText(text);
	}

	@Override
	public void setDocuments(List<Document> docs) {
		FxUtils.invoke(() -> documents.setAll(docs));
	}

	@Override
	public void setDocument(Document doc) {
		FxUtils.invoke(() -> docSetComboBox.getSelectionModel().select(doc));
	}

	@Override
	public void setQuizType(QuizType type) {
		FxUtils.invoke(() -> {
			switch (type) {
				case MULTIPLE:
					multipleTypeRadioButton.setSelected(true);
					break;
				case NUMERIC:
					numericTypeRadioButton.setSelected(true);
					break;
				case SINGLE:
					singleTypeRadioButton.setSelected(true);
					break;
			}
		});
	}

	@Override
	public void setOnDocumentSelected(ConsumerAction<Document> action) {
		docSetComboBox.valueProperty().addListener((observable, oldDoc, newDoc) -> {
			executeAction(action, newDoc);
		});
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnNewOption(Action action) {
		FxUtils.bindAction(newOptionButton, action);
	}

	@Override
	public void setOnSaveQuiz(Action action) {
		FxUtils.bindAction(saveQuizButton, action);
	}

	@Override
	public void setOnStartQuiz(Action action) {
		FxUtils.bindAction(startQuizButton, action);
	}

	@Override
	public void setOnQuizType(ConsumerAction<QuizType> action) {
		this.quizTypeAction = action;
	}

	@FXML
	private void onMultipleType() {
		executeAction(quizTypeAction, QuizType.MULTIPLE);
	}

	@FXML
	private void onSingleType() {
		executeAction(quizTypeAction, QuizType.SINGLE);
	}

	@FXML
	private void onNumericType() {
		executeAction(quizTypeAction, QuizType.NUMERIC);
	}

	@FXML
	private void initialize() {
		// Handle document combo-box.
		documents = docSetComboBox.getItems();

		optionContainer.getChildren().addListener((InvalidationListener) observable -> {
			ObservableList<Node> children = optionContainer.getChildren();
			int size = children.size();

			// Update tooltips.
			for (int i = 0; i < size; i++) {
				Node node = children.get(i);
				FxCreateQuizOptionView view = (FxCreateQuizOptionView) node;

				if (i == size - 1) {
					view.setOptionTooltip(lastOptionTooltip);
				}
				else {
					view.setOptionTooltip(optionTooltip);
				}
			}
		});

		// Consume any scene shortcuts.
		htmlEditor.setEventDispatcher(new BasicEventDispatcher() {

			@Override
			public Event dispatchBubblingEvent(Event event) {
				EventType<? extends Event> eventType = event.getEventType();

				if (eventType == KeyEvent.KEY_PRESSED || eventType == KeyEvent.KEY_RELEASED) {
					event.consume();
				}

				return event;
			}
		});

		// Initial focus.
		htmlEditor.setHtmlText("<body onLoad='document.body.focus();' contenteditable='true'/>");

		// Update accelerator display text for tooltips.
		Node pane = lastOptionTooltip.getGraphic();
		Label label = (Label) pane.lookup("#tooltipTabAccelerator");
		label.setText(KeyCombination.keyCombination(label.getText()).getDisplayText());

		label = (Label) pane.lookup("#tooltipEnterAccelerator");
		label.setText(KeyCombination.keyCombination(label.getText()).getDisplayText());

		label = (Label) pane.lookup("#tooltipUpAccelerator");
		label.setText(KeyCombination.keyCombination(label.getText()).getDisplayText());

		pane = optionTooltip.getGraphic();
		label = (Label) pane.lookup("#tooltipTabAccelerator");
		label.setText(KeyCombination.keyCombination(label.getText()).getDisplayText());

		label = (Label) pane.lookup("#tooltipUpAccelerator");
		label.setText(KeyCombination.keyCombination(label.getText()).getDisplayText());

		label = (Label) pane.lookup("#tooltipDownAccelerator");
		label.setText(KeyCombination.keyCombination(label.getText()).getDisplayText());
	}

}
