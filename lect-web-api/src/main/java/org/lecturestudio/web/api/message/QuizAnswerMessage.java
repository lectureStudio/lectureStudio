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

package org.lecturestudio.web.api.message;

import org.lecturestudio.web.api.model.quiz.QuizAnswer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A message class for transmitting quiz answers over the web.
 * Extends the base WebMessage class to provide specific functionality for handling quiz answer data.
 *
 * @author Alex Andres
 */
@Getter
@Setter
@NoArgsConstructor
public class QuizAnswerMessage extends WebMessage {

    /** The quiz answer data contained within this message. */
	private QuizAnswer quizAnswer;

}
