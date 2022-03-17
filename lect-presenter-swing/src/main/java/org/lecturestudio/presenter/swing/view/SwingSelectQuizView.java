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

package org.lecturestudio.presenter.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTable;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.SelectQuizView;
import org.lecturestudio.presenter.swing.view.model.QuizTableModel;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;

@SwingView(name = "select-quiz")
public class SwingSelectQuizView extends ContentPane implements SelectQuizView {

	private ConsumerAction<Quiz> deleteQuizAction;

	private ConsumerAction<Quiz> editQuizAction;

	private final ObjectProperty<Quiz> selectedQuizProperty;

	private JTable quizTableView;

	private JButton closeButton;

	private JButton createQuizButton;

	private JButton startQuizButton;

	public javax.swing.Action deleteAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			Quiz quiz = model.getItem(row);

			executeAction(deleteQuizAction, quiz);
		}
	};

	public javax.swing.Action editAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			Quiz quiz = model.getItem(row);

			executeAction(editQuizAction, quiz);
		}
	};


	public SwingSelectQuizView() {
		super();

		selectedQuizProperty = new ObjectProperty<>();
	}

	@Override
	public void removeQuiz(Quiz quiz) {
		SwingUtils.invoke(() -> {
			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			model.removeItem(quiz);
		});
	}

	@Override
	public void selectQuiz(Quiz quiz) {
		SwingUtils.invoke(() -> {
			if (nonNull(quiz)) {
				QuizTableModel model = (QuizTableModel) quizTableView.getModel();
				int row = model.getRow(quiz);

				if (row > -1) {
					quizTableView.setRowSelectionInterval(row, row);
				}
			}
		});
	}

	@Override
	public void setQuizzes(List<Quiz> quizList) {
		if (isNull(quizList)) {
			return;
		}

		QuizTableModel model = (QuizTableModel) quizTableView.getModel();

		// Replace generic quizzes.
		model.removeIf(quiz -> quiz.getQuizSet() == QuizSet.GENERIC);
		model.addItems(quizList);
	}

	@Override
	public void setDocumentQuizzes(List<Quiz> quizList) {
		if (isNull(quizList)) {
			return;
		}

		QuizTableModel model = (QuizTableModel) quizTableView.getModel();

		// Replace document quizzes.
		model.removeIf(quiz -> quiz.getQuizSet() == QuizSet.DOCUMENT_SPECIFIC);
		model.addItems(quizList);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnCreateQuiz(Action action) {
		SwingUtils.bindAction(createQuizButton, action);
	}

	@Override
	public void setOnDeleteQuiz(ConsumerAction<Quiz> action) {
		deleteQuizAction = action;
	}

	@Override
	public void setOnEditQuiz(ConsumerAction<Quiz> action) {
		editQuizAction = action;
	}

	@Override
	public void setOnStartQuiz(ConsumerAction<Quiz> action) {
		startQuizButton.addActionListener(event -> {
			executeAction(action, selectedQuizProperty.get());
		});
	}

	@ViewPostConstruct
	private void initialize() {
		quizTableView.setModel(new QuizTableModel(quizTableView.getColumnModel()));
		quizTableView.getSelectionModel().addListSelectionListener(e -> {
			int selectedRow = quizTableView.getSelectedRow();

			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			Quiz selectedQuiz = selectedRow > -1 ? model.getItem(selectedRow) : null;

			selectedQuizProperty.set(selectedQuiz);
		});

		selectedQuizProperty.addListener((observable, oldValue, newValue) -> {
			startQuizButton.setEnabled(nonNull(newValue));
		});
	}

}
