package org.lecturestudio.broadcast.service;

import io.smallrye.jwt.build.Jwt;

import java.util.Set;

import javax.enterprise.context.RequestScoped;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@RequestScoped
public class AuthResource {

	@POST
	public Response auth() {
		String token = Jwt.issuer("https://lecturestudio.org/issuer")
						.upn("lecturer@lecturestudio.org")
						.groups(Set.of("admin", "lecturer"))
						.sign();

		return Response.ok().entity(token).build();
	}

}
