/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.model;

import java.util.Objects;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.web.api.model.quiz.Quiz;

/**
 * Associates a document with a quiz.
 * This record represents the relationship between a document and a quiz
 * for document-specific quiz management.
 *
 * @param document The document associated with the quiz.
 * @param quiz     The quiz associated with the document.
 */
public record DocumentQuiz(Document document, Quiz quiz) {

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DocumentQuiz that = (DocumentQuiz) o;

		return Objects.equals(quiz, that.quiz) && Objects.equals(document, that.document);
	}

	@Override
	public int hashCode() {
		return Objects.hash(document, quiz);
	}
}
