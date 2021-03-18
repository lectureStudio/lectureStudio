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

import java.time.Instant;
import java.util.List;

import org.apache.cxf.rs.security.oauth2.common.UserSubject;
import org.apache.cxf.rs.security.oidc.common.IdToken;
import org.apache.cxf.rs.security.oidc.idp.IdTokenProvider;

public class OidcIdTokenProvider implements IdTokenProvider {

	@Override
	public IdToken getIdToken(String clientId, UserSubject authenticatedUser, List<String> scopes) {
		Instant now = Instant.now();

		IdToken token = new IdToken();
		token.setIssuedAt(now.getEpochSecond());
		token.setExpiryTime(now.plusSeconds(60L).getEpochSecond());
		token.setAudience(clientId);
		token.setSubject(authenticatedUser.getLogin());
		token.setIssuer("OIDC IdP");

		return token;
	}
}
