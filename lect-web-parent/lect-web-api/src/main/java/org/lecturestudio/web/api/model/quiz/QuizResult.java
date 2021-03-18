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

import java.util.Map;
import java.util.TreeMap;

import org.lecturestudio.core.bus.ApplicationBus;

public class QuizResult {

	private final Quiz quiz;

	private final TreeMap<QuizAnswer, Integer> result;


	public QuizResult(Quiz quiz) {
		this.quiz = quiz;
		this.result = new TreeMap<>();
	}

	public boolean addAnswer(QuizAnswer answer) {
		// Drop distorted answer.
		if (!checkAnswer(answer)) {
			return false;
		}

		result.merge(answer, 1, Integer::sum);

		fireChange();

		return true;
	}
	
	public Quiz getQuiz() {
		return quiz;
	}
	
	public Map<QuizAnswer, Integer> getResult() {
		return result;
	}
	
	public String getAnswerText(QuizAnswer answer) {
		StringBuilder str = new StringBuilder();
		int c = 1;
		int length = answer.getOptions().length;

		for (String o : answer.getOptions()) {
			str.append(quiz.getOptionAlpha(o)).append(c == length ? " " : ", ");
			c++;
		}

		if (str.length() == 0) {
			str = new StringBuilder("{ }");
		}

		return str.toString();
	}

	protected void fireChange() {
		ApplicationBus.post(this);
	}
	
	protected boolean checkAnswer(QuizAnswer answer) {
		if (answer.getOptions().length > quiz.getOptions().size()
				&& quiz.getType() != Quiz.QuizType.NUMERIC) {
			return false;
		}

		if (quiz.getType() == Quiz.QuizType.NUMERIC) {
			try {
				for (int i = 0; i < answer.getOptions().length; i++) {
					String option = answer.getOptions()[i];
					Number value = getNumber(option);
					answer.setValue(i, value.toString());
				}
			}
			catch (Exception e) {
				return false;
			}
		}
		else {
			try {
				for (String option : answer.getOptions()) {
					int num = Integer.parseInt(option);
					if (num < 0 || num >= quiz.getOptions().size()) {
						return false;
					}
				}
			}
			catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	protected Number getNumber(String value) {
		Number num = getInteger(value);

		if (num != null) {
			return num;
		}

		if (value.contains(",")) {
			value = value.replaceAll(",", ".");
		}

		num = getFloat(value);

		return num;
	}

	protected Number getInteger(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (Exception e) {
			return null;
		}
	}

	protected Number getFloat(String value) {
		try {
			return Float.parseFloat(value);
		}
		catch (Exception e) {
			return null;
		}
	}

}
