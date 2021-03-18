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

package org.lecturestudio.web.service.model;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import org.lecturestudio.web.api.connector.RelayConnectors;
import org.lecturestudio.web.api.model.ClassroomService;

@Default
@ApplicationScoped
public class WebSessions {

	private final Map<KeyPair, RelayConnectors> sessions;


	public WebSessions() {
		sessions = new ConcurrentHashMap<>();
	}

	public void addWebSession(KeyPair key, RelayConnectors session) {
		sessions.put(key, session);
	}

	public void removeWebSession(KeyPair key) {
		sessions.remove(key);
	}

	public RelayConnectors getWebSession(KeyPair key) {
		return sessions.get(key);
	}



	public static class KeyPair {

		private final String contextPath;

		private final Class<? extends ClassroomService> serviceClass;


		public KeyPair(String contextPath, Class<? extends ClassroomService> serviceClass) {
			this.contextPath = contextPath;
			this.serviceClass = serviceClass;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			KeyPair keyPair = (KeyPair) o;

			return Objects.equals(contextPath, keyPair.contextPath) && Objects.equals(serviceClass, keyPair.serviceClass);
		}

		@Override
		public int hashCode() {
			return Objects.hash(contextPath, serviceClass);
		}
	}
}
