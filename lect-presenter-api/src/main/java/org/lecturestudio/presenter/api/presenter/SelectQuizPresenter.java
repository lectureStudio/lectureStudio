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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.presenter.command.CreateQuizCommand;
import org.lecturestudio.presenter.api.presenter.command.EditQuizCommand;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.view.SelectQuizView;
import org.lecturestudio.web.api.model.quiz.Quiz;

public class SelectQuizPresenter extends Presenter<SelectQuizView> {

	private final DocumentService documentService;

	private final QuizService quizService;

	private final WebService webService;

	private final StreamService streamService;

	private ConsumerAction<Quiz> editAction;


	@Inject
	SelectQuizPresenter(ApplicationContext context, SelectQuizView view,
						DocumentService documentService, QuizService quizService,
						StreamService streamService, WebService webService) {
		super(context, view);

		this.documentService = documentService;
		this.quizService = quizService;
		this.streamService = streamService;
		this.webService = webService;
	}

	@Override
	public void initialize() throws IOException {
		setOnEdit(quiz -> context.getEventBus().post(new EditQuizCommand(quiz, this::close, this::viewUpdateQuiz)));

		viewUpdateQuiz();
		view.setOnClose(this::close);
		view.setOnCreateQuiz(this::createQuiz);
		view.setOnDeleteQuiz(this::deleteQuiz);
		view.setOnEditQuiz(this::editQuiz);
		view.setOnStartQuiz(this::startQuiz);
	}

	public void setOnEdit(ConsumerAction<Quiz> action) {
		this.editAction = ConsumerAction.concatenate(editAction, action);
	}

	private void createQuiz() {
		context.getEventBus().post(new CreateQuizCommand(this::close, this::viewUpdateQuiz));
	}

	private void deleteQuiz(Quiz quiz) {
		try {
			quizService.deleteQuiz(quiz);
			view.removeQuiz(quiz);
		} catch (IOException e) {
			handleException(e, "Delete quiz failed", "quiz.delete.error");
		}
	}

	private void editQuiz(Quiz quiz) {
		if (nonNull(editAction)) {
			editAction.execute(quiz);
		}
	}

	private void startQuiz(Quiz quiz) {
		streamService.startQuiz(quiz);

		close();
	}

	private void viewUpdateQuiz() {
		DocumentList docList = documentService.getDocuments();
		Document doc = docList.getSelectedDocument();

		if (doc.isQuiz()) {
			doc = docList.getLastNonWhiteboard();
		}


		try {
			view.setQuizzes(quizService.getQuizzes());
			view.setDocumentQuizzes(quizService.getQuizzes(doc));
		} catch (IOException e) {
			e.printStackTrace();
		}
		view.selectQuiz(webService.getStartedQuiz());
	}
}
