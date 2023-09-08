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

package org.lecturestudio.web.api.websocket;

import static java.util.Objects.nonNull;

import java.net.http.WebSocket.Builder;

import org.lecturestudio.web.api.client.TokenProvider;

/**
 * Bearer token authorization header provider.
 *
 * @author Alex Andres
 */
public class WebSocketBearerTokenProvider implements WebSocketHeaderProvider {

	protected static final String API_KEY_HEADER = "ApiKey";

	protected final TokenProvider tokenProvider;


	public WebSocketBearerTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void setHeaders(Builder builder) {
		String token = tokenProvider.getToken();

		if (nonNull(token)) {
			builder.header(API_KEY_HEADER, token);
		}
	}
}
