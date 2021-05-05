package org.lecturestudio.web.api.client;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.lecturestudio.web.api.model.AuthState;

public class AuthHeadersFactory implements ClientHeadersFactory {

	@Override
	public MultivaluedMap<String, String> update(
			MultivaluedMap<String, String> incomingHeaders,
			MultivaluedMap<String, String> outgoingHeaders) {
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.putSingle("Authorization", "Bearer " + AuthState.getInstance().getToken());

		return headers;
	}
}
