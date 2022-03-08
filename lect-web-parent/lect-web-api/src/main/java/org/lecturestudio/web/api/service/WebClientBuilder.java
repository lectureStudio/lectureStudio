package org.lecturestudio.web.api.service;

import static java.util.Objects.nonNull;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.net.SSLContextFactory;

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
			builder.sslContext(SSLContextFactory.createSSLContext());
			builder.hostnameVerifier((s, sslSession) -> true);
		}

		return builder.build();
	}

}
