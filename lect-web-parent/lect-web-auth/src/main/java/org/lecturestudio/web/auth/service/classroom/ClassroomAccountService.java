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

package org.lecturestudio.web.auth.service.classroom;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.lecturestudio.web.auth.model.UserAccounts;
import org.lecturestudio.web.auth.service.oauth2.client.filter.AuthenticatorFilter;
import org.lecturestudio.web.auth.service.oauth2.client.filter.LoggingFilter;
import org.lecturestudio.web.auth.service.oauth2.oidc.flow.AuthCodeFlow;

import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;
import org.apache.cxf.rs.security.oauth2.common.OAuthAuthorizationData;
import org.apache.cxf.rs.security.oidc.utils.OidcUtils;

@Path("account")
public class ClassroomAccountService {

	@Context
	private SecurityContext context;

	private UserAccounts accounts;


	public void setAccounts(UserAccounts accounts) {
		this.accounts = accounts;
	}

	@Path("login")
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login() throws Exception
	{
		AuthCodeFlow authCodeFlow = new AuthCodeFlow();
		authCodeFlow.addClientResponseFilter(new LoggingFilter());
		authCodeFlow.addClientRequestFilter(new AuthenticatorFilter("admin", "admin"));
		authCodeFlow.setAuthorizeEndpoint("https://localhost:" + 443 + "/bcast/auth/oauth2/authorize/");
		authCodeFlow.setTokenEndpoint("https://localhost:" + 443 + "/bcast/auth/oauth2/token");
		authCodeFlow.setClientId("consumer-id");
		authCodeFlow.setRedirectUri("https://localhost/classroom/callback");
		authCodeFlow.setState("init");

		OAuthAuthorizationData authData = authCodeFlow.authorize();

		authCodeFlow.decide(authData, true);

		ClientAccessToken accessToken = authCodeFlow.getAccessToken();
		String idToken = accessToken.getParameters().get(OidcUtils.ID_TOKEN);

		return Response.ok()
				.entity(accessToken)
				.cookie(createCookie("access_token", accessToken.getTokenKey()))
				.cookie(createCookie("id_token", idToken))
				.build();
	}

	private static NewCookie createCookie(String name, String value) {
		return new NewCookie(name, value, "/", null, null, -1, true, true);
	}

}
