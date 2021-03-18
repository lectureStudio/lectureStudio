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

package org.lecturestudio.web.api.ws.rs;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.lecturestudio.web.api.model.ClassroomServiceResponse;
import org.lecturestudio.web.api.ws.ConnectionParameters;

public abstract class RestClientBase {

	private final SSLContext sslContext;

	private final ConnectionParameters parameters;

	private final String baseUri;


	public RestClientBase(ConnectionParameters parameters, String servicePath) throws Exception {
		this.parameters = parameters;
		this.baseUri = parameters.getDomainUrl() + servicePath;
		this.sslContext = parameters.enableTls() ? createSSLContext() : null;
	}

	protected WebTarget getWebTarget(Class<?>... providerClasses) {
		Client client;

		if (parameters.enableTls()) {
			HostnameVerifier verifier = (s, sslSession) -> true;

			client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(verifier).build();
		}
		else {
			client = ClientBuilder.newClient();
		}

		for (Class<?> providerClass : providerClasses) {
			client.register(providerClass);
		}

		return client.target(UriBuilder.fromUri(baseUri).build());
	}

	protected void checkForError(Response response) throws Exception {
		if (response.getStatus() != Status.OK.getStatusCode()) {
			if (!response.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
				throw new Exception(response.readEntity(String.class));
			}

			ClassroomServiceResponse error = response.readEntity(ClassroomServiceResponse.class);
			throw new Exception(error.statusMessage);
		}
	}

	protected String responseToString(Response response) throws Exception {
		return response.getStatusInfo().getReasonPhrase();
	}

	private SSLContext createSSLContext() throws Exception {
		TrustManager[] noopTrustManager = new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, noopTrustManager, null);

		return sslContext;
	}
}
