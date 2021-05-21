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

package org.lecturestudio.broadcast.service;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.lecturestudio.broadcast.service.validator.QuizAnswerValidator;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.QuizService;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;

@Path("/quiz")
@RequestScoped
public class QuizResource extends ServiceBase {

	private final QuizAnswerValidator quizAnswerValidator;

	@ConfigProperty(name = "quiz.file.whitelist")
	List<String> fileWhitelist;


	@Inject
	QuizResource(QuizAnswerValidator validator) {
		quizAnswerValidator = validator;
	}

	@Context
	public void setSse(Sse sse) {
		setSseEventBuilder(sse.newEventBuilder());
	}

	@Path("start/{classroomId}")
	@POST
	@RolesAllowed({ "admin", "lecturer" })
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public Response startService(@PathParam("classroomId") String classroomId,
			Quiz quiz) throws Exception {
		QuizService service = new QuizService();
		service.setQuiz(quiz);

		return super.startService(classroomId, service);
	}

	@Path("stop/{classroomId}/{serviceId}")
	@POST
	@RolesAllowed({ "admin", "lecturer" })
	@Transactional
	public Response stopService(@PathParam("classroomId") String classroomId,
			@PathParam("serviceId") String serviceId) throws Exception {
		return super.stopService(classroomId, serviceId, QuizService.class);
	}

	@Path("subscribe/{serviceId}")
	@GET
	@RolesAllowed({ "admin", "lecturer" })
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void subscribe(@Context SseEventSink eventSink,
			@PathParam("serviceId") String serviceId) {
		registerSseSink(eventSink, serviceId, QuizService.class);
	}

	@Path("post")
	@POST
	@PermitAll
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postQuizAnswer(@Context HttpServletRequest request,
			QuizAnswer quizAnswer) {
		String serviceId = quizAnswer.getServiceId();

		Classroom classroom = classroomDataService.getByContextPath("");
		QuizService service = classroom.getServices()
				.stream()
				.filter(QuizService.class::isInstance)
				.map(QuizService.class::cast)
				.findFirst().orElse(null);

		// Validate input.
		Response response = quizAnswerValidator.validate(request, service, quizAnswer);

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			String remoteAddress = request.getRemoteAddr();

			service.getHosts().add(remoteAddress.hashCode());

			// Notify service provider endpoint.
			sendWebMessage(new QuizAnswerMessage(quizAnswer, remoteAddress),
					serviceId);
		}

		return response;
	}

	private boolean inWhiteList(String fileName) {
		for (String ext : fileWhitelist) {
			if (fileName.toLowerCase().endsWith("." + ext)) {
				return true;
			}
		}
		return false;
	}
}
