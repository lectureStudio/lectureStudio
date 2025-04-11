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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.web.api.model.quiz.Quiz;

/**
 * Service class for managing quiz operations including retrieving, saving, deleting,
 * and replacing quizzes. This service works with both standalone quizzes and document-specific quizzes.
 *
 * @author Alex Andres
 */
@Singleton
public class QuizService {

	/** Data source for quiz operations. */
	private final QuizDataSource quizDataSource;

	/** Service for document operations. */
	private final DocumentService documentService;


	/**
	 * Creates a new QuizService with the specified quiz data source and document service.
	 *
	 * @param quizDataSource  the data source for quiz operations.
	 * @param documentService the service for document operations.
	 */
	@Inject
	public QuizService(QuizDataSource quizDataSource, DocumentService documentService) {
		this.quizDataSource = quizDataSource;
		this.documentService = documentService;
	}

	/**
	 * Retrieves all quizzes.
	 *
	 * @return a list of all quizzes.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public List<Quiz> getQuizzes() throws IOException {
		return quizDataSource.getQuizzes();
	}

	/**
	 * Retrieves all quizzes associated with the specified document.
	 *
	 * @param doc the document to get quizzes for.
	 *
	 * @return a list of quizzes associated with the document.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public List<Quiz> getQuizzes(Document doc) throws IOException {
		return quizDataSource.getQuizzes(doc);
	}

	/**
	 * Saves a quiz to the general quiz collection.
	 *
	 * @param quiz the quiz to save.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void saveQuiz(Quiz quiz) throws IOException {
		quizDataSource.saveQuiz(quiz);
	}

	/**
	 * Saves a quiz associated with a specific document.
	 *
	 * @param quiz the quiz to save.
	 * @param doc the document to associate the quiz with.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void saveQuiz(Quiz quiz, Document doc) throws IOException {
		quizDataSource.saveQuiz(quiz, doc);
	}

	/**
	 * Deletes a quiz from both the general collection and from all open PDF documents.
	 *
	 * @param quiz the quiz to delete.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void deleteQuiz(Quiz quiz) throws IOException {
		quizDataSource.deleteQuiz(quiz);

		// Find and delete the quiz for any opened document.
		for (var document : documentService.getDocuments().getPdfDocuments()) {
			quizDataSource.deleteQuiz(quiz, document);
		}
	}

	/**
	 * Replaces an existing quiz with a new one in the general collection.
	 * If the old quiz is not in the general collection, it will be deleted from
	 * the selected document and the new quiz will be saved to the general collection.
	 *
	 * @param oldQuiz the quiz to be replaced.
	 * @param newQuiz the new quiz to replace with.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void replaceQuiz(Quiz oldQuiz, Quiz newQuiz) throws IOException {
		// Check, if the generic set contains the edited quiz.
		List<Quiz> quizzes = quizDataSource.getQuizzes();

		if (quizzes.contains(oldQuiz)) {
			// Replace in-place.
			quizDataSource.replaceQuiz(oldQuiz, newQuiz);
		}
		else {
			Document selectedDoc = documentService.getDocuments().getSelectedDocument();

			if (selectedDoc.isPDF()) {
				quizDataSource.deleteQuiz(oldQuiz, selectedDoc);
			}
			else {
				quizDataSource.deleteQuiz(oldQuiz);
			}

			quizDataSource.saveQuiz(newQuiz);
		}
	}

	/**
	 * Replaces an existing quiz with a new one in a specific document.
	 * This method handles the logic of quiz replacement when moving between documents.
	 *
	 * @param oldQuiz the quiz to be replaced.
	 * @param newQuiz the new quiz to replace with.
	 * @param doc     the target document for the replacement.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void replaceQuiz(Quiz oldQuiz, Quiz newQuiz, Document doc) throws IOException {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();

		// Remove from generic set.
		quizDataSource.deleteQuiz(oldQuiz);

		// Refresh within the same set.
		if (selectedDoc.equals(doc)) {
			quizDataSource.replaceQuiz(oldQuiz, newQuiz, doc);
		}
		else {
			// Remove from current set, since it has been moved to another set.
			quizDataSource.deleteQuiz(oldQuiz, selectedDoc);

			if (doc.isPDF()) {
				quizDataSource.replaceQuiz(oldQuiz, newQuiz, doc);
			}
			else {
				quizDataSource.replaceQuiz(oldQuiz, newQuiz);
			}
		}
	}
}
