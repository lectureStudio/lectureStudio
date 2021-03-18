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

package org.lecturestudio.web.auth.service.oauth2.oidc.flow;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.rs.security.jose.jwa.SignatureAlgorithm;
import org.apache.cxf.rs.security.jose.jws.JwsJwtCompactConsumer;
import org.apache.cxf.rs.security.jose.jwt.JwtConstants;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oidc.common.IdToken;
import org.apache.cxf.rs.security.oidc.utils.OidcUtils;

public abstract class AuthFlow {

	private final List<ClientResponseFilter> responseFilters;

	private final List<ClientRequestFilter> requestFilters;

	private List<String> scopes;

	protected String authorizeEndpoint;

	protected String tokenEndpoint;


	public AuthFlow() {
		this.responseFilters = new ArrayList<>();
		this.requestFilters = new ArrayList<>();
	}

	public void addClientResponseFilter(ClientResponseFilter filter) {
		this.responseFilters.add(filter);
	}

	public void addClientRequestFilter(ClientRequestFilter filter) {
		this.requestFilters.add(filter);
	}

	public ClientAccessToken refreshAccessToken(String refreshToken) throws Exception {
		Form form = new Form();
		form.param(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN_GRANT);
		form.param(OAuthConstants.REFRESH_TOKEN, refreshToken);
		form.param(OAuthConstants.SCOPE, getScopeString());

		Response response = post(tokenEndpoint, form, null);

		ClientAccessToken accessToken = response.readEntity(ClientAccessToken.class);

		validateAccessToken(accessToken);

		return accessToken;
	}

	public void setAuthorizeEndpoint(String uri) {
		this.authorizeEndpoint = uri;
	}

	public void setTokenEndpoint(String uri) {
		this.tokenEndpoint = uri;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	protected void validateAccessToken(ClientAccessToken accessToken) throws Exception {
		String idToken = accessToken.getParameters().get(OidcUtils.ID_TOKEN);

		validateIdToken(idToken, null);
	}

	protected String getScopeString() {
		StringJoiner scopesJoiner = new StringJoiner(" ");

		scopes.forEach(scopesJoiner::add);

		return scopesJoiner.toString();
	}

	protected static String getSubstring(String parentString, String substringName) {
		if (!parentString.contains(substringName)) {
			return null;
		}

		String foundString = parentString.substring(parentString.indexOf(substringName + "=") + (substringName + "=").length());

		int ampersandIndex = foundString.indexOf('&');
		if (ampersandIndex < 1) {
			ampersandIndex = foundString.length();
		}

		return foundString.substring(0, ampersandIndex);
	}

	protected void validateIdToken(String idToken, String nonce) throws Exception {
		JwsJwtCompactConsumer jwtConsumer = new JwsJwtCompactConsumer(idToken);
		JwtToken jwt = jwtConsumer.getJwtToken();

		// Validate claims
		if (isNull(jwt.getClaim(JwtConstants.CLAIM_EXPIRY))) {
			throw new Exception("Missing expiry claim");
		}
		if (isNull(jwt.getClaim(JwtConstants.CLAIM_ISSUED_AT))) {
			throw new Exception("Missing issued at claim");
		}
		if (nonNull(nonce) && !nonce.equals(jwt.getClaim(IdToken.NONCE_CLAIM))) {
			throw new Exception("Nonce mismatch");
		}

		// Validate signature
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(this.getClass().getResourceAsStream("/alice.jks"), "password".toCharArray());

		Certificate cert = keystore.getCertificate("alice");

		if (isNull(cert)) {
			throw new NullPointerException("Missing certificate");
		}

		if (!jwtConsumer.verifySignatureWith((X509Certificate)cert, SignatureAlgorithm.RS256)) {
			throw new Exception("Invalid signature");
		}
	}

	protected Response get(String uri, Map<String, String> queryStrings, Cookie cookie) throws Exception {
		Function<WebTarget, Response> function = webTarget -> {
			if (nonNull(queryStrings)) {
				for (String key : queryStrings.keySet()) {
					String value = queryStrings.get(key);
					webTarget = webTarget.queryParam(key, value);
				}
			}

			Invocation.Builder request = webTarget.request();

			if (nonNull(cookie)) {
				request.cookie(cookie);
			}

			return request.accept(MediaType.APPLICATION_JSON_TYPE).get();
		};

		return execute(uri, function);
	}

	protected Response post(String uri, Form form, Cookie cookie) throws Exception {
		Function<WebTarget, Response> function = webTarget -> {
			Invocation.Builder request = webTarget.request();

			if (nonNull(cookie)) {
				request.cookie(cookie);
			}

			return request.post(Entity.form(form));
		};

		return execute(uri, function);
	}

	private Response execute(String uri, Function<WebTarget, Response> func) throws Exception {
		HostnameVerifier verifier = (s, sslSession) -> true;
		SSLContext sslContext = createSSLContext();

		Client client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(verifier).build();

		responseFilters.forEach(client::register);
		requestFilters.forEach(client::register);

		WebTarget target = client.target(UriBuilder.fromUri(uri).build());

		return func.apply(target);
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
