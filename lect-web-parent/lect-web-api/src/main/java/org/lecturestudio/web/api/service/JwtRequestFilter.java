package org.lecturestudio.web.api.service;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.lecturestudio.web.api.model.AuthState;

public class JwtRequestFilter implements ClientRequestFilter {

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		requestContext.getHeaders().add("Authorization", "Bearer " + AuthState
				.getInstance().getToken());
	}
}
