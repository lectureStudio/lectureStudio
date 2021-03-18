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
import static java.util.Objects.nonNull;

import java.security.Principal;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.lecturestudio.web.auth.model.UserAccount;
import org.lecturestudio.web.auth.model.UserAccounts;
import org.lecturestudio.web.auth.service.oauth2.oidc.OidcSecurityContext;

import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;

@Provider
public class OAuth2SecurityContextFilter implements ContainerRequestFilter {

	@Context
	private HttpHeaders headers;

	private UserAccounts accounts;


	public void setAccounts(UserAccounts accounts) {
		this.accounts = accounts;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		Message message = JAXRSUtils.getCurrentMessage();
		SecurityContext sc = message.get(SecurityContext.class);

		if (nonNull(sc)) {
			Principal principal = sc.getUserPrincipal();

			if (nonNull(principal)) {
				String accountName = principal.getName();
				UserAccount account = accounts.getAccount(accountName);

				if (isNull(account)) {
					requestContext.abortWith(createFaultResponse());
				}
				else {
					setNewSecurityContext(message, account);
				}
				return;
			}
		}

		List<String> authValues = headers.getRequestHeader("Authorization");

		if (isNull(authValues) || authValues.size() != 1) {
			requestContext.abortWith(createFaultResponse());
			return;
		}

		String[] values = authValues.get(0).split(" ");

		if (values.length != 2 || !"Basic".equals(values[0])) {
			requestContext.abortWith(createFaultResponse());
			return;
		}

		String decodedValue;

		try {
			decodedValue = new String(Base64Utility.decode(values[1]));
		}
		catch (Base64Exception ex) {
			requestContext.abortWith(createFaultResponse());
			return;
		}

		String[] namePassword = decodedValue.split(":");

		if (namePassword.length != 2) {
			requestContext.abortWith(createFaultResponse());
			return;
		}

		final UserAccount account = accounts.getAccount(namePassword[0]);

		if (isNull(account) || !account.getPassword().equals(namePassword[1])) {
			requestContext.abortWith(createFaultResponse());
			return;
		}

		setNewSecurityContext(message, account);
	}

	private void setNewSecurityContext(Message message, final UserAccount account) {
		message.put(SecurityContext.class, new OidcSecurityContext(account.getName(), account.getRoles()));
	}

	private Response createFaultResponse() {
		return Response.status(401).header("WWW-Authenticate", "Basic realm='Authentication Required'").build();
	}

}
