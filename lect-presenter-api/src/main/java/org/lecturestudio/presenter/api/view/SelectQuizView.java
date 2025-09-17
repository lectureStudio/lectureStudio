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

package org.lecturestudio.presenter.api.view;

import java.util.List;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.model.DocumentQuiz;

/**
 * Interface defining the view for quiz selection functionality.
 * This view allows users to manage quizzes including selection, creation,
 * deletion, editing, and starting quizzes.
 *
 * @author Alex Andres
 */
public interface SelectQuizView extends View {

	/**
	 * Removes the specified quiz from the view.
	 *
	 * @param documentQuiz the quiz to be removed.
	 */
	void removeQuiz(DocumentQuiz documentQuiz);

	/**
	 * Selects the specified quiz in the view.
	 *
	 * @param documentQuiz the quiz to be selected.
	 */
	void selectQuiz(DocumentQuiz documentQuiz);

	/**
	 * Sets the list of available quizzes in the view.
	 *
	 * @param quizList the list of quizzes to display.
	 */
	void setQuizzes(List<DocumentQuiz> quizList);

	/**
	 * Sets the list of quizzes associated with the current document.
	 *
	 * @param quizList the list of document-specific quizzes.
	 */
	void setDocumentQuizzes(List<DocumentQuiz> quizList);

	/**
	 * Sets the action to be executed when the view is closed.
	 *
	 * @param action the action to execute on close.
	 */
	void setOnClose(Action action);

	/**
	 * Sets the action to be executed when creating a new quiz.
	 *
	 * @param action the action to execute on quiz creation.
	 */
	void setOnCreateQuiz(Action action);

	/**
	 * Sets the action to be executed when deleting a quiz.
	 *
	 * @param action the consumer action that processes the quiz to be deleted.
	 */
	void setOnDeleteQuiz(ConsumerAction<DocumentQuiz> action);

	/**
	 * Sets the action to be executed when editing a quiz.
	 *
	 * @param action the consumer action that processes the quiz to be edited.
	 */
	void setOnEditQuiz(ConsumerAction<DocumentQuiz> action);

	/**
	 * Sets the action to be executed when starting a quiz.
	 *
	 * @param action the consumer action that processes the quiz to be started.
	 */
	void setOnStartQuiz(ConsumerAction<DocumentQuiz> action);

}
