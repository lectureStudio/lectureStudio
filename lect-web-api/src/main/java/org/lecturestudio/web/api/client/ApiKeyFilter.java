/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.client;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiKeyFilter implements ClientRequestFilter {

	private static final String API_KEY_HEADER = "ApiKey";


	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		TokenProvider tokenProvider = (TokenProvider) requestContext
				.getConfiguration().getProperty(TokenProvider.class.getName());

		if (isNull(tokenProvider)) {
			return;
		}

		String token = tokenProvider.getToken();

		if (nonNull(token)) {
			requestContext.getHeaders().putSingle(API_KEY_HEADER, token);
		}
	}
}
