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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import org.apache.cxf.BusFactory;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.OAuthPermission;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.grants.code.DefaultEHCacheCodeDataProvider;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.rs.security.oauth2.tokens.refresh.RefreshToken;

@Default
@ApplicationScoped
public class OAuth2DataProvider extends DefaultEHCacheCodeDataProvider {

	public OAuth2DataProvider() {
		super(DEFAULT_CONFIG_URL, BusFactory.getThreadDefaultBus(true),
				CLIENT_CACHE_KEY + "_" + Math.abs(new Random().nextInt()),
				CODE_GRANT_CACHE_KEY + "_" + Math.abs(new Random().nextInt()),
				ACCESS_TOKEN_CACHE_KEY + "_" + Math.abs(new Random().nextInt()),
				REFRESH_TOKEN_CACHE_KEY + "_" + Math.abs(new Random().nextInt()));

		setUseJwtFormatForAccessTokens(true);

		// filters/grants test client
		Client client = new Client("consumer-id", "this-is-a-secret", true);
		client.getRedirectUris().add("https://localhost/classroom/callback");

		client.getAllowedGrantTypes().add("authorization_code");
		client.getAllowedGrantTypes().add("refresh_token");

		client.getRegisteredScopes().add("read_balance");
		client.getRegisteredScopes().add("openid");

		setClient(client);
	}

	@Override
	public List<OAuthPermission> convertScopeToPermissions(Client client, List<String> requestedScopes) {
		if (requestedScopes.isEmpty()) {
			return Collections.emptyList();
		}

		List<OAuthPermission> permissions = new ArrayList<>();

		for (String requestedScope : requestedScopes) {
			if ("read_balance".equals(requestedScope)) {
				OAuthPermission permission = new OAuthPermission("read_balance", "Read balance");
				permission.setHttpVerbs(Collections.singletonList("GET"));
				List<String> uris = new ArrayList<>();
				uris.add("/classroom/test");
				permission.setUris(uris);

				permissions.add(permission);
			}
			else if ("openid".equals(requestedScope)) {
				OAuthPermission permission = new OAuthPermission("openid", "Authenticate user");
				permissions.add(permission);
			}
			else {
				throw new OAuthServiceException("invalid_scope");
			}
		}

		return permissions;
	}

	@Override
	protected boolean isRefreshTokenSupported(List<String> theScopes) {
		return true;
	}

	@Override
	protected ServerAccessToken doRefreshAccessToken(Client client, RefreshToken oldRefreshToken, List<String> requestedScopes) {
		ServerAccessToken at = super.doRefreshAccessToken(client, oldRefreshToken, requestedScopes);

		if (isUseJwtFormatForAccessTokens()) {
			JwtClaims claims = createJwtAccessToken(at);
			String jose = processJwtAccessToken(claims);
			at.setTokenKey(jose);
		}

		return at;
	}

	@Override
	protected JwtClaims createJwtAccessToken(ServerAccessToken at) {
		JwtClaims claims = super.createJwtAccessToken(at);
		claims.setClaim("roles", at.getSubject().getRoles());

		return claims;
	}

}
