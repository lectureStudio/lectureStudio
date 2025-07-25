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

import java.util.Objects;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.StringProperty;

/**
 * Represents an option in a quiz question.
 * Each option has text content and can be marked as correct or incorrect.
 */
public class QuizOption {

    /** The text content of this quiz option. */
	private final StringProperty text = new StringProperty();

    /** Indicates whether this option is the correct answer. */
	private final BooleanProperty correct = new BooleanProperty();


	/**
	 * Creates a new quiz option with default values.
	 * The option text will be null, and the option will not be marked as correct.
	 */
	public QuizOption() {

	}

	/**
	 * Creates a new quiz option with the specified text and correctness status.
	 *
	 * @param text    the text content of the option
	 * @param correct true if this option is the correct answer, false otherwise
	 */
	public QuizOption(String text, boolean correct) {
		this.text.set(text);
		this.correct.set(correct);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		QuizOption that = (QuizOption) o;

		return correct == that.correct && Objects.equals(getOptionText(), that.getOptionText());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getOptionText(), isCorrect());
	}

	@Override
	public String toString() {
		return "QuizOption{" +
				"optionText='" + getOptionText() + '\'' +
				", correct=" + isCorrect() +
				'}';
	}

	/**
	 * Gets the text content of this quiz option.
	 *
	 * @return the text content of the option
	 */
	public String getOptionText() {
		return text.get();
	}

	/**
	 * Sets the text content of this quiz option.
	 *
	 * @param text the new text content for the option.
	 */
	public void setOptionText(String text) {
		this.text.set(text);
	}

	/**
	 * Gets the property object for the text content of this option.
	 * This can be used for binding or observing changes to the text content.
	 *
	 * @return the StringProperty representing the text content of this option.
	 */
	public StringProperty textProperty() {
		return text;
	}

	/**
	 * Checks if this option is marked as the correct answer.
	 *
	 * @return true if this option is the correct answer.
	 */
	public boolean isCorrect() {
		return correct.get();
	}

	/**
	 * Sets whether this option is the correct answer.
	 *
	 * @param correct true to mark this option as correct.
	 */
	public void setCorrect(boolean correct) {
		this.correct.set(correct);
	}

	/**
	 * Gets the property object for the correct status of this option.
	 * This can be used for binding or observing changes to the correctness status.
	 *
	 * @return the BooleanProperty representing whether this option is correct
	 */
	public BooleanProperty correctProperty() {
		return correct;
	}
}
