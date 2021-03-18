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

package org.lecturestudio.web.auth.service.oauth2.oidc;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.security.LoginSecurityContext;

public class OidcSecurityContext implements LoginSecurityContext {

	private Principal principal;

	private Subject subject;


	public OidcSecurityContext(String user, Set<String> roles) {
		this.principal = new SimplePrincipal(user);
		this.subject = new Subject();

		for (String role : roles) {
			this.subject.getPrincipals().add(new SimplePrincipal(role));
		}
	}

	@Override
	public Subject getSubject() {
		return subject;
	}

	@Override
	public Set<Principal> getUserRoles() {
		return subject.getPrincipals();
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

	@Override
	public boolean isUserInRole(String role) {
		for (Principal principal : subject.getPrincipals()) {
			if (role.equals(principal.getName())) {
				return true;
			}
		}

		return false;
	}
}
