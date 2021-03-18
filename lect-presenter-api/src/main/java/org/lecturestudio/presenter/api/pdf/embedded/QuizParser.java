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

package org.lecturestudio.presenter.api.pdf.embedded;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.Quiz.QuizType;

/**
 * This QuizParser parses any given text to extract coded quizzes.
 * The quizzes are coded the following way:
 * <pre>
 *      #(Quiz-X:Question) {
 *      Option text x
 *      Option text y
 *      }
 * </pre>
 *
 * Where X may be: M (multiple choice), S (one answer) or N (numerical input answer).
 * Question: is the quiz question with following various option text.
 */
public class QuizParser {

    private final String regex = "#\\(Quiz-(M|S|N):(.*)\\)(\\s*\\{(\\w|\\d|\\s|äÄöÖüÜ|(?![}]).)*[}])";

    private final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);


    public List<Quiz> parse(String text) {
	    if (text == null || text.isEmpty()) {
			return null;
		}

		List<Quiz> list = new ArrayList<>();
        Matcher quizMatcher = pattern.matcher(text);

        while (quizMatcher.find()) {
            // skip if there is no quiz type and question
            if (quizMatcher.groupCount() < 2)
                continue;

            String type = quizMatcher.group(1);
            String question = quizMatcher.group(2);

            QuizType quizType = getQuizType(type);

            if (quizType == null || question == null || question.isEmpty())
                continue;

            Quiz quiz = new Quiz(quizType, question);
            quiz.setQuizSet(Quiz.QuizSet.GENERIC);

            try {
                // remove braces
                String optionStr = quizMatcher.group(3).replaceAll("\\{|\\}", "");
                
                if (optionStr.contains(";")) {
                	String[] options = optionStr.split(";");

                    for (String option : options) {
                        option = option.trim();
                        if (option.length() > 0) {
                            quiz.addOption(option);
                        }
                    }
                }
                else {
                	String[] options = optionStr.split("\r|\n|\r\n");

                    for (String option : options) {
                        option = option.trim();
                        if (option.length() > 0) {
                            quiz.addOption(option);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            list.add(quiz);
        }

        return list;
    }

    private Quiz.QuizType getQuizType(String type) {
        switch (type) {
            case "M":
                return Quiz.QuizType.MULTIPLE;

            case "S":
                return Quiz.QuizType.SINGLE;

            case "N":
                return Quiz.QuizType.NUMERIC;

            default:
                return null;
        }
    }

}
