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

package org.lecturestudio.presenter.api.quiz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.lecturestudio.presenter.api.quiz.exception.QuizParseFileException;
import org.lecturestudio.web.api.filter.InputFieldRule;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;

public class QuizFileReader implements QuizReader {

	private final File file;

	private final QuizSet set;


	public QuizFileReader(File file, Quiz.QuizSet set) {
		this.file = file;
		this.set = set;
	}

	@Override
	public List<Quiz> readQuizzes() throws IOException {
		if (file == null || !file.exists()) {
			return null;
		}

		List<Quiz> quizzes = new ArrayList<>();
		int line = 1;

		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String question = scanner.nextLine();

				if (question.isEmpty()) {
					throw new QuizParseFileException("Missing quiz question", line);
				}

				if (!scanner.hasNextLine()) {
					throw new QuizParseFileException("Missing quiz type", question, line);
				}

				line++;
				QuizType type;

				try {
					type = QuizType.valueOf(scanner.nextLine());
				}
				catch (Exception e) {
					throw new QuizParseFileException("Expected quiz type: MULTIPLE | SINGLE | NUMERIC", question, line);
				}

				line++;

				String[] parts = question.split("\b\b\b");
				question = parts[0];
				List<InputFieldRule<String>> rules = null;

				if (parts.length > 1) {
					byte[] rulesRaw = Base64.getDecoder().decode(parts[1]);
					rules = (List<InputFieldRule<String>>) fromString(rulesRaw);
				}

				// old format conversion
				question = question.replace("\f", "\n");
				// end old format conversion

				Quiz quiz = new Quiz(type, question);
				quiz.setQuizSet(set);

				if (rules != null) {
					quiz.getInputFilter().registerRules(rules);
				}

				if (!scanner.hasNextLine()) {
					throw new QuizParseFileException("Missing quiz option count", question, line);
				}

				int options;
				try {
					options = Integer.parseInt(scanner.nextLine());
				}
				catch (Exception e) {
					throw new QuizParseFileException("Expected a number as option count", question, line);
				}
				line++;

				for (int i = 0; i < options; i++) {
					if (!scanner.hasNextLine()) {
						throw new QuizParseFileException("Missing quiz option: " + (i + 1), question, line);
					}

					quiz.addOption(scanner.nextLine());
					line++;
				}

				quizzes.add(quiz);

				// check for next quiz
				if (scanner.hasNextLine() && !scanner.nextLine().isEmpty()) {
					break;
				}
				line++;
			}
		}

		return quizzes;
	}

	private static Object fromString(byte[] s) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(s));
		Object o;

		try {
			o = ois.readObject();
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}

		ois.close();

		return o;
	}

}
