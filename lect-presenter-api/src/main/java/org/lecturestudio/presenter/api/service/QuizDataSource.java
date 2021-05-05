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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.presenter.api.quiz.QuizFileReader;
import org.lecturestudio.presenter.api.quiz.QuizFileWriter;
import org.lecturestudio.presenter.api.quiz.QuizReader;
import org.lecturestudio.presenter.api.quiz.QuizWriter;
import org.lecturestudio.web.api.model.quiz.Quiz;

public class QuizDataSource {

	private final File quizFile;


	public QuizDataSource(File dataFile) {
		quizFile = dataFile;
	}

	public void clear() {
		quizFile.delete();
	}

	public void clear(Document doc) {
		File quizFile = getQuizFile(doc);

		if (nonNull(quizFile)) {
			quizFile.delete();
		}
	}

	public List<Quiz> getQuizzes() throws IOException {
		return getQuizzes(this.quizFile, Quiz.QuizSet.GENERIC);
	}

	public List<Quiz> getQuizzes(Document doc) throws IOException {
		File quizFile = getQuizFile(doc);
		return getQuizzes(quizFile, Quiz.QuizSet.DOCUMENT_SPECIFIC);
	}

	public void deleteQuiz(Quiz quiz, Document doc) throws IOException {
		QuizReader reader = getQuizReader(doc);
		QuizWriter writer = getQuizWriter(doc);

		if (reader == null) {
			return;
		}

		List<Quiz> quizzes = reader.readQuizzes();

		if (quizzes == null) {
			return;
		}

		if (quizzes.remove(quiz)) {
			writer.clear();

			for (Quiz q : quizzes) {
				writer.writeQuiz(q);
			}
		}
	}

	public void saveQuiz(Quiz quiz, Document doc) throws IOException {
		requireNonNull(quiz);

		if (quiz.getQuestion().isEmpty() || quiz.getOptions().isEmpty()) {
			return;
		}

		QuizReader reader = getQuizReader(doc);
		QuizWriter writer = getQuizWriter(doc);

		if (nonNull(reader)) {
			List<Quiz> quizzes = reader.readQuizzes();
			int index = quizzes != null ? quizzes.indexOf(quiz) : -1;

			// If quiz is already present, skip.
			if (index > -1) {
				return;
			}
		}

		writer.writeQuiz(quiz);
	}

	public void saveQuiz(Quiz oldQuiz, Quiz newQuiz, Document doc) throws IOException {
		QuizWriter writer = getQuizWriter(doc);
		QuizReader reader = getQuizReader(doc);

		List<Quiz> quizzes = reader.readQuizzes();

		int index = quizzes != null ? quizzes.indexOf(oldQuiz) : -1;
		if (index > -1) {
			// Overwrite old quiz.
			quizzes.set(index, newQuiz);

			writer.clear();

			for (Quiz q : quizzes) {
				writer.writeQuiz(q);
			}
		}
		else {
			// Append quiz.
			writer.writeQuiz(newQuiz);
		}
	}

	private List<Quiz> getQuizzes(File quizFile, Quiz.QuizSet set) throws IOException {
		QuizReader reader = new QuizFileReader(quizFile, set);

		return reader.readQuizzes();
	}

	private QuizReader getQuizReader(Document doc) {
		QuizReader reader = null;

		if (isNull(doc) || !doc.isPDF()) {
			// Get generic quiz reader.
			reader = new QuizFileReader(quizFile, Quiz.QuizSet.GENERIC);
		}
		else {
			// Get document specific quiz reader.
			File quizFile = getQuizFile(doc);

			if (nonNull(quizFile)) {
				reader = new QuizFileReader(quizFile, Quiz.QuizSet.DOCUMENT_SPECIFIC);
			}
		}

		return reader;
	}

	private QuizWriter getQuizWriter(Document doc) {
		File file;

		if (isNull(doc) || !doc.isPDF()) {
			// Get generic quiz writer.
			file = this.quizFile;
		}
		else {
			// Get document specific quiz writer.
			file = getQuizFile(doc);

			// Fall back to generic quiz file.
			if (isNull(file)) {
				file = this.quizFile;
			}
		}

		return new QuizFileWriter(file);
	}

	private File getQuizFile(Document doc) {
		if (doc == null || doc.getFilePath() == null || !doc.isPDF()) {
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