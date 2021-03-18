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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizSet;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;

public class QuizTableItem {

	private final StringProperty quizQuestion;
	private final ObjectProperty<QuizType> quizType;
	private final ObjectProperty<QuizSet> quizSet;

	private final Quiz quiz;


	public QuizTableItem(Quiz quiz) {
		this.quizQuestion = new SimpleStringProperty(quiz.getQuestion());
		this.quizType = new SimpleObjectProperty<>(quiz.getType());
		this.quizSet = new SimpleObjectProperty<>(quiz.getQuizSet());

		this.quiz = quiz;
	}

	/**
	 * Returns the quiz associated with this table item.
	 * 
	 * @return the quiz.
	 */
	public Quiz getQuiz() {
		return quiz;
	}

	public String getQuizQuestion() {
		return quizQuestion.get();
	}

	public StringProperty quizQuestionProperty() {
		return quizQuestion;
	}

	public QuizType getQuizType() {
		return quizType.get();
	}

	public ObjectProperty<QuizType> quizTypeProperty() {
		return quizType;
	}

	public QuizSet getQuizSet() {
		return quizSet.get();
	}

	public ObjectProperty<QuizSet> quizSetProperty() {
		return quizSet;
	}

	public boolean isGeneric() {
		return quiz.getQuizSet() == QuizSet.GENERIC;
	}

	@Override
	public int hashCode() {
		return Objects.hash(quiz);
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

		QuizTableItem other = (QuizTableItem) obj;

		return Objects.equals(quiz, other.quiz);
	}

}
