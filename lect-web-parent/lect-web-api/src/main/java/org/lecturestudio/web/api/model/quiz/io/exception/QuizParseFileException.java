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

package org.lecturestudio.web.api.model.quiz.io.exception;

import java.io.IOException;

public class QuizParseFileException extends IOException implements QuizParseException {

	private static final long serialVersionUID = -7650759897102905950L;

	private final int line;

	private String question;


	public QuizParseFileException(String message, int line) {
		super(message);
		this.line = line;
	}

	public QuizParseFileException(String message, String question, int line) {
		super(message);
		this.question = question;
		this.line = line;
	}

	public String getQuestion() {
		return question;
	}

	public int getLine() {
		return line;
	}

	@Override
	public String getMessage() {
		String message = "Error: Line " + getLine() + "\n";

		if (getQuestion() != null) {
			message += getQuestion() + "\n\n";
		}

		message += super.getMessage();

		return message;
	}

}
