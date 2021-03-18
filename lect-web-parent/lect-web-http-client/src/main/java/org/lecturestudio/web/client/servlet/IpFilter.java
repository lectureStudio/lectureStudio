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

package org.lecturestudio.web.client.servlet;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.data.ClassroomDataService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IpFilter implements Filter {

	private static final Logger LOG = LogManager.getLogger(IpFilter.class);

	/**
	 * Mime type "text/plain".
	 */
	private static final String PLAIN_TEXT_MIME_TYPE = "text/plain";

	/**
	 * The HTTP response status code that is used when rejecting denied request.
	 * It is 403 (FORBIDDEN) by default.
	 */
	private static final int DENY_STATUS = HttpServletResponse.SC_FORBIDDEN;

	/**
	 * The last logged denied IP address.
	 */
	private AtomicReference<String> lastAddress;

	@Inject
	private ClassroomDataService classroomDataService;


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initializing for context path: {}", filterConfig.getServletContext().getContextPath());
		}

		if (isNull(classroomDataService)) {
			throw new ServletException("ClassroomDataService is not initialized.");
		}

		lastAddress = new AtomicReference<>();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String contextPath = request.getServletContext().getContextPath();
		String remoteAddress = request.getRemoteAddr();
		String lastAddress = this.lastAddress.getAndSet(remoteAddress);

		Classroom classroom = classroomDataService.getByContextPath(contextPath);

		if (isNull(classroom)) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		org.lecturestudio.web.api.filter.IpFilter ipFilter = new org.lecturestudio.web.api.filter.IpFilter();
		ipFilter.setRules(classroom.getIpFilterRules());

		if (!ipFilter.isAllowed(remoteAddress)) {
			if (LOG.isInfoEnabled()) {
				// Try not to log consecutive equal addresses.
				if (!remoteAddress.equals(lastAddress)) {
					LOG.info("Connection attempt blocked from: {}", remoteAddress);
				}
			}

			sendError(response);
			return;
		}

		// Pass request down the filter chain.
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Destroying");
		}

		lastAddress = null;
	}

	private void sendError(ServletResponse response) throws IOException {
		if (response instanceof HttpServletResponse) {
			((HttpServletResponse) response).sendError(DENY_STATUS);
		}
		else {
			response.setContentType(PLAIN_TEXT_MIME_TYPE);
			response.getWriter().write(String.valueOf(DENY_STATUS));
			response.getWriter().flush();
		}
	}
}
