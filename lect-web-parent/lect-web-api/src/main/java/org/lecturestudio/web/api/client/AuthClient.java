package org.lecturestudio.web.api.client;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/api/auth")
public interface AuthClient {

	@POST
	String authenticate();

}
