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

package org.lecturestudio.web.api.data.bind;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.lecturestudio.web.api.filter.MinMaxRule;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizOption;

/**
 * Custom Jackson deserializer for Quiz objects.
 * <p>
 * This class is responsible for converting JSON data into Quiz objects
 * by handling the specific format of quiz data, including a quiz type and properties.
 * </p>
 */
public class QuizDeserializer extends JsonDeserializer<Quiz> {

	@Override
	public Quiz deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
		JsonNode node = parser.getCodec().readTree(parser);
		Quiz quiz;

		try {
			Quiz.QuizType type = Quiz.QuizType.valueOf(node.get("type").asText());
			Quiz.QuizSet set = Quiz.QuizSet.valueOf(node.get("quizSet").asText());
			String question = node.get("question").asText();
			JsonNode optionsNode = node.get("options");
			JsonNode inputFilterNode = node.get("inputFilter");

			quiz = new Quiz(type, question);
			quiz.setQuizSet(set);

			for (Iterator<JsonNode> it = optionsNode.elements(); it.hasNext(); ) {
				quiz.addOption(new QuizOption(it.next().asText(), false));
			}

			if (inputFilterNode.has("rules")) {
				JsonNode rulesNode = inputFilterNode.get("rules");

				for (Iterator<JsonNode> it = rulesNode.elements(); it.hasNext(); ) {
					JsonNode ruleNode = it.next();

					if (ruleNode.has("type")) {
						String ruleType = ruleNode.get("type").asText();

						if (ruleType.equals("min-max")) {
							MinMaxRule minMaxRule = new MinMaxRule(
									ruleNode.get("min").asInt(0),
									ruleNode.get("max").asInt(0),
									ruleNode.get("fieldId").asInt()
							);

							quiz.addInputRule(minMaxRule);
						}
					}
				}
			}
		}
		catch (Throwable e) {
			throw new IOException("Deserialize quiz failed.", e);
		}

		return quiz;
	}
}
