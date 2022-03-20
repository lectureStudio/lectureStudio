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

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;

import org.lecturestudio.web.api.filter.FilterRule;
import org.lecturestudio.web.api.filter.InputFieldFilter;
import org.lecturestudio.web.api.filter.InputFieldRule;
import org.lecturestudio.web.api.filter.RegexRule;
import org.lecturestudio.web.api.model.HttpResourceFile;

@Entity
public class Quiz implements Cloneable, Serializable {

	private static final long serialVersionUID = -2922040254601147407L;


	public enum QuizType {
		MULTIPLE, SINGLE, NUMERIC
	}

	public enum QuizSet {
		GENERIC, DOCUMENT_SPECIFIC
	}


	@Id
	@SequenceGenerator(name = "QuizGen", sequenceName = "quiz_seq", allocationSize = 1)
	@GeneratedValue(generator = "QuizGen")
	private long id;

	private QuizType type;

	/**
	 * This is only used to identify where to store this quiz.
	 */
	private QuizSet set;

	private String question;

	@OneToMany(cascade = {CascadeType.ALL})
	private List<HttpResourceFile> questionResources = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "QuizOptions", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "option")
	private List<String> options = new ArrayList<>();

	@OneToMany(cascade = {CascadeType.ALL})
	private List<RegexRule> regexRules;

	private InputFieldFilter filter = new InputFieldFilter();


	public Quiz() {
		this(null, null);
	}

	public Quiz(QuizType type, String question) {
		this.type = type;
		this.question = question;
	}

	public void addOption(String option) {
		options.add(option);
	}

	public String getOptionAlpha(String o) {
		if (getType() == QuizType.NUMERIC) {
			return o;
		} else {
			return Character.toString((char) (65 + Integer.parseInt(o)));
		}
	}

	public QuizType getType() {
		return type;
	}

	public void setType(QuizType type) {
		this.type = type;
	}

	public void setQuestion(String content) {
		this.question = content;
	}

	public String getQuestion() {
		return question;
	}

	public List<HttpResourceFile> getQuestionResources() {
		return questionResources;
	}

	public void setQuestionResources(List<HttpResourceFile> resources) {
		this.questionResources = resources;
	}

	public void clearOptions() {
		options.clear();
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	/**
	 * @return the regex rules
	 */
	public List<RegexRule> getRegexRules() {
		return regexRules;
	}

	/**
	 * @param rules the regex rules to set
	 */
	public void setRegexRules(List<RegexRule> rules) {
		this.regexRules = rules;
	}

	public void setQuizSet(QuizSet set) {
		this.set = set;
	}

	public QuizSet getQuizSet() {
		return set;
	}

	public void addInputRule(InputFieldRule<String> rule) {
		filter.registerRule(rule);
	}

	public void removeInputRule(InputFieldRule<String> rule) {
		filter.unregisterRule(rule);
	}

	public InputFieldFilter getInputFilter() {
		return filter;
	}

	public void setInputFilter(InputFieldFilter filter) {
		this.filter = filter;
	}

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

		// The replacement of the newlines is a workaround because the QuizWriter removes them
		boolean a = Objects.equals(question.replaceAll("[ \\n]", ""), other.question.replaceAll("[ \\n]", ""));
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

		for (String o : getOptions()) {
			quiz.addOption(o);
		}

		for (InputFieldRule<String> rule : getInputFilter().getRules()) {
			quiz.addInputRule(rule);
		}

		return quiz;
	}

}
