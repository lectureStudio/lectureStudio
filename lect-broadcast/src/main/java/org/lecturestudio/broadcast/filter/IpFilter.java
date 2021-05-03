package org.lecturestudio.broadcast.filter;

import static java.util.Objects.isNull;

import io.quarkus.runtime.TemplateHtmlBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.lecturestudio.web.api.data.ClassroomDataService;
import org.lecturestudio.web.api.model.Classroom;

@WebFilter(filterName = "IpFilter", urlPatterns = "/*", asyncSupported = true)
public class IpFilter implements Filter {

	private final Instance<ClassroomDataService> classroomDataService;


	@Inject
	IpFilter(Instance<ClassroomDataService> classroomDataService) {
		this.classroomDataService = classroomDataService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

		if (!isIndex(path)) {
			// Pass request down the filter chain.
			chain.doFilter(request, response);
			return;
		}

		String contextPath = request.getServletContext().getContextPath();
		String remoteAddress = request.getRemoteAddr();

		Classroom classroom = classroomDataService.get().getByContextPath(contextPath);

		if (isNull(classroom)) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Not Found");
			return;
		}

		org.lecturestudio.web.api.filter.IpFilter ipFilter = new org.lecturestudio.web.api.filter.IpFilter();
		ipFilter.setRules(classroom.getIpFilterRules());

		if (!ipFilter.isAllowed(remoteAddress)) {
			sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			return;
		}

		// Pass request down the filter chain.
		chain.doFilter(request, response);
	}

	private boolean isIndex(String path) {
		return path.equals("/") || path.equals("/index.html");
	}

	private void sendError(ServletResponse response, int status, String message) throws IOException {
		if (response instanceof HttpServletResponse) {
			final TemplateHtmlBuilder htmlBuilder = new TemplateHtmlBuilder(message, "", "");

			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setContentType(MediaType.TEXT_HTML);
			httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
			httpResponse.setStatus(status);
			httpResponse.getWriter().write(htmlBuilder.toString());
		}
		else {
			response.setContentType(MediaType.TEXT_PLAIN);
			response.getWriter().write(String.valueOf(status));
			response.getWriter().flush();
		}
	}
}
