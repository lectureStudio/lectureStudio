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

package org.lecturestudio.web.api.model.quiz.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.model.quiz.QuizResult;

public class QuizResultCsvWriter {

	private final char newLine = '\n';

	private final char delimiter;


	public QuizResultCsvWriter(char delimiter) {
		this.delimiter = delimiter;
	}

	public void write(QuizResult result, File file) throws IOException {
		Map<QuizAnswer, Integer> resultMap = result.getResult();

		// Initialize result table.
		Map<String, Integer> optionCountMap = new LinkedHashMap<>();
		List<String> options = result.getQuiz().getOptions();
		for (String option : options) {
			optionCountMap.put(option, 0);
		}

		FileWriter writer = new FileWriter(file);

		org.jsoup.nodes.Document jdoc = Jsoup.parseBodyFragment(result.getQuiz().getQuestion());
		jdoc.outputSettings().prettyPrint(true);

		// Write headers.
		writer.append("Question");
		writer.append(delimiter);
		writer.append(jdoc.text());
		writer.append(newLine);

		writer.append("Option");
		writer.append(delimiter);
		writer.append("Count");
		writer.append(newLine);

		// Sum up all options.
		for (QuizAnswer answer : resultMap.keySet()) {
			for (String optionIndex : answer.getOptions()) {
				String option = options.get(Integer.parseInt(optionIndex));
				Integer optCount = optionCountMap.get(option);

				int count = resultMap.get(answer) + optCount;

				optionCountMap.put(option, count);
			}
		}

		// Write results.
		for (String option : optionCountMap.keySet()) {
			writer.append(option);
			writer.append(delimiter);
			writer.append(optionCountMap.get(option).toString());
			writer.append(newLine);
		}

		writer.flush();
		writer.close();
	}

}
