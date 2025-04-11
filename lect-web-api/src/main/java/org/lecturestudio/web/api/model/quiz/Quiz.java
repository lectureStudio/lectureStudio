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

import static java.util.Objects.nonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lecturestudio.web.api.filter.FilterRule;
import org.lecturestudio.web.api.filter.InputFieldFilter;
import org.lecturestudio.web.api.filter.InputFieldRule;
import org.lecturestudio.web.api.filter.RegexRule;
import org.lecturestudio.web.api.model.HttpResourceFile;

/**
 * Represents a quiz with a question and answer options.
 * This class supports different quiz types (multiple choice, single choice, numeric),
 * questions with media files, and text input rules.
 * <p>
 * The class implements Cloneable and Serializable to enable copying and saving quiz instances.
 * </p>
 *
 * @author Alex Andres
 */
public class Quiz implements Cloneable, Serializable {

	private static final long serialVersionUID = -2922040254601147407L;


	/**
	 * Defines the type of quiz question presented to users.
	 * <p>
	 * MULTIPLE: Multiple choice question allowing multiple correct answers.
	 * SINGLE: Single choice question with only one correct answer.
	 * NUMERIC: Question requiring a numeric input as the answer.
	 * </p>
	 */
	public enum QuizType {
		MULTIPLE, SINGLE, NUMERIC
	}

	/**
	 * Defines the scope or context of the quiz.
	 * <p>
	 * GENERIC: Quiz that is generally applicable and not tied to specific document content.
	 * DOCUMENT_SPECIFIC: Quiz that relates to specific content in a document.
	 * </p>
	 */
	public enum QuizSet {
		GENERIC, DOCUMENT_SPECIFIC
	}

	/** The type of this quiz (multiple choice, single choice, or numeric). */
	private QuizType type;

	/** Identifies the context where this quiz should be stored or displayed. */
	private QuizSet set;

	/** The text of the quiz question presented to users. */
	private String question;

	/** Media files (images, audio, etc.) that accompany the question. */
	private List<HttpResourceFile> questionResources = new ArrayList<>();

	/** The possible answer options for multiple choice and single choice questions. */
	private List<String> options = new ArrayList<>();

	/** Regular expression rules used to validate text input for numeric questions. */
	private List<RegexRule> regexRules = new ArrayList<>();

	/** Filter with rules that validate user input for this quiz. */
	private InputFieldFilter filter = new InputFieldFilter();


	/**
	 * Creates an empty quiz with no type or question.
	 */
	public Quiz() {
		this(null, null);
	}

	/**
	 * Creates a new quiz with the specified type and question.
	 *
	 * @param type     The type of quiz (MULTIPLE, SINGLE, or NUMERIC).
	 * @param question The text of the quiz question.
	 */
	public Quiz(QuizType type, String question) {
		this.type = type;
		this.question = question;
	}

	/**
	 * Adds a new answer option to the quiz.
	 *
	 * @param option The text of the answer option to add.
	 */
	public void addOption(String option) {
		options.add(option);
	}

	/**
	 * Converts a numeric option index to an alphabetic representation.
	 * For non-numeric quiz types, returns the corresponding letter (A, B, C, etc.).
	 * For numeric quiz types, returns the original string.
	 *
	 * @param o The option index as a string.
	 *
	 * @return The alphabetic representation of the option.
	 */
	public String getOptionAlpha(String o) {
		if (getType() == QuizType.NUMERIC) {
			return o;
		}
		else {
			return Character.toString((char) (65 + Integer.parseInt(o)));
		}
	}

	/**
	 * Returns the type of this quiz.
	 *
	 * @return The quiz type (MULTIPLE, SINGLE, or NUMERIC).
	 */
	public QuizType getType() {
		return type;
	}

	/**
	 * Sets the type of this quiz.
	 *
	 * @param type The quiz type to set.
	 */
	public void setType(QuizType type) {
		this.type = type;
	}

	/**
	 * Sets the text of the quiz question.
	 *
	 * @param content The question text to set.
	 */
	public void setQuestion(String content) {
		this.question = content;
	}

