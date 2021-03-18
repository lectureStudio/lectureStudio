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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.common.OAuthAuthorizationData;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oidc.utils.OidcUtils;

public class AuthCodeFlow extends AuthFlow {

	private String clientId;

	private String responseType;

	private String redirectUri;

	private String state;

	private String code;

	private Cookie sessionId;


	public AuthCodeFlow() {
		setResponseType(OAuthConstants.CODE_RESPONSE_TYPE);
		setScopes(new ArrayList<>());

		getScopes().add(OidcUtils.getOpenIdScope());
	}

	public OAuthAuthorizationData authorize() throws Exception {
		Map<String, String> query = new HashMap<>();
		query.put(OAuthConstants.CLIENT_ID, clientId);
		query.put(OAuthConstants.RESPONSE_TYPE, responseType);
		query.put(OAuthConstants.REDIRECT_URI, redirectUri);
		query.put(OAuthConstants.SCOPE, getScopeString());

		if (nonNull(state)) {
			query.put(OAuthConstants.STATE, state);
		}

		Response response = get(authorizeEndpoint, query, null);

		OAuthAuthorizationData authData = response.readEntity(OAuthAuthorizationData.class);

		sessionId = response.getCookies().get("JSESSIONID");

		return authData;
	}

	public void decide(OAuthAuthorizationData authData, boolean decision) throws Exception {
		String decisionStr = decision ? OAuthConstants.AUTHORIZATION_DECISION_ALLOW : OAuthConstants.AUTHORIZATION_DECISION_DENY;

		Form form = new Form();
		form.param(OAuthConstants.CLIENT_ID, authData.getClientId());
		form.param(OAuthConstants.RESPONSE_TYPE, authData.getResponseType());
		form.param(OAuthConstants.REDIRECT_URI, authData.getRedirectUri());
		form.param(OAuthConstants.SCOPE, authData.getProposedScope());
		form.param(OAuthConstants.STATE, authData.getState());
		form.param(OAuthConstants.AUTHORIZATION_DECISION_KEY, decisionStr);
		form.param(OAuthConstants.SESSION_AUTHENTICITY_TOKEN, authData.getAuthenticityToken());

		Response response = post(authData.getReplyTo(), form, sessionId);

		String location = response.getHeaderString("Location");

		code = getSubstring(location, OAuthConstants.AUTHORIZATION_CODE_VALUE);

		if (isNull(code) || code.isEmpty()) {
			throw new Exception("Invalid code");
		}

		String locationState = getSubstring(location, OAuthConstants.STATE);
		
		if (isNull(locationState) || !locationState.equals(state)) {
			throw new Exception("Invalid state");
		}
	}

	public ClientAccessToken getAccessToken() throws Exception {
		Form form = new Form();
		form.param(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE_GRANT);
		form.param(OAuthConstants.AUTHORIZATION_CODE_VALUE, code);
		form.param(OAuthConstants.CLIENT_ID, clientId);
		form.param(OAuthConstants.REDIRECT_URI, redirectUri);

		Response response = post(tokenEndpoint, form, null);

		ClientAccessToken accessToken = response.readEntity(ClientAccessToken.class);

		validateAccessToken(accessToken);

		return accessToken;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public void setState(String state) {
		this.state = state;
	}

}
