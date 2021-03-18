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

package org.lecturestudio.web.service.manager;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.lecturestudio.web.api.http.HttpClient;

@Default
@ApplicationScoped
public class ApplicationManagerHttpClient implements ApplicationManagerClient {

	@Context
	private HttpServletRequest request;


	@Override
	public void startWebApp(String contextPath, String appName) throws Exception {
		int port = 80;

		if (request.getServerPort() == 8443) {
			port = 8080;
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put("contextPath", contextPath);
		parameters.put("appName", appName);
		parameters.put("opcode", "start");

		HttpClient httpClient = new HttpClient(new URL("http://127.0.0.1:" + port + "/server/manager"));
		int status = httpClient.post(parameters);

		if (status != 200) {
			throw new Exception("Start web application failed: " + status);
		}
	}

	@Override
	public void stopWebApp(String contextPath) throws Exception {
		int port = 80;

		if (request.getServerPort() == 8443) {
			port = 8080;
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put("contextPath", contextPath);
		parameters.put("opcode", "stop");

		HttpClient httpClient = new HttpClient(new URL("http://127.0.0.1:" + port + "/server/manager"));
		int status = httpClient.post(parameters);

		if (status != 200) {
			throw new Exception("Stop web application failed: " + status);
		}
	}
}
