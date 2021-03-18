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

package org.lecturestudio.web.auth.service.oauth2;

import static java.util.Objects.isNull;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.rs.security.jose.common.JoseConstants;
import org.apache.cxf.rs.security.oauth2.common.AccessTokenValidation;
import org.apache.cxf.rs.security.oauth2.common.AuthenticationMethod;
import org.apache.cxf.rs.security.oauth2.common.OAuthContext;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.apache.cxf.rs.security.oauth2.filters.OAuthRequestFilter;
import org.apache.cxf.rs.security.oauth2.utils.AuthorizationUtils;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.security.transport.TLSSessionInfo;

/**
 * JAX-RS OAuth2 filter which protects end-user endpoints.
 *
 * @author Alex Andres
 */
public class OAuth2RequestFilter extends OAuthRequestFilter {

	private static final Logger LOG = LogUtils.getL7dLogger(OAuth2RequestFilter.class);

	private static final String AUTH_SCHEME = OAuthConstants.BEARER_AUTHORIZATION_SCHEME;

	private List<String> requiredScopes = Collections.emptyList();

	private String issuer;

	private boolean allPermissionsMatch;

	private boolean blockPublicClients;

	private AuthenticationMethod am;


	@Override
	public void filter(ContainerRequestContext context) {
		Map<String, Cookie> cookies = context.getCookies();

		Cookie accessCookie = cookies.get(OAuthConstants.ACCESS_TOKEN);

		if (isNull(accessCookie)) {
			super.filter(context);
			return;
		}

		String accessToken = accessCookie.getValue();

		if (isNull(accessToken)) {
			super.filter(context);
			return;
		}

		String[] authParts = new String[] { AUTH_SCHEME, accessToken };

		// Get the access token
		AccessTokenValidation accessTokenV = getAccessTokenValidation(AUTH_SCHEME, accessToken, null);
		if (!accessTokenV.isInitialValidationSuccessful()) {
			AuthorizationUtils.throwAuthorizationFailure(supportedSchemes, realm);
		}
		// Check audiences
		String validAudience = validateAudiences(accessTokenV.getAudiences());

		// Check if token was issued by the supported issuer
		if (issuer != null && !issuer.equals(accessTokenV.getTokenIssuer())) {
			AuthorizationUtils.throwAuthorizationFailure(supportedSchemes, realm);
		}
		// Find the scopes which match the current request

		List<OAuthPermission> permissions = accessTokenV.getTokenScopes();
		List<OAuthPermission> matchingPermissions = new ArrayList<>();

		HttpServletRequest req = getMessageContext().getHttpServletRequest();
		for (OAuthPermission perm : permissions) {
			boolean uriOK = checkRequestURI(req, perm.getUris());
			boolean verbOK = checkHttpVerb(req, perm.getHttpVerbs());
			boolean scopeOk = checkScopeProperty(perm.getPermission());
			if (uriOK && verbOK && scopeOk) {
				matchingPermissions.add(perm);
			}
		}

		if (!permissions.isEmpty() && matchingPermissions.isEmpty()
				|| allPermissionsMatch && (matchingPermissions.size() != permissions.size())
				|| !requiredScopes.isEmpty() && requiredScopes.size() != matchingPermissions.size()) {
			String message = "Client has no valid permissions";
			LOG.warning(message);
			throw ExceptionUtils.toForbiddenException(null, null);
		}

		if (accessTokenV.getClientIpAddress() != null) {
			String remoteAddress = getMessageContext().getHttpServletRequest().getRemoteAddr();
			if (remoteAddress == null || accessTokenV.getClientIpAddress().equals(remoteAddress)) {
				String message = "Client IP Address is invalid";
				LOG.warning(message);
				throw ExceptionUtils.toForbiddenException(null, null);
			}
		}
		if (blockPublicClients && !accessTokenV.isClientConfidential()) {
			String message = "Only Confidential Clients are supported";
			LOG.warning(message);
			throw ExceptionUtils.toForbiddenException(null, null);
		}
		if (am != null && !am.equals(accessTokenV.getTokenSubject().getAuthenticationMethod())) {
			String message = "The token has been authorized by the resource owner "
					+ "using an unsupported authentication method";
			LOG.warning(message);
			throw ExceptionUtils.toNotAuthorizedException(null, null);

		}
		// Check Client Certificate Binding if any
		String certThumbprint = accessTokenV.getExtraProps().get(JoseConstants.HEADER_X509_THUMBPRINT_SHA256);
		if (certThumbprint != null) {
			TLSSessionInfo tlsInfo = getTlsSessionInfo();
			X509Certificate cert = tlsInfo == null ? null : OAuthUtils.getRootTLSCertificate(tlsInfo);
			if (cert == null || !OAuthUtils.compareCertificateThumbprints(cert, certThumbprint)) {
				throw ExceptionUtils.toNotAuthorizedException(null, null);
			}
		}

		// Create the security context and make it available on the message
		SecurityContext sc = createSecurityContext(req, accessTokenV);

		Message message = JAXRSUtils.getCurrentMessage();
		message.put(SecurityContext.class, sc);

		// Also set the OAuthContext
		OAuthContext oauthContext = new OAuthContext(accessTokenV.getTokenSubject(),
				accessTokenV.getClientSubject(),
				matchingPermissions,
				accessTokenV.getTokenGrantType());

		oauthContext.setClientId(accessTokenV.getClientId());
		oauthContext.setClientConfidential(accessTokenV.isClientConfidential());
		oauthContext.setTokenKey(accessTokenV.getTokenKey());
		oauthContext.setTokenAudience(validAudience);
		oauthContext.setTokenIssuer(accessTokenV.getTokenIssuer());
		oauthContext.setTokenRequestParts(authParts);
		oauthContext.setTokenExtraProperties(accessTokenV.getExtraProps());

		message.setContent(OAuthContext.class, oauthContext);
	}

	private TLSSessionInfo getTlsSessionInfo() {
		return (TLSSessionInfo) getMessageContext().get(TLSSessionInfo.class.getName());
	}

	public void setRequiredScopes(List<String> requiredScopes) {
		super.setRequiredScopes(requiredScopes);

		this.requiredScopes = requiredScopes;
	}

	public void setAllPermissionsMatch(boolean allPermissionsMatch) {
		super.setAllPermissionsMatch(allPermissionsMatch);

		this.allPermissionsMatch = allPermissionsMatch;
	}

	public void setBlockPublicClients(boolean blockPublicClients) {
		super.setBlockPublicClients(blockPublicClients);

		this.blockPublicClients = blockPublicClients;
	}

	public void setIssuer(String issuer) {
		super.setIssuer(issuer);

		this.issuer = issuer;
	}

	public void setTokenSubjectAuthenticationMethod(AuthenticationMethod method) {
		super.setTokenSubjectAuthenticationMethod(method);

		this.am = method;
	}
}
