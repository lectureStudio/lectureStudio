package org.lecturestudio.web.api.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.lecturestudio.web.api.model.quiz.Quiz;

@Path("/api/quiz")
@RegisterClientHeaders(AuthHeadersFactory.class)
public interface QuizProviderClient {

	@Path("/start/{classroomId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	String startQuiz(@PathParam("classroomId") String classroomId, Quiz quiz);

	@Path("/stop/{classroomId}/{serviceId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	void stopQuiz(@PathParam("classroomId") String classroomId,
			@PathParam("serviceId") String serviceId);

}
