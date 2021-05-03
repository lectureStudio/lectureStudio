package org.lecturestudio.web.api.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;

@Path("/api/message")
@RegisterClientHeaders(AuthHeadersFactory.class)
public interface MessageProviderClient {

	@Path("/start/{classroomId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	String startMessenger(@PathParam("classroomId") String classroomId);

	@Path("/stop/{classroomId}/{serviceId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	void stopMessenger(@PathParam("classroomId") String classroomId,
			@PathParam("serviceId") String serviceId);

}
