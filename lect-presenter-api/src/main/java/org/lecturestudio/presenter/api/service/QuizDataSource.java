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

/**
 * A data source for managing quiz-related operations.
 * <p>
 * This class provides functionality to load, save, update, and delete quizzes
 * from the file system. It handles both generic quizzes and document-specific quizzes,
 * providing different repositories for each context.
 * <p>
 * The class also manages backward compatibility with legacy quiz file formats
 * by automatically converting and backing up old quiz files.
 *
 * @author Alex Andres
 */
public class QuizDataSource {

	/**
	 * The file representing the original quiz data source.
	 * Used for backup and backward compatibility purposes.
	 */
	private final File quizFile;

	/**
	 * The repository for storing and retrieving quiz data in JSON format.
	 * Provides CRUD operations for quiz entities.
	 */
	private final JsonQuizFileRepository repository;


	/**
	 * Constructs a new QuizDataSource with the specified data file.
	 *
	 * @param dataFile The file that will serve as the quiz data source.
	 */
	public QuizDataSource(File dataFile) {
		quizFile = dataFile;
		repository = new JsonQuizFileRepository(
				new File(FileUtils.stripExtension(dataFile) + ".quizzes"));
	}

	/**
	 * Retrieves all generic quizzes from the repository.
	 * If a legacy quiz file exists, it will be backed up and converted to the new format.
	 *
	 * @return A list of all generic quizzes.
	 *
	 * @throws IOException If an I/O error occurs during the retrieval process.
	 */
	public List<Quiz> getQuizzes() throws IOException {
		backupFile(quizFile, QuizSet.GENERIC);

		return repository.findAll();
	}

	/**
	 * Retrieves all quizzes associated with the specified document.
	 * If a legacy quiz file exists, it will be backed up and converted to the new format.
	 *
	 * @param doc The document for which to retrieve associated quizzes.
	 *
	 * @return A list of quizzes associated with the document, or an empty list if none found.
	 *
	 * @throws IOException If an I/O error occurs during the retrieval process.
	 */
	public List<Quiz> getQuizzes(Document doc) throws IOException {
		File quizFile = getQuizFile(doc);

		backupFile(quizFile, QuizSet.DOCUMENT_SPECIFIC);

		QuizRepository repository = getQuizRepository(quizFile);

		return nonNull(repository) ? repository.findAll() : List.of();
	}

	/**
	 * Deletes a quiz from the generic quiz repository.
	 *
	 * @param quiz The quiz to delete.
	 *
	 * @throws IOException If an I/O error occurs during the deletion process.
	 *
	 * @throws NullPointerException If the quiz parameter is null.
	 */
	public void deleteQuiz(Quiz quiz) throws IOException {
		requireNonNull(quiz);

		repository.delete(quiz);
	}

	/**
	 * Deletes a quiz associated with the specified document.
	 * This operation only works for PDF documents.
	 *
	 * @param quiz The quiz to delete.
	 * @param doc  The document associated with the quiz.
	 *
	 * @throws IOException          If the repository does not exist or if an I/O error occurs.
	 *
	 * @throws NullPointerException If the quiz parameter is nul.
	 */
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

	/**
	 * Saves a quiz to the generic quiz repository.
	 *
	 * @param quiz The quiz to save.
	 *
	 * @throws IOException          If an I/O error occurs during the save process.
	 * @throws NullPointerException If the quiz parameter is null.
	 */
	public void saveQuiz(Quiz quiz) throws IOException {
		requireNonNull(quiz);

		repository.save(quiz);
	}

	/**
	 * Saves a quiz associated with the specified document.
	 *
	 * @param quiz The quiz to save.
	 * @param doc  The document to associate the quiz with.
	 *
	 * @throws IOException          If the repository does not exist or if an I/O error occurs.
	 * @throws NullPointerException If the quiz parameter is null.
	 */
	public void saveQuiz(Quiz quiz, Document doc) throws IOException {
		requireNonNull(quiz);

		QuizRepository repository = getQuizRepository(getQuizFile(doc));

		if (isNull(repository)) {
			throw new IOException("Repository does not exist");
		}

		repository.save(quiz);
	}

	/**
	 * Replaces an existing quiz with a new quiz in the generic repository.
	 * If the old quiz does not exist, the new quiz will be appended to the repository.
	 *
	 * @param oldQuiz The quiz to be replaced.
	 * @param newQuiz The new quiz to replace the old one.
	 *
	 * @throws IOException          If an I/O error occurs during the replacement process.
	 * @throws NullPointerException If either quiz parameter is null.
	 */
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

	/**
	 * Replaces an existing quiz with a new quiz in the document-specific repository.
	 * If the old quiz does not exist, the new quiz will be appended to the repository.
	 *
	 * @param oldQuiz The quiz to be replaced.
	 * @param newQuiz The new quiz to replace the old one.
	 * @param doc     The document associated with these quizzes.
	 *
	 * @throws IOException          If the repository does not exist or if an I/O error occurs.
	 * @throws NullPointerException If either quiz parameter is null.
	 */
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

	/**
	 * Reads quizzes from a file using the deprecated file format.
	 *
	 * @param quizFile The file containing quiz data.
	 * @param set      The quiz set type (GENERIC or DOCUMENT_SPECIFIC).
	 *
	 * @return A list of quizzes read from the file.
	 *
	 * @throws IOException If an I/O error occurs during the reading process.
	 *
	 * @deprecated This method is used for backward compatibility only.
	 */
	@Deprecated
	private List<Quiz> getQuizzes(File quizFile, Quiz.QuizSet set) throws IOException {
		QuizReader reader = new QuizFileReader(quizFile, set);

		return reader.readQuizzes();
	}

	/**
	 * Backs up a quiz file after converting it to the new format.
	 * This method handles the migration from the old quiz file format to the new one.
	 *
	 * @param file The quiz file to back up.
	 * @param set  The quiz set type (GENERIC or DOCUMENT_SPECIFIC).
	 *
	 * @throws IOException If an I/O error occurs during the backup process.
	 */
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

	/**
	 * Creates a quiz repository for the specified file.
	 *
	 * @param file The file for which to create a repository.
	 *
	 * @return A QuizRepository instance, or null if the file is null.
	 */
	private QuizRepository getQuizRepository(File file) {
		if (isNull(file)) {
			return null;
		}

		return new JsonQuizFileRepository(
				new File(FileUtils.stripExtension(file) + ".quizzes"));
	}

	/**
	 * Gets the quiz file associated with a document.
	 * The quiz file has the same path as the document but with a .quiz extension.
	 *
	 * @param doc The document for which to find the associated quiz file.
	 *
	 * @return The quiz file, or null if the document is invalid or not a PDF.
	 */
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