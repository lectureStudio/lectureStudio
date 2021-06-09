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

package org.lecturestudio.web.api.janus;

import javax.json.bind.annotation.JsonbProperty;

/**
 * Information about the Janus instance retrieved from the {@code info}
 * endpoint.
 *
 * @author Alex Andres
 */
public class JanusInfo {

	@JsonbProperty("session-timeout")
	private final int sessionTimeout;

	@JsonbProperty("api_secret")
	private final boolean apiSecret;

	@JsonbProperty("auth_token")
	private final boolean authToken;


	public JanusInfo(boolean apiSecret, boolean authToken, int sessionTimeout) {
		this.apiSecret = apiSecret;
		this.authToken = authToken;
		this.sessionTimeout = sessionTimeout;
	}

	/**
	 * Get the session timeout in seconds. Within this time period a keep-alive
	 * message must be sent to the Janus instance to keep the session open.
	 *
	 * @return The session timeout in seconds.
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * Check if an API secret is required.
	 *
	 * @return True if an API secret is required.
	 */
	public boolean hasApiSecret() {
		return apiSecret;
	}

	/**
	 * Check if an auth token is required.
	 *
	 * @return True if an auth token is required.
	 */
	public boolean hasAuthToken() {
		return authToken;
	}
}
