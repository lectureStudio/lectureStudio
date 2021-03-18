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

package org.lecturestudio.presenter.javafx.view.model;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.lecturestudio.web.api.filter.RegexRule;

public class QuizRegexTableItem {

	private final RegexRule rule;

	private final StringProperty quizRegex;


	public QuizRegexTableItem() {
		this(new RegexRule());
	}

	public QuizRegexTableItem(RegexRule rule) {
		this.quizRegex = new SimpleStringProperty(rule.getRegex());
		this.quizRegex.addListener(observable -> {
			rule.setRegex(getQuizRegex());
		});
		this.rule = rule;
	}

	public RegexRule getRegexRule() {
		return rule;
	}

	public String getQuizRegex() {
		return quizRegex.get();
	}

	public void setQuizRegex(String regex) {
		quizRegex.set(regex);
	}

	public StringProperty quizRegexProperty() {
		return quizRegex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(quizRegex);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		QuizRegexTableItem other = (QuizRegexTableItem) obj;

		return Objects.equals(quizRegex, other.quizRegex);
	}

}