	/**
	 * Returns the text of the quiz question.
	 *
	 * @return The question text.
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * Returns the list of media resources associated with the question.
	 *
	 * @return List of HTTP resource files for the question.
	 */
	public List<HttpResourceFile> getQuestionResources() {
		return questionResources;
	}

	/**
	 * Sets the media resources associated with the question.
	 *
	 * @param resources The list of HTTP resource files to set.
	 */
	public void setQuestionResources(List<HttpResourceFile> resources) {
		this.questionResources = resources;
	}

	/**
	 * Removes all answer options from the quiz.
	 */
	public void clearOptions() {
		options.clear();
	}

	/**
	 * Returns the list of answer options for this quiz.
	 *
	 * @return The list of answer option texts.
	 */
	public List<String> getOptions() {
		return options;
	}

	/**
	 * Sets the list of answer options for this quiz.
	 *
	 * @param options The list of answer option texts to set.
	 */
	public void setOptions(List<String> options) {
		this.options = options;
	}

	/**
	 * Returns the list of regular expression rules for validating input.
	 *
	 * @return The list of regex rules.
	 */
	public List<RegexRule> getRegexRules() {
		return regexRules;
	}

	/**
	 * Sets the regular expression rules for validating input.
	 *
	 * @param rules The list of regex rules to set.
	 */
	public void setRegexRules(List<RegexRule> rules) {
		this.regexRules = rules;
	}

	/**
	 * Sets the quiz set (context) for this quiz.
	 *
	 * @param set The quiz set to assign (GENERIC or DOCUMENT_SPECIFIC).
	 */
	public void setQuizSet(QuizSet set) {
		this.set = set;
	}

	/**
	 * Returns the quiz set (context) of this quiz.
	 *
	 * @return The quiz set (GENERIC or DOCUMENT_SPECIFIC).
	 */
	public QuizSet getQuizSet() {
		return set;
	}

	/**
	 * Adds a rule for validating user input.
	 *
	 * @param rule The input field rule to add.
	 */
	public void addInputRule(InputFieldRule<String> rule) {
		filter.registerRule(rule);
	}

	/**
	 * Removes a rule for validating user input.
	 *
	 * @param rule The input field rule to remove.
	 */
	public void removeInputRule(InputFieldRule<String> rule) {
		filter.unregisterRule(rule);
	}

	/**
	 * Returns the filter used for validating user input.
	 *
	 * @return The input field filter.
	 */
	public InputFieldFilter getInputFilter() {
		return filter;
	}

	/**
	 * Sets the filter used for validating user input.
	 *
	 * @param filter The input field filter to set.
	 */
	public void setInputFilter(InputFieldFilter filter) {
		this.filter = filter;
	}

	/**
	 * Removes all rules from the input filter.
	 * Does nothing if the filter is null.
	 */
	public void clearInputFilter() {
		if (nonNull(filter)) {
			filter.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final Quiz other = (Quiz) obj;

		boolean a = Objects.equals(question, other.question);
		boolean b = Objects.equals(type, other.type);
		boolean c = Objects.equals(options, other.options);
		boolean d = Objects.equals(filter, other.filter);

		return a && b && c && d;
	}

	@Override
	public int hashCode() {
		return Objects.hash(question, type, options, filter);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(type).append("\n");
		buffer.append(set).append("\n");
		buffer.append(question).append("\n");

		for (String option : options) {
			buffer.append(" ").append(option).append("\n");
		}

		for (FilterRule<String> rule : filter.getRules()) {
			buffer.append(" ").append(rule).append("\n");
		}

		return buffer.toString();
	}

	@Override
	public Quiz clone() {
		Quiz quiz = new Quiz(type, question);
		quiz.setQuizSet(getQuizSet());

		for (String o : getOptions()) {
			quiz.addOption(o);
		}

		for (InputFieldRule<String> rule : getInputFilter().getRules()) {
			quiz.addInputRule(rule);
		}

		return quiz;
	}

}
