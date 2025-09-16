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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.presenter.api.service.QuizDataSource;
import org.lecturestudio.presenter.api.service.QuizService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.service.WebServiceInfo;
import org.lecturestudio.presenter.api.view.SelectQuizView;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizOption;

class SelectQuizPresenterTest extends PresenterTest {

	private Path quizzesCopyPath;

	private DocumentService documentService;

	private QuizService quizService;

	private WebService webService;

	private StreamService streamService;


	@BeforeEach
	void setup() throws ExecutionException, InterruptedException, IOException, URISyntaxException {
		Path quizzesPath = getResourcePath(".").resolve("quizzes.txt");
		quizzesCopyPath = quizzesPath.getParent().resolve("quizzes.txt.copy");

		Files.copy(quizzesPath, quizzesCopyPath, StandardCopyOption.REPLACE_EXISTING);

		String quizzesFile = quizzesCopyPath.toString();

		documentService = context.getDocumentService();
		documentService.openDocument(getResourcePath(".").resolve("empty.pdf").toFile()).get();

		quizService = new QuizService(new QuizDataSource(new File(quizzesFile)), documentService);

		Properties streamProps = new Properties();
		streamProps.load(getClass().getClassLoader()
				.getResourceAsStream("resources/stream.properties"));

		WebServiceInfo webServiceInfo = new WebServiceInfo(streamProps);

		webService = new WebService((PresenterContext) context, documentService, new LocalBroadcaster(context), webServiceInfo);

		streamService = new StreamService((PresenterContext) context, null, webService);
	}

	@AfterEach
	void dispose() throws IOException {
		Files.delete(quizzesCopyPath);
	}

	@Test
	void testInit() throws IOException {
		Quiz quiz1 = new Quiz(Quiz.QuizType.MULTIPLE, "What's up?");
		quiz1.addOption(new QuizOption("a", false));
		quiz1.addOption(new QuizOption("b", false));
		quiz1.addOption(new QuizOption("c", false));

		Quiz quiz2 = new Quiz(Quiz.QuizType.SINGLE, "No choice");
		quiz2.addOption(new QuizOption("-", false));

		Quiz quiz3 = new Quiz(Quiz.QuizType.MULTIPLE, "Pick one");
		quiz3.addOption(new QuizOption("a", false));
		quiz3.addOption(new QuizOption("b", false));
		quiz3.addOption(new QuizOption("c", false));

		Quiz quiz4 = new Quiz(Quiz.QuizType.SINGLE, "Some smart question");
		quiz4.addOption(new QuizOption("1", false));
		quiz4.addOption(new QuizOption("1", false));

		Quiz quiz5 = new Quiz(Quiz.QuizType.NUMERIC, "a + b");
		quiz5.addOption(new QuizOption("x", false));
		quiz5.addOption(new QuizOption("y", false));

		AtomicReference<List<Quiz>> quizzesRef = new AtomicReference<>();
		AtomicReference<List<Quiz>> docQuizzesRef = new AtomicReference<>();

		SelectQuizMockView view = new SelectQuizMockView() {
			@Override
			public void setQuizzes(List<Quiz> quizList) {
				quizzesRef.set(quizList);
			}

			@Override
			public void setDocumentQuizzes(List<Quiz> quizList) {
				docQuizzesRef.set(quizList);
			}
		};

		SelectQuizPresenter presenter = new SelectQuizPresenter(context, view,
				quizService, streamService, webService);
		presenter.initialize();

		assertEquals(List.of(quiz1, quiz2), quizzesRef.get());
		assertEquals(List.of(quiz3, quiz4, quiz5), docQuizzesRef.get());
	}

	@Test
	void testStartQuiz() throws IOException {
		AtomicBoolean closed = new AtomicBoolean();

		SelectQuizMockView view = new SelectQuizMockView();

		SelectQuizPresenter presenter = new SelectQuizPresenter(context, view,
				quizService, streamService, webService);
		presenter.initialize();
		presenter.setOnClose(() -> closed.set(true));

		view.startAction.execute(quizService.getQuizzes().get(0));

		assertTrue(closed.get());
	}

	@Test
	void testDeleteQuiz() throws IOException {
		AtomicReference<Quiz> quizRef = new AtomicReference<>();

		SelectQuizMockView view = new SelectQuizMockView() {
			@Override
			public void removeQuiz(Quiz quiz) {
				quizRef.set(quiz);
			}
		};

		SelectQuizPresenter presenter = new SelectQuizPresenter(context, view,
				quizService, streamService, webService);
		presenter.initialize();

		Quiz toDelete = quizService.getQuizzes().get(1);

		view.deleteAction.execute(toDelete);

		assertEquals(1, quizService.getQuizzes().size());
		assertNotEquals(quizService.getQuizzes().get(0), quizRef.get());
		assertEquals(toDelete, quizRef.get());
	}

	@Test
	void testEditQuiz() throws IOException {
		AtomicReference<Quiz> quizRef = new AtomicReference<>();

		SelectQuizMockView view = new SelectQuizMockView();

		SelectQuizPresenter presenter = new SelectQuizPresenter(context, view,
				quizService, streamService, webService);
		presenter.initialize();
		presenter.setOnEdit(quizRef::set);

		Document doc = documentService.getDocuments().getSelectedDocument();

		view.editAction.execute(quizService.getQuizzes(doc).get(2));

		assertEquals(quizService.getQuizzes(doc).get(2), quizRef.get());
	}



	private static class SelectQuizMockView implements SelectQuizView {

		Action createAction;

		ConsumerAction<Quiz> startAction;

		ConsumerAction<Quiz> editAction;

		ConsumerAction<Quiz> deleteAction;


		@Override
		public void removeQuiz(Quiz quiz) {

		}

		@Override
		public void selectQuiz(Quiz quiz) {

		}

		@Override
		public void setQuizzes(List<Quiz> quizList) {

		}

		@Override
		public void setDocumentQuizzes(List<Quiz> quizList) {

		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnCreateQuiz(Action action) {
			createAction = action;
		}

		@Override
		public void setOnDeleteQuiz(ConsumerAction<Quiz> action) {
			deleteAction = action;
		}

		@Override
		public void setOnEditQuiz(ConsumerAction<Quiz> action) {
			editAction = action;
		}

		@Override
		public void setOnStartQuiz(ConsumerAction<Quiz> action) {
			startAction = action;
		}
	}

}