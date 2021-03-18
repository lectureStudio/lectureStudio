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

package org.lecturestudio.broadcast.servlet;

import static java.util.Objects.isNull;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lecturestudio.core.net.ApplicationServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApplicationManagerServlet extends HttpServlet {

	private static final Logger LOG = LogManager.getLogger(ApplicationManagerServlet.class);

	private final ApplicationServer applicationServer;


	public ApplicationManagerServlet(ApplicationServer applicationServer) {
		this.applicationServer = applicationServer;
	}

	@Override
	public void init() {
		LOG.debug("Initialize " + getClass().getSimpleName());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String host = req.getRemoteHost();

		if (!host.equals("127.0.0.1")) {
			resp.sendError(403);
			return;
		}

		String opcode = req.getParameter("opcode");

		if (isInvalid(opcode)) {
			resp.sendError(400);
			return;
		}

		try {
			switch (opcode) {
				case "start":
					start(req);
					break;

				case "stop":
					stop(req);
					break;

				default:
					break;
			}
		}
		catch (Exception e) {
			LOG.error("Manage application failed.", e);

			resp.sendError(400);
			return;
		}

		resp.sendError(200);
	}

	private void start(HttpServletRequest req) throws Exception {
		String contextPath = req.getParameter("contextPath");
		String appName = req.getParameter("appName");

		LOG.debug("Starting application {} on {}", appName, contextPath);

		if (isInvalid(contextPath) || isInvalid(appName)) {
			throw new IllegalArgumentException("Missing or invalid parameters passed.");
		}

		applicationServer.startWebApp(contextPath, appName);
	}

	private void stop(HttpServletRequest req) throws Exception {
		String contextPath = req.getParameter("contextPath");

		LOG.debug("Stopping application on {}", contextPath);

		if (isInvalid(contextPath)) {
			throw new IllegalArgumentException("Missing or invalid parameters passed.");
		}

		applicationServer.stopWebApp(contextPath);
	}

	private boolean isInvalid(String param) {
		return isNull(param) || param.isEmpty();
	}
}
