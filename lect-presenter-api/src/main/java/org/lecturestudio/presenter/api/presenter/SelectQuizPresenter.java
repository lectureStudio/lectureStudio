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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.DocumentQuiz;
import org.lecturestudio.presenter.api.presenter.command.EditQuizCommand;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.view.SelectQuizView;

/**
 * Presenter class responsible for managing quiz selection, creation, editing, and execution.
 * <p>
 * This presenter handles user interactions with the quiz selection view, including
 * <li>Displaying available quizzes from both general and document-specific sources</li>
 * <li>Creating new quizzes</li>
 * <li>Editing existing quizzes</li>
 * <li>Deleting quizzes</li>
 * <li>Starting quizzes for participant interaction</li>
 * <p>
 * It coordinates with multiple services (QuizService, WebService, StreamService) to manage
 * quiz data and execution flow.
 *
 * @author Alex Andres
 */
public class SelectQuizPresenter extends Presenter<SelectQuizView> {

	/**
	 * Service for managing quizzes, providing functionality to retrieve, create, and delete quizzes.
	 */
	private final QuizService quizService;

	/**
	 * Service for web-related functionality, used to retrieve information about currently
	 * started quizzes.
	 */
	private final WebService webService;

	/**
	 * Service for managing the streaming of quizzes to participants.
	 */
	private final StreamService streamService;

	/** Document representing a generic quiz option that's not associated with any specific document. */
	private final Document genericDoc;

	/**
	 * Action to be executed when a quiz is edited. This can be composed with multiple actions
	 * through the setOnEdit method.
	 */
	private ConsumerAction<DocumentQuiz> editAction;


	@Inject
	SelectQuizPresenter(ApplicationContext context, SelectQuizView view, QuizService quizService,
						StreamService streamService, WebService webService) {
		super(context, view);

		this.quizService = quizService;
		this.streamService = streamService;
		this.webService = webService;
		this.genericDoc = ((PresenterContext) context).getGenericDocument();
	}

	@Override
	public void initialize() throws IOException {
		setOnEdit((documentQuiz) -> {
			// Copy quiz. No in-place editing.
			context.getEventBus().post(new EditQuizCommand(documentQuiz.quiz().clone(), documentQuiz.document(),
					this::close, this::reload));
		});

		view.setQuizzes(quizService.getQuizzes().stream()
				.map(q -> new DocumentQuiz(genericDoc, q))
				.collect(Collectors.toList()));
		view.setDocumentQuizzes(quizService.getDocumentQuizzes());
		view.selectQuiz(webService.getStartedQuiz());
		view.setOnClose(this::close);
		view.setOnCreateQuiz(this::createQuiz);
		view.setOnDeleteQuiz(this::deleteQuiz);
		view.setOnEditQuiz(this::editQuiz);
		view.setOnStartQuiz(this::startQuiz);
	}

	/**
	 * Sets or adds an edit action to be executed when a quiz is edited.
	 *
	 * @param action The consumer action to execute when editing a quiz.
	 */
	public void setOnEdit(ConsumerAction<DocumentQuiz> action) {
		this.editAction = action;
	}

	/**
	 * Creates a new quiz by posting an EditQuizCommand to the event bus.
	 * The command is initialized with null quiz, indicating a new quiz creation.
	 */
	private void createQuiz() {
		context.getEventBus().post(new EditQuizCommand(this::close, this::reload));
	}

	/**
	 * Deletes the specified quiz from the quiz service and removes it from the view.
	 *
	 * @param documentQuiz The quiz to be deleted.
	 */
	private void deleteQuiz(DocumentQuiz documentQuiz) {
		try {
			if (genericDoc.equals(documentQuiz.document())) {
				quizService.deleteQuiz(documentQuiz.quiz());
			}
			else {
				quizService.deleteQuiz(documentQuiz.quiz(), documentQuiz.document());
			}

			view.removeQuiz(documentQuiz);
		}
		catch (IOException e) {
			handleException(e, "Delete quiz failed", "quiz.delete.error");
		}
	}

	/**
	 * Executes the edit action on the specified quiz if an edit action is set.
	 *
	 * @param documentQuiz The quiz to be edited.
	 */
	private void editQuiz(DocumentQuiz documentQuiz) {
		if (nonNull(editAction)) {
			editAction.execute(documentQuiz);
		}
	}

	/**
	 * Starts the specified quiz through the stream service and closes the view.
	 *
	 * @param documentQuiz The quiz to be started.
	 */
	private void startQuiz(DocumentQuiz documentQuiz) {
		streamService.startQuiz(documentQuiz);

		close();
	}

	/**
	 * Reloads quiz data from the quiz service and updates the view.
	 * This is typically called after quiz creation or modification.
	 */
	private void reload() {
		try {
			view.setQuizzes(quizService.getQuizzes().stream()
					.map(q -> new DocumentQuiz(genericDoc, q))
					.collect(Collectors.toList()));
			view.setDocumentQuizzes(quizService.getDocumentQuizzes());
			view.selectQuiz(webService.getStartedQuiz());
		}
		catch (IOException e) {
			handleException(e, "Load quiz set failed", "quiz.edit.error");
		}
	}
}