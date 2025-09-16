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

package org.lecturestudio.presenter.api.presenter.command;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.presenter.CreateQuizPresenter;
import org.lecturestudio.web.api.model.quiz.Quiz;

/**
 * Command to edit an existing quiz or create a new one.
 * This command initializes and shows the CreateQuizPresenter with optional
 * quiz and document data, along with specified actions for closing and starting a quiz.
 *
 * @author Alex Andres
 */
public class EditQuizCommand extends ShowPresenterCommand<CreateQuizPresenter> {

	/** The quiz to be edited may be null if creating a new quiz. */
	private final Quiz quiz;

	/** The document associated with the quiz. */
	private final Document document;

	/** Action to execute when starting the quiz. */
	private final Action startAction;

	/** Action to execute when closing the quiz editor. */
	private final Action closeAction;


	/**
	 * Creates a new EditQuizCommand for creating a new quiz.
	 *
	 * @param startAction The action to execute when starting the quiz.
	 * @param closeAction The action to execute when closing the quiz editor.
	 */
	public EditQuizCommand(Action startAction, Action closeAction) {
		super(CreateQuizPresenter.class);

		this.quiz = null;
		this.document = null;
		this.startAction = startAction;
		this.closeAction = closeAction;
	}

	/**
	 * Creates a new EditQuizCommand for editing an existing quiz.
	 *
	 * @param quiz        The quiz to edit.
	 * @param document    The document associated with the quiz.
	 * @param startAction The action to execute when starting the quiz.
	 * @param closeAction The action to execute when closing the quiz editor.
	 */
	public EditQuizCommand(Quiz quiz, Document document, Action startAction, Action closeAction) {
		super(CreateQuizPresenter.class);

		this.quiz = quiz;
		this.document = document;
		this.startAction = startAction;
		this.closeAction = closeAction;
	}

	/**
	 * Executes this command by configuring the presenter with quiz data, document,
	 * and the specified actions.
	 *
	 * @param presenter The CreateQuizPresenter instance to configure.
	 */
	@Override
	public void execute(CreateQuizPresenter presenter) {
		if (nonNull(quiz)) {
			presenter.setQuiz(quiz);
		}
		if (nonNull(document)) {
			presenter.setSelectedDoc(document);
		}

		presenter.setOnClose(closeAction);
		presenter.setOnStartQuiz(startAction);
	}
}
