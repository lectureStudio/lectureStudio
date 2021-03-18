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

package org.lecturestudio.presenter.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.web.api.model.quiz.Quiz;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuizServiceTest extends ServiceTest {

	private static Path quizzesPath;
	private static Path quizzesCopyPath;

	private static Path docQuizzesPath;
	private static Path docQuizzesCopyPath;

	private QuizService quizService;

	private Document doc;


	@BeforeAll
	static void init() throws IOException {
		String quizzesFile = QuizServiceTest.class.getClassLoader().getResource("quizzes.txt").getFile();
		String docQuizzesFile = QuizServiceTest.class.getClassLoader().getResource("empty.quiz").getFile();

		quizzesPath = Path.of(quizzesFile);
		quizzesCopyPath = quizzesPath.getParent().resolve("quizzes.txt.copy");

		docQuizzesPath = Path.of(docQuizzesFile);
		docQuizzesCopyPath = docQuizzesPath.getParent().resolve("empty.quiz.copy");

		Files.move(quizzesPath, quizzesCopyPath, StandardCopyOption.REPLACE_EXISTING);
		Files.move(docQuizzesPath, docQuizzesCopyPath, StandardCopyOption.REPLACE_EXISTING);
	}

	@AfterAll
	static void destroy() throws IOException {
		Files.copy(quizzesCopyPath, quizzesPath, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(docQuizzesCopyPath, docQuizzesPath, StandardCopyOption.REPLACE_EXISTING);

		Files.delete(quizzesCopyPath);
		Files.delete(docQuizzesCopyPath);
	}

	@BeforeEach
	void setUp() throws Exception {
		Files.copy(quizzesCopyPath, quizzesPath, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(docQuizzesCopyPath, docQuizzesPath, StandardCopyOption.REPLACE_EXISTING);

		String quizzesFile = quizzesPath.toFile().getPath();
		String docPath = getClass().getClassLoader().getResource("empty.pdf").getFile();

		doc = documentService.openDocument(new File(docPath)).get();

		quizService = new QuizService(new QuizDataSource(new File(quizzesFile)), documentService);
	}

	@Test
	@Order(1)
	void testGetQuizzes() throws IOException {
		Quiz quiz1 = new Quiz(Quiz.QuizType.MULTIPLE, "What's up?");
		quiz1.addOption("a");
		quiz1.addOption("b");
		quiz1.addOption("c");

		Quiz quiz2 = new Quiz(Quiz.QuizType.SINGLE, "No choice");
		quiz2.addOption("-");

		List<Quiz> quizzes = quizService.getQuizzes();

		assertEquals(2, quizzes.size());

		assertQuizEquals(quiz1, quizzes.get(0));
		assertQuizEquals(quiz2, quizzes.get(1));
	}

	@Test
	@Order(2)
	void testGetDocumentQuizzes() throws Exception {
		Quiz quiz1 = new Quiz(Quiz.QuizType.MULTIPLE, "Pick one");
		quiz1.addOption("a");
		quiz1.addOption("b");
		quiz1.addOption("c");

		Quiz quiz2 = new Quiz(Quiz.QuizType.SINGLE, "Some smart question");
		quiz2.addOption("1");
		quiz2.addOption("1");

		Quiz quiz3 = new Quiz(Quiz.QuizType.NUMERIC, "a + b");
		quiz3.addOption("x");
		quiz3.addOption("y");

		List<Quiz> quizzes = quizService.getQuizzes(doc);

		assertEquals(3, quizzes.size());

		assertQuizEquals(quiz1, quizzes.get(0));
		assertQuizEquals(quiz2, quizzes.get(1));
		assertQuizEquals(quiz3, quizzes.get(2));
	}

	@Test
	@Order(3)
	void testSaveQuiz() throws IOException {
		Quiz quiz1 = new Quiz(Quiz.QuizType.MULTIPLE, "Pick one");
		quiz1.addOption("a");
		quiz1.addOption("b");
		quiz1.addOption("c");

		Quiz quiz2 = new Quiz(Quiz.QuizType.SINGLE, "Some smart question");
		quiz2.addOption("1");
		quiz2.addOption("1");

		Quiz quiz3 = new Quiz(Quiz.QuizType.NUMERIC, "a + b");
		quiz3.addOption("x");
		quiz3.addOption("y");

		quizService.saveQuiz(quiz1);
		quizService.saveQuiz(quiz2);
		quizService.saveQuiz(quiz3);

		List<Quiz> quizzes = quizService.getQuizzes();

		assertEquals(5, quizzes.size());

		assertQuizEquals(quiz1, quizzes.get(2));
		assertQuizEquals(quiz2, quizzes.get(3));
		assertQuizEquals(quiz3, quizzes.get(4));
	}

	@Test
	@Order(4)
	void testSaveDocumentQuiz() throws Exception {
		Quiz quiz = new Quiz(Quiz.QuizType.MULTIPLE, "Multiple choice quiz");
		quiz.addOption("r");
		quiz.addOption("s");
		quiz.addOption("t");

		quizService.saveQuiz(quiz, doc);

		List<Quiz> quizzes = quizService.getQuizzes(doc);

		assertEquals(4, quizzes.size());
		assertQuizEquals(quiz, quizzes.get(3));
	}

	@Test
	@Order(5)
	void testDeleteQuiz() throws IOException {
		List<Quiz> quizzes = quizService.getQuizzes();

		quizService.deleteQuiz(quizzes.get(0));

		quizzes = quizService.getQuizzes();
		assertEquals(1, quizzes.size());

		Quiz quiz2 = new Quiz(Quiz.QuizType.SINGLE, "No choice");
		quiz2.addOption("-");

		assertQuizEquals(quiz2, quizzes.get(0));
	}

	@Test
	@Order(6)
	void testDeleteDocumentQuiz() throws IOException {
		List<Quiz> quizzes = quizService.getQuizzes(doc);

		quizService.deleteQuiz(quizzes.get(0));

		quizzes = quizService.getQuizzes(doc);
		assertEquals(2, quizzes.size());

		quizService.deleteQuiz(quizzes.get(0));

		Quiz quiz3 = new Quiz(Quiz.QuizType.NUMERIC, "a + b");
		quiz3.addOption("x");
		quiz3.addOption("y");

		assertQuizEquals(quiz3, quizService.getQuizzes(doc).get(0));
	}

	@Test
	@Order(7)
	void testReplaceQuiz() throws IOException {
		Quiz quiz = new Quiz(Quiz.QuizType.NUMERIC, "Edited question");
		quiz.addOption("num 1");
		quiz.addOption("num 2");

		List<Quiz> quizzes = quizService.getQuizzes();

		quizService.replaceQuiz(quizzes.get(0), quiz);

		quizzes = quizService.getQuizzes();

		assertEquals(2, quizzes.size());
		assertQuizEquals(quiz, quizzes.get(0));
	}

	@Test
	@Order(8)
	void testMoveDocumentQuiz() throws IOException {
		List<Quiz> quizzes = quizService.getQuizzes();
		List<Quiz> docQuizzes = quizService.getQuizzes(doc);

		quizService.replaceQuiz(quizzes.get(0), docQuizzes.get(0));

		quizzes = quizService.getQuizzes();

		assertEquals(2, quizzes.size());
		assertQuizEquals(docQuizzes.get(0), quizzes.get(0));
	}

	private static void assertQuizEquals(Quiz a, Quiz b) {
		assertEquals(a.getType(), b.getType());
		assertEquals(a.getQuestion(), b.getQuestion());
		assertEquals(a.getOptions(), b.getOptions());
	}
}