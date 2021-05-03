package org.lecturestudio.web.api.client;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.jwt.build.Jwt;

import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@RegisterForReflection
public class AuthHeadersFactory implements ClientHeadersFactory {

	@Override
	public MultivaluedMap<String, String> update(
			MultivaluedMap<String, String> incomingHeaders,
			MultivaluedMap<String, String> outgoingHeaders) {
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.putSingle("Authorization",
				"Bearer " + Jwt.issuer("https://lecturestudio.org/issuer")
						.upn("lecturer@lecturestudio.org")
						.groups(Set.of("admin", "lecturer")).sign());

		return headers;
	}
}
