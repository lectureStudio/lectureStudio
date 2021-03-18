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

package org.lecturestudio.web.service.validator;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.filter.FilterRule;
import org.lecturestudio.web.api.filter.InputFieldRule;
import org.lecturestudio.web.api.filter.RegexFilter;
import org.lecturestudio.web.api.model.ClassroomServiceResponse;
import org.lecturestudio.web.api.model.ClassroomServiceResponse.Status;
import org.lecturestudio.web.api.model.QuizService;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;

@ApplicationScoped
public class QuizAnswerValidator {

	public Response validate(HttpServletRequest request, QuizService quizService, QuizAnswer quizAnswer) {
		Response.ResponseBuilder responseBuilder;

		if (isNull(quizService)) {
			ClassroomServiceResponse serviceResponse = new ClassroomServiceResponse();
			serviceResponse.statusCode = Status.ERROR.getCode();
			serviceResponse.statusMessage = "service.absent";

			responseBuilder = Response.status(Response.Status.BAD_REQUEST);
			responseBuilder.entity(serviceResponse);
		}
		else {
			String remoteAddress = request.getRemoteAddr();
			Map<Integer, String> fieldErrors = new HashMap<>();

			try {
				validateRemoteAddress(quizService, remoteAddress);
				validateServiceId(quizService, quizAnswer.getServiceId());
				validateInputFields(quizService, quizAnswer.getOptions(), fieldErrors);

				ClassroomServiceResponse serviceResponse = new ClassroomServiceResponse();
				serviceResponse.statusCode = Status.SUCCESS.getCode();
				serviceResponse.statusMessage = "quiz.answer.sent";

				responseBuilder = Response.status(Response.Status.OK);
				responseBuilder.entity(serviceResponse);
			}
			catch (Exception e) {
				ClassroomServiceResponse serviceResponse = new ClassroomServiceResponse();
				serviceResponse.statusCode = Status.DATA_ERROR.getCode();
				serviceResponse.statusMessage = e.getMessage();
				serviceResponse.data = fieldErrors;

				responseBuilder = Response.status(Response.Status.BAD_REQUEST);
				responseBuilder.entity(serviceResponse);
			}
		}

		return responseBuilder.build();
	}

	private static void validateRemoteAddress(QuizService quizService, String remoteAddress) throws Exception {
		if (quizService.getHosts().contains(remoteAddress.hashCode())) {
			throw new Exception("quiz.answer.count.error");
		}
	}

	private static void validateServiceId(QuizService quizService, String serviceId) throws Exception {
		String quizServiceId = quizService.getServiceId();

		if (!quizServiceId.equals(serviceId)) {
			throw new Exception("service.absent");
		}
	}

	private static void validateInputFields(QuizService quizService, String[] options, Map<Integer, String> fieldErrors) throws Exception {
		Quiz quiz = quizService.getQuiz();

		if (isNull(options)) {
			options = new String[0];
		}

		if (options.length > quiz.getOptions().size()) {
			throw new Exception("quiz.answer.input.invalid");
		}

		// Check for possible blacklisted input.
		if (quiz.getType() == Quiz.QuizType.NUMERIC) {
			RegexFilter regexFilter = new RegexFilter();
			regexFilter.registerRules(quizService.getRegexRules());

			int fieldId = 0;
			for (String optionValue : options) {
				if (!regexFilter.isAllowedByAll(optionValue)) {
					String error = "quiz.answer.input.error";

					fieldErrors.put(fieldId, error);
				}

				for (FilterRule<String> r : quiz.getInputFilter().getRules()) {
					InputFieldRule<String> rule = (InputFieldRule<String>) r;
					if (!rule.isAllowed(optionValue, fieldId)) {
						String error = "quiz.answer.input.error";

						fieldErrors.put(fieldId, error);
					}
				}
				fieldId++;
			}
		}

		if (!fieldErrors.isEmpty()) {
			throw new Exception("quiz.answer.input.invalid");
		}
	}

}
