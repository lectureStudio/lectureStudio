package org.lecturestudio.web.api.service;

import io.smallrye.jwt.build.Jwt;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class JwtRequestFilter implements ClientRequestFilter {

	private final String token;


	@Inject
	public JwtRequestFilter() {
		this.token = Jwt.issuer("https://lecturestudio.org/issuer")
				.upn("lecturer@lecturestudio.org")
				.groups(Set.of("admin", "lecturer"))
				.sign();
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		requestContext.getHeaders().add("Authorization", "Bearer " + token);
	}
}
