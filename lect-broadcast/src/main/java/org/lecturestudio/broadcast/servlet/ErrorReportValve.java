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

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.Constants;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.text.StringSubstitutor;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

/**
 * Modified and simplified version of the default Tomcat ErrorReportValve.
 * This ErrorReportValve is able to load and return an HTML template file.
 *
 * @author Alex Andres
 */
public class ErrorReportValve extends ValveBase {

	private String errorTemplate;


	public ErrorReportValve() {
		super(true);
	}

	public void setErrorTemplate(String template) {
		this.errorTemplate = template;
	}

	/**
	 * Invoke the next Valve in the sequence. When the invoke returns, check
	 * the response state. If the status code is greater than or equal to 400
	 * or an uncaught exception was thrown then the error handling will be
	 * triggered.
	 *
	 * @param request  The servlet request to be processed
	 * @param response The servlet response to be created
	 *
	 * @throws IOException      if an input/output error occurs
	 * @throws ServletException if a servlet error occurs
	 */
	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		// Perform the request
		getNext().invoke(request, response);

		if (response.isCommitted()) {
			if (response.setErrorReported()) {
				// Error wasn't previously reported but we can't write an error
				// page because the response has already been committed. Attempt
				// to flush any data that is still to be written to the client.
				try {
					response.flushBuffer();
				}
				catch (Throwable t) {
					ExceptionUtils.handleThrowable(t);
				}
				// Close immediately to signal to the client that something went wrong.
				response.getCoyoteResponse().action(ActionCode.CLOSE_NOW, null);
			}
			return;
		}

		Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

		// If an async request is in progress and is not going to end once this
		// container thread finishes, do not process any error page here.
		if (request.isAsync() && !request.isAsyncCompleting()) {
			return;
		}

		if (throwable != null && !response.isError()) {
			// Make sure that the necessary methods have been called on the
			// response. (It is possible a component may just have set the
			// Throwable. Tomcat won't do that but other components might.)
			// These are safe to call at this point as we know that the response
			// has not been committed.
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		// One way or another, response.sendError() will have been called before
		// execution reaches this point and suspended the response. Need to
		// reverse that so this valve can write to the response.
		response.setSuspended(false);

		try {
			report(request, response, throwable);
		}
		catch (Throwable tt) {
			ExceptionUtils.handleThrowable(tt);
		}
	}

	/**
	 * Prints out an error report.
	 *
	 * @param request   The request being processed
	 * @param response  The response being generated
	 * @param throwable The exception that occurred (which possibly wraps
	 *                  a root cause exception
	 */
	private void report(Request request, Response response, Throwable throwable) {
		int statusCode = response.getStatus();

		// Do nothing on a 1xx, 2xx and 3xx status
		// Do nothing if anything has been written already
		// Do nothing if the response hasn't been explicitly marked as in error
		// and that error has not been reported.
		if (statusCode < 400 || response.getContentWritten() > 0 || !response.setErrorReported()) {
			return;
		}

		// If an error has occurred that prevents further I/O, don't waste time
		// producing an error report that will never be read.
		AtomicBoolean result = new AtomicBoolean(false);
		response.getCoyoteResponse().action(ActionCode.IS_IO_ALLOWED, result);

		if (!result.get()) {
			return;
		}

		// Do nothing if there is no reason phrase for the specified status code and
		// no error message provided
		String reason = null;
		StringManager smClient = StringManager.getManager(Constants.Package, request.getLocales());

		response.setLocale(smClient.getLocale());

		try {
			reason = smClient.getString("http." + statusCode + ".reason");
		}
		catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
		}

		if (reason == null) {
			reason = smClient.getString("errorReportValve.unknownReason");
		}

		StringBuilder sb = new StringBuilder();

		if (errorTemplate != null) {
			Map<String, String> valuesMap = new HashMap<>();
			valuesMap.put("statusCode", String.valueOf(statusCode));
			valuesMap.put("reason", reason);

			sb.append(StringSubstitutor.replace(errorTemplate, valuesMap));
		}
		else {
			String message = String.valueOf(statusCode) + " - " + reason;

			sb.append("<!doctype html>");
			sb.append("<head>");
			sb.append("<title>");
			sb.append(reason);
			sb.append("</title>");
			sb.append("</head>");
			sb.append("<body>");
			sb.append("<h1>");
			sb.append(message);
			sb.append("</h1>");
			sb.append("</body>");
			sb.append("</html>");
		}

		try {
			try {
				response.setContentType("text/html");
				response.setCharacterEncoding("utf-8");
			}
			catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);

				if (container.getLogger().isDebugEnabled()) {
					container.getLogger().debug("status.setContentType", t);
				}
			}

			Writer writer = response.getReporter();
			if (writer != null) {
				// If writer is null, it's an indication that the response has
				// been hard committed already, which should never happen
				writer.write(sb.toString());
				response.finishResponse();
			}
		}
		catch (Exception e) {
			// Ignore
		}
	}

}
