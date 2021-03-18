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

package org.lecturestudio.presenter.api.view;

import java.util.List;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.web.api.model.quiz.Quiz;

public interface CreateQuizView extends View {

	void clearOptions();

	void addQuizOptionView(CreateQuizOptionView optionView);

	void removeQuizOptionView(CreateQuizOptionView optionView);

	void moveQuizOptionViewUp(CreateQuizOptionView optionView);

	void moveQuizOptionViewDown(CreateQuizOptionView optionView);

	String getQuizText();

	void setQuizText(String text);

	void setDocuments(List<Document> documents);

	void setDocument(Document doc);

	void setQuizType(Quiz.QuizType type);

	void setOnDocumentSelected(ConsumerAction<Document> action);

	void setOnClose(Action action);

	void setOnNewOption(Action action);

	void setOnSaveQuiz(Action action);

	void setOnStartQuiz(Action action);

	void setOnQuizType(ConsumerAction<Quiz.QuizType> action);

}
