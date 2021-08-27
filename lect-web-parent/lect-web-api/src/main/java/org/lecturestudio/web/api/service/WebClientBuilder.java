package org.lecturestudio.web.api.service;

import static java.util.Objects.nonNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.net.OwnTrustManager;

public class WebClientBuilder {

	private TokenProvider tokenProvider;

	private Class<?>[] componentClasses;

	private boolean tls;


	public WebClientBuilder setTls(boolean tls) {
		this.tls = tls;
		return this;
	}

	public void setTokenProvider(TokenProvider provider) {
		this.tokenProvider = provider;
	}

	public WebClientBuilder setComponentClasses(Class<?>... componentClasses) {
		this.componentClasses = componentClasses;
		return this;
	}

	public Client build() {
		ClientBuilder builder = ClientBuilder.newBuilder();

		if (nonNull(componentClasses) && componentClasses.length > 0) {
			for (Class<?> cls : componentClasses) {
				builder.register(cls);
			}
		}

		if (nonNull(tokenProvider)) {
			builder.property(TokenProvider.class.getName(), tokenProvider);
		}

		if (tls) {
			builder.sslContext(createSSLContext());
			builder.hostnameVerifier((s, sslSession) -> true);
		}

		return builder.build();
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
