package org.lecturestudio.web.api.service;

import java.net.URI;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.data.bind.JsonConfig;

public abstract class ProviderService {

	protected ServiceParameters parameters;


	protected <T> T createProxy(Class<T> proxyClass, ServiceParameters parameters) {
		this.parameters = parameters;

		RestClientBuilder builder = RestClientBuilder.newBuilder();
		builder.baseUri(URI.create(parameters.getUrl()));
		builder.register(JsonConfig.class);

		if (parameters.getUrl().startsWith("https")) {
			builder.sslContext(createSSLContext());
			builder.hostnameVerifier((s, sslSession) -> true);
		}

		return builder.build(proxyClass);
	}

	private static SSLContext createSSLContext() {
		TrustManager[] noopTrustManager = new TrustManager[] {
				new X509TrustManager() {

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					@Override
					public void checkClientTrusted(X509Certificate[] certs,
							String authType) {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] certs,
							String authType) {
					}
				}
		};

		SSLContext sslContext;

		try {
			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, noopTrustManager, null);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return sslContext;
	}
}
