package org.lecturestudio.web.api.client;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;

import org.lecturestudio.web.api.model.Classroom;

@Path("/api/classroom")
@RegisterClientHeaders(AuthHeadersFactory.class)
public interface ClassroomProviderClient {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	String createClassroom(Classroom classroom);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateClassroom(Classroom classroom);

	@Path("{classroomId}")
	@DELETE
	Response deleteClassroom(@PathParam("classroomId") String classroomId);

	@GET
	Classroom getClassroom();

	@Path("/list")
	@GET
	List<Classroom> getClassrooms();

}
