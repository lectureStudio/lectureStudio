package org.lecturestudio.web.api.service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.data.bind.JsonConfig;
import org.lecturestudio.web.api.net.OwnTrustManager;

public abstract class ProviderService {

	protected ServiceParameters parameters;


	protected <T> T createProxy(Class<T> proxyClass, ServiceParameters parameters) {
		this.parameters = parameters;

		RestClientBuilder builder = RestClientBuilder.newBuilder();
		builder.baseUri(URI.create(parameters.getUrl()));
		builder.register(JsonConfig.class);
		builder.connectTimeout(2, TimeUnit.SECONDS);

		if (parameters.getUrl().startsWith("https")) {
			builder.sslContext(createSSLContext());
			builder.hostnameVerifier((hostname, sslSession) -> hostname
					.equalsIgnoreCase(sslSession.getPeerHost()));
		}

		return builder.build(proxyClass);
	}

	private static SSLContext createSSLContext() {
		SSLContext sslContext;

		try {
			X509TrustManager trustManager = new OwnTrustManager("keystore.jks",
					"mypassword");

			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new TrustManager[] { trustManager }, null);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return sslContext;
	}
}
