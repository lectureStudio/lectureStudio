package org.lecturestudio.web.api.service;

import static java.util.Objects.nonNull;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class WebClientBuilder {

	private Class<?>[] componentClasses;

	private boolean tls;


	public WebClientBuilder setTls(boolean tls) {
		this.tls = tls;
		return this;
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

		if (tls) {
			builder.sslContext(createSSLContext());
			builder.hostnameVerifier((s, sslSession) -> true);
		}

		return builder.build();
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
