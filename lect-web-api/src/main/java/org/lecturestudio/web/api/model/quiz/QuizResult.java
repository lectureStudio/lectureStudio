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

package org.lecturestudio.web.api.model.quiz;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents the result of a quiz, containing the quiz itself and the collected answers.
 * This class handles the validation and storage of quiz answers submitted by participants.
 *
 * @author Alex Andres
 */
public class QuizResult {

	/** The quiz associated with these results. */
	private final Quiz quiz;

	/** Map of quiz answers to their counts, representing how many participants chose each answer. */
	private final TreeMap<QuizAnswer, Integer> result;


	/**
	 * Creates a new QuizResult for the specified quiz.
	 *
	 * @param quiz The quiz for which results are being collected.
	 */
	public QuizResult(final Quiz quiz) {
		this.quiz = quiz;
		this.result = new TreeMap<>();
	}

	/**
	 * Adds a quiz answer to the results if it's valid.
	 * Increments the count for the answer if it already exists.
	 *
	 * @param answer The quiz answer to add.
	 *
	 * @return true if the answer was added successfully, false if the answer was invalid.
	 */
	public boolean addAnswer(QuizAnswer answer) {
		// Drop distorted answer.
		if (!checkAnswer(answer)) {
			return false;
		}

		result.merge(answer, 1, Integer::sum);

		return true;
	}
	
	/**
	 * Gets the quiz associated with these results.
	 *
	 * @return The quiz object.
	 */
	public Quiz getQuiz() {
		return quiz;
	}
	
	/**
	 * Gets the map of quiz answers to their counts.
	 *
	 * @return Map of quiz answers to the number of times each was submitted.
	 */
	public Map<QuizAnswer, Integer> getResult() {
		return result;
	}
	
	/**
	 * Formats a quiz answer as a readable string.
	 * For multiple-choice questions, this returns the selected options as alpha characters.
	 *
	 * @param answer The answer to format.
	 *
	 * @return A string representation of the answer.
	 */
	public String getAnswerText(final QuizAnswer answer) {
		StringBuilder str = new StringBuilder();
		int c = 1;
		int length = answer.getOptions().length;

		for (String o : answer.getOptions()) {
			str.append(quiz.getOptionAlpha(o)).append(c == length ? " " : ", ");
			c++;
		}

		if (str.isEmpty()) {
			str = new StringBuilder("{ }");
		}

		return str.toString();
	}

	/**
	 * Validates whether an answer is acceptable for this quiz.
	 * Checks if the number of options is valid and if the options themselves are valid.
	 * For numeric quizzes, it also validates and normalizes numeric values.
	 *
	 * @param answer The answer to validate.
	 *
	 * @return true if the answer is valid, false otherwise.
	 */
	protected boolean checkAnswer(final QuizAnswer answer) {
		if (answer.getOptions().length > quiz.getOptions().size()
				&& quiz.getType() != Quiz.QuizType.NUMERIC) {
			return false;
		}

		if (quiz.getType() == Quiz.QuizType.NUMERIC) {
			try {
				for (int i = 0; i < answer.getOptions().length; i++) {
					String option = answer.getOptions()[i];
					Number value = getNumber(option);
					answer.setValue(i, value.toString());
				}
			}
			catch (Exception e) {
				return false;
			}
		}
		else if (quiz.getType() == Quiz.QuizType.FREE_TEXT) {
			// Implement restriction for free text answers if needed.
		}
		else {
			try {
				for (String option : answer.getOptions()) {
					int num = Integer.parseInt(option);
					if (num < 0 || num >= quiz.getOptions().size()) {
						return false;
					}
				}
			}
			catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Attempts to parse a string value as a number.
	 * First try to parse as integer, then as float.
	 * Handles comma as decimal separator by converting it to a period.
	 *
	 * @param value The string to parse.
	 *
	 * @return The parsed number, or null if parsing fails.
	 */
	protected Number getNumber(String value) {
		Number num = getInteger(value);

		if (num != null) {
			return num;
		}

		if (value.contains(",")) {
			value = value.replaceAll(",", ".");
		}

		num = getFloat(value);

		return num;
	}

	/**
	 * Attempts to parse a string value as an integer.
	 *
	 * @param value The string to parse.
	 *
	 * @return The parsed integer, or null if parsing fails.
	 */
	protected Number getInteger(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Attempts to parse a string value as a float.
	 *
	 * @param value The string to parse.
	 *
	 * @return The parsed float, or null if parsing fails.
	 */
	protected Number getFloat(String value) {
		try {
			return Float.parseFloat(value);
		}
		catch (Exception e) {
			return null;
		}
	}
}
