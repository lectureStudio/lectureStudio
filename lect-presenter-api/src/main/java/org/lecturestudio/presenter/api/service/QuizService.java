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

@Singleton
public class QuizService {

	private final QuizDataSource quizDataSource;

	private final DocumentService documentService;


	@Inject
	public QuizService(QuizDataSource quizDataSource, DocumentService documentService) {
		this.quizDataSource = quizDataSource;
		this.documentService = documentService;
	}

	public List<Quiz> getQuizzes() throws IOException {
		return quizDataSource.getQuizzes();
	}

	public List<Quiz> getQuizzes(Document doc) throws IOException {
		return quizDataSource.getQuizzes(doc);
	}

	public void saveQuiz(Quiz quiz) throws IOException {
		quizDataSource.saveQuiz(quiz);
	}

	public void saveQuiz(Quiz quiz, Document doc) throws IOException {
		quizDataSource.saveQuiz(quiz, doc);
	}

	public void deleteQuiz(Quiz quiz) throws IOException {
		quizDataSource.deleteQuiz(quiz);

		// Find and delete the quiz for any opened document.
		for (var document : documentService.getDocuments().getPdfDocuments()) {
			quizDataSource.deleteQuiz(quiz, document);
		}
	}

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
