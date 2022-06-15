/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.presenter.api.quiz.JsonQuizFileRepository;
import org.lecturestudio.presenter.api.quiz.QuizFileReader;
import org.lecturestudio.presenter.api.quiz.QuizReader;
import org.lecturestudio.presenter.api.quiz.QuizRepository;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;

public class QuizDataSource {

	private final File quizFile;

	private final JsonQuizFileRepository repository;


	public QuizDataSource(File dataFile) {
		quizFile = dataFile;
		repository = new JsonQuizFileRepository(
				new File(FileUtils.stripExtension(dataFile) + ".quizzes"));
	}

	public List<Quiz> getQuizzes() throws IOException {
		backupFile(quizFile, QuizSet.GENERIC);

		return repository.findAll();
	}

	public List<Quiz> getQuizzes(Document doc) throws IOException {
		File quizFile = getQuizFile(doc);

		backupFile(quizFile, QuizSet.DOCUMENT_SPECIFIC);

		QuizRepository repository = getQuizRepository(quizFile);

		return nonNull(repository) ? repository.findAll() : List.of();
	}

	public void deleteQuiz(Quiz quiz) throws IOException {
		requireNonNull(quiz);

		repository.delete(quiz);
	}

	public void deleteQuiz(Quiz quiz, Document doc) throws IOException {
		requireNonNull(quiz);

		if (!doc.isPDF()) {
			return;
		}

		QuizRepository repository = getQuizRepository(getQuizFile(doc));

		if (isNull(repository)) {
			throw new IOException("Repository does not exist");
		}

		repository.delete(quiz);
	}

	public void saveQuiz(Quiz quiz) throws IOException {
		requireNonNull(quiz);

		repository.save(quiz);
	}

	public void saveQuiz(Quiz quiz, Document doc) throws IOException {
		requireNonNull(quiz);

		QuizRepository repository = getQuizRepository(getQuizFile(doc));

		if (isNull(repository)) {
			throw new IOException("Repository does not exist");
		}

		repository.save(quiz);
	}

	public void replaceQuiz(Quiz oldQuiz, Quiz newQuiz) throws IOException {
		requireNonNull(oldQuiz);
		requireNonNull(newQuiz);

		List<Quiz> quizzes = repository.findAll();

		int index = quizzes != null ? quizzes.indexOf(oldQuiz) : -1;
		if (index > -1) {
			// Overwrite old quiz.
			quizzes.set(index, newQuiz);

			repository.deleteAll();
			repository.saveAll(quizzes);
		}
		else {
			// Append quiz.
			repository.save(newQuiz);
		}
	}

	public void replaceQuiz(Quiz oldQuiz, Quiz newQuiz, Document doc)
			throws IOException {
		requireNonNull(oldQuiz);
		requireNonNull(newQuiz);

		QuizRepository repository = getQuizRepository(getQuizFile(doc));

		if (isNull(repository)) {
			throw new IOException("Repository does not exist");
		}

		List<Quiz> quizzes = repository.findAll();

		int index = quizzes != null ? quizzes.indexOf(oldQuiz) : -1;
		if (index > -1) {
			// Overwrite old quiz.
			quizzes.set(index, newQuiz);

			repository.deleteAll();
			repository.saveAll(quizzes);
		}
		else {
			// Append quiz.
			repository.save(newQuiz);
		}
	}

	@Deprecated
	private List<Quiz> getQuizzes(File quizFile, Quiz.QuizSet set) throws IOException {
		QuizReader reader = new QuizFileReader(quizFile, set);

		return reader.readQuizzes();
	}

	private void backupFile(File file, Quiz.QuizSet set) throws IOException {
		if (nonNull(file) && file.exists()) {
			// Convert deprecated file format and backup old file storage.
			List<Quiz> oldList = getQuizzes(file, set);

			QuizRepository repository = getQuizRepository(file);
			if (nonNull(repository)) {
				repository.saveAll(oldList);
			}

			var oldFilePath = file.toPath();
			var oldBackupPath = Paths.get(file.getAbsolutePath() + ".backup");

			Files.copy(oldFilePath, oldBackupPath, StandardCopyOption.REPLACE_EXISTING);
			Files.delete(oldFilePath);
		}
	}

	private QuizRepository getQuizRepository(File file) {
		if (isNull(file)) {
			return null;
		}

		return new JsonQuizFileRepository(
				new File(FileUtils.stripExtension(file) + ".quizzes"));
	}

	private File getQuizFile(Document doc) {
		if (isNull(doc) || isNull(doc.getFilePath()) || !doc.isPDF()) {
			return null;
		}

		String path = doc.getFilePath();
		File docFile = new File(path);

		if (!docFile.exists() || !docFile.isFile()) {
			return null;
		}

		path = path.substring(0, path.lastIndexOf(".")) + ".quiz";

		return new File(path);
	}
}