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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.presenter.api.quiz.JsonQuizFileRepository;
import org.lecturestudio.presenter.api.quiz.QuizRepository;
import org.lecturestudio.web.api.data.bind.QuizDeserializer;
import org.lecturestudio.web.api.model.quiz.Quiz;

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
	 * Represents the file extension used for legacy quiz files in the system.
	 * Legacy quiz files are in an outdated format and may require conversion
	 * to the current format to ensure compatibility with the application.
	 * This constant is primarily used within file operations to identify
	 * and process legacy quiz files.
	 */
	private static final String LEGACY_QUIZ_FILE_ENDING = ".quizzes";

	/**
	 * Represents the file extension used for the storage of quizzes in the current file format.
	 * This constant is used to distinguish the new quiz file format from legacy formats, enabling
	 * the system to correctly identify and process files containing quizzes.
	 */
	private static final String QUIZ_FILE_ENDING = ".json";

	/**
	 * The repository for storing and retrieving quiz data in JSON format.
	 * Provides CRUD operations for quiz entities.
	 */
	private final QuizRepository repository;


	/**
	 * Constructs a new QuizDataSource with the specified data file.
	 *
	 * @param dataFile The file that will serve as the quiz data source.
	 */
	public QuizDataSource(final File dataFile) {
		File quizFile = new File(FileUtils.stripExtension(dataFile) + QUIZ_FILE_ENDING);

		repository = getQuizRepository(quizFile);

		if (!quizFile.exists()) {
			try {
				migrateQuizFormat(dataFile, repository);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
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
		QuizRepository repository = getQuizRepository(quizFile);

		if (nonNull(quizFile) && !quizFile.exists()) {
			try {
				migrateQuizFormat(quizFile, repository);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

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

		replaceQuiz(oldQuiz, newQuiz, repository);
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
	public void replaceQuiz(Quiz oldQuiz, Quiz newQuiz, Document doc) throws IOException {
		requireNonNull(oldQuiz);
		requireNonNull(newQuiz);

		QuizRepository repository = getQuizRepository(getQuizFile(doc));

		if (isNull(repository)) {
			throw new IOException("Repository does not exist");
		}

		replaceQuiz(oldQuiz, newQuiz, repository);
	}

	/**
	 * Replaces an existing quiz with a new quiz in the specified repository.
	 * If the old quiz does not exist in the repository, the new quiz will be appended.
	 *
	 * @param oldQuiz    The quiz to be replaced.
	 * @param newQuiz    The new quiz to replace the old one.
	 * @param repository The repository where the replacement will take place.
	 *
	 * @throws IOException If an I/O error occurs during the replacement process.
	 */
	public void replaceQuiz(Quiz oldQuiz, Quiz newQuiz, QuizRepository repository) throws IOException {
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
	 * Migrates quiz data from the legacy format to the current format.
	 * <p>
	 * This method attempts to read quizzes from the legacy file format, and if any
	 * are found, saves them to the current repository format. After successful migration,
	 * the original data file is moved to a backup with the ".old" extension.
	 *
	 * @param dataFile   The quiz data file to migrate.
	 * @param repository The repository where to store the migrated quizzes.
	 *
	 * @throws IOException If an I/O error occurs during the reading, saving, or moving
	 *                     of files during the migration process.
	 */
	private void migrateQuizFormat(final File dataFile, QuizRepository repository) throws IOException {
		File legacyFile = new File(FileUtils.stripExtension(dataFile) + LEGACY_QUIZ_FILE_ENDING);

		if (!legacyFile.exists()) {
			// No legacy file to migrate.
			return;
		}

		// Set up legacy quiz deserializers.
		Map<Class<?>, JsonDeserializer<?>> deserializers = Map.of(Quiz.class, new QuizDeserializer());
		// Initialize a repository to access quizzes stored in the legacy file format.
		JsonQuizFileRepository legacyRepository = new JsonQuizFileRepository(legacyFile, deserializers);
		List<Quiz> legacyQuizzes = legacyRepository.findAll();

		// Only proceed with migration if legacy quizzes exist.
		if (!legacyQuizzes.isEmpty()) {
		    // Save all quizzes from the legacy format to the current repository format
			repository.saveAll(legacyQuizzes);
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

		return new JsonQuizFileRepository(file);
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

		path = path.substring(0, path.lastIndexOf(".")) + QUIZ_FILE_ENDING;

		return new File(path);
	}
}