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

package org.lecturestudio.web.api.ws;

import java.util.Objects;

public class ConnectionParameters {

	private final String domainUrl;

	private final boolean enableTls;


	public ConnectionParameters(String host, int port, boolean enableTls) {
		String scheme = enableTls ? "https" : "http";

		this.enableTls = enableTls;
		this.domainUrl = scheme + "://" + host + ":" + port;
	}

	public String getDomainUrl() {
		return domainUrl;
	}

	public boolean enableTls() {
		return enableTls;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ConnectionParameters that = (ConnectionParameters) o;

		return enableTls == that.enableTls && Objects.equals(domainUrl, that.domainUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainUrl, enableTls);
	}
}
