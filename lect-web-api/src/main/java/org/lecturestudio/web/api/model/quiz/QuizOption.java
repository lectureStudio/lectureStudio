package org.lecturestudio.web.api.model.quiz;

import java.util.Objects;

/**
 * A record representing a quiz optionText.
 * Each optionText consists of a text and a flag indicating whether it is correct.
 *
 * @param optionText  The text of the quiz optionText.
 * @param correct {@code true} if this optionText represents a correct answer, {@code false} otherwise.
 */
public record QuizOption(String optionText, boolean correct) {

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		QuizOption that = (QuizOption) o;

		return correct == that.correct && Objects.equals(optionText, that.optionText);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionText, correct);
	}

	@Override
	public String toString() {
		return "QuizOption{" +
				"optionText='" + optionText + '\'' +
				", correct=" + correct +
				'}';
	}
}
