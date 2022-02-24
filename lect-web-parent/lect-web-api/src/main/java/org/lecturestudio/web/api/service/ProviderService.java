package org.lecturestudio.web.api.service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.net.SSLContextFactory;

public abstract class ProviderService {

	protected ServiceParameters parameters;


	protected RestClientBuilder createClientBuilder(ServiceParameters parameters) {
		RestClientBuilder builder = RestClientBuilder.newBuilder();
		builder.baseUri(URI.create(parameters.getUrl()));
		builder.connectTimeout(12, TimeUnit.SECONDS);

		if (parameters.getUrl().startsWith("https")) {
			builder.sslContext(SSLContextFactory.createSSLContext());
			builder.hostnameVerifier((hostname, sslSession) -> hostname
					.equalsIgnoreCase(sslSession.getPeerHost()));
		}

		return builder;
	}
}
