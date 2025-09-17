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
import org.lecturestudio.presenter.api.model.DocumentQuiz;
import org.lecturestudio.presenter.api.view.SelectQuizView;
import org.lecturestudio.presenter.swing.view.model.QuizTableModel;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;

@SwingView(name = "select-quiz")
public class SwingSelectQuizView extends ContentPane implements SelectQuizView {

	private ConsumerAction<DocumentQuiz> deleteQuizAction;

	private ConsumerAction<DocumentQuiz> editQuizAction;

	private final ObjectProperty<DocumentQuiz> selectedQuizProperty;

	private JTable quizTableView;

	private JButton closeButton;

	private JButton createQuizButton;

	private JButton startQuizButton;

	public javax.swing.Action deleteAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			DocumentQuiz documentQuiz = model.getItem(row);

			executeAction(deleteQuizAction, documentQuiz);
		}
	};

	public javax.swing.Action editAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = Integer.parseInt(e.getActionCommand());
			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			DocumentQuiz documentQuiz = model.getItem(row);

			executeAction(editQuizAction, documentQuiz);
		}
	};


	public SwingSelectQuizView() {
		super();

		selectedQuizProperty = new ObjectProperty<>();
	}

	@Override
	public void removeQuiz(DocumentQuiz documentQuiz) {
		SwingUtils.invoke(() -> {
			QuizTableModel model = (QuizTableModel) quizTableView.getModel();
			model.removeItem(documentQuiz);
		});
	}

	@Override
	public void selectQuiz(DocumentQuiz documentQuiz) {
		SwingUtils.invoke(() -> {
			if (nonNull(documentQuiz)) {
				QuizTableModel model = (QuizTableModel) quizTableView.getModel();
				int row = model.getRow(documentQuiz);

				if (row > -1) {
					quizTableView.setRowSelectionInterval(row, row);
				}
			}
		});
	}

	@Override
	public void setQuizzes(List<DocumentQuiz> quizList) {
		if (isNull(quizList)) {
			return;
		}

		QuizTableModel model = (QuizTableModel) quizTableView.getModel();

		// Replace generic quizzes.
		model.removeIf(documentQuiz -> documentQuiz.quiz().getQuizSet() == QuizSet.GENERIC);
		model.addItems(quizList);
	}

	@Override
	public void setDocumentQuizzes(List<DocumentQuiz> quizList) {
		if (isNull(quizList)) {
			return;
		}

		QuizTableModel model = (QuizTableModel) quizTableView.getModel();

		// Replace document quizzes.
		model.removeIf(documentQuiz -> documentQuiz.quiz().getQuizSet() == QuizSet.DOCUMENT_SPECIFIC);
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
	public void setOnDeleteQuiz(ConsumerAction<DocumentQuiz> action) {
		deleteQuizAction = action;
	}

	@Override
	public void setOnEditQuiz(ConsumerAction<DocumentQuiz> action) {
		editQuizAction = action;
	}

	@Override
	public void setOnStartQuiz(ConsumerAction<DocumentQuiz> action) {
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
			DocumentQuiz selectedDocQuiz = selectedRow > -1 ? model.getItem(selectedRow) : null;

			selectedQuizProperty.set(selectedDocQuiz);
		});

		selectedQuizProperty.addListener((observable, oldValue, newValue) -> {
			startQuizButton.setEnabled(nonNull(newValue));
		});
	}

}
