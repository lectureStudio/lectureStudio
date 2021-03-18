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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.lecturestudio.web.auth.model.UserAccounts;
import org.lecturestudio.web.auth.service.AuthApplication;
import org.lecturestudio.web.auth.service.oauth2.oidc.OidcIdTokenProvider;

import org.apache.cxf.rs.security.jose.jaxrs.JsonWebKeysProvider;
import org.apache.cxf.rs.security.oauth2.grants.refresh.RefreshTokenGrantHandler;
import org.apache.cxf.rs.security.oauth2.services.AccessTokenService;
import org.apache.cxf.rs.security.oidc.idp.IdTokenResponseFilter;
import org.apache.cxf.rs.security.oidc.idp.OidcAuthorizationCodeService;
import org.apache.cxf.rs.security.oidc.idp.OidcKeysService;

@ApplicationPath("/oauth2")
@ApplicationScoped
public class OAuth2Application extends AuthApplication {

	@Inject
	private OAuth2DataProvider manager;

	@Inject
	private UserAccounts accounts;


	@Override
	public Set<Object> getSingletons() {
		Set<Object> classes = new HashSet<>();

		OidcAuthorizationCodeService oidcAuthorizationService = new OidcAuthorizationCodeService();
		oidcAuthorizationService.setDataProvider(manager);

		IdTokenResponseFilter idTokenFilter = new IdTokenResponseFilter();
		idTokenFilter.setIdTokenProvider(new OidcIdTokenProvider());

		RefreshTokenGrantHandler refreshTokenHandler = new RefreshTokenGrantHandler();
		refreshTokenHandler.setDataProvider(manager);

		AccessTokenService tokenService = new AccessTokenService();
		tokenService.setDataProvider(manager);
		tokenService.setGrantHandler(refreshTokenHandler);
		tokenService.setResponseFilter(idTokenFilter);

		OidcKeysService oidcKeysService = new OidcKeysService();

		OAuth2SecurityContextFilter securityFilter = new OAuth2SecurityContextFilter();
		securityFilter.setAccounts(accounts);

		classes.add(securityFilter);
		classes.add(oidcAuthorizationService);
		classes.add(oidcKeysService);
		classes.add(tokenService);
		classes.add(new JsonWebKeysProvider());

		return classes;
	}
}
