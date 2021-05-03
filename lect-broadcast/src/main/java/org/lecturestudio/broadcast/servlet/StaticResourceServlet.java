package org.lecturestudio.broadcast.servlet;

import static java.util.Objects.nonNull;

import io.quarkus.runtime.TemplateHtmlBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lecturestudio.web.api.data.ClassroomDataService;
import org.lecturestudio.web.api.model.HttpResourceFile;
import org.lecturestudio.web.api.model.QuizService;

@WebServlet(urlPatterns = { "/static/*" })
public class StaticResourceServlet extends HttpServlet {

	private static final String ETAG_HEADER = "W/\"%s-%s\"";
	private static final String CONTENT_DISPOSITION_HEADER = "inline;filename=\"%1$s\"; filename*=UTF-8''%1$s";

	private static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);
	private static final long DEFAULT_EXPIRE_TIME_IN_MILLIS = TimeUnit.DAYS.toMillis(30);

	private final ClassroomDataService classroomDataService;


	@Inject
	StaticResourceServlet(ClassroomDataService classroomDataService) {
		super();

		this.classroomDataService = classroomDataService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String contextPath = request.getServletContext().getContextPath();
		String pathInfo = request.getPathInfo();
		String fileName = URLDecoder.decode(pathInfo.substring(1), StandardCharsets.UTF_8.name());

		QuizService service = classroomDataService
				.getServiceByContextPath(contextPath, QuizService.class);

		if (nonNull(service)) {
			List<HttpResourceFile> resources = service.getQuiz().getQuestionResources();

			if (nonNull(resources) && !resources.isEmpty()) {
				for (HttpResourceFile resource : resources) {
					if (resource.getName().equals(fileName)) {
						sendContent(request, response, resource);
						return;
					}
				}
			}
		}

		sendError(response);
	}

	private void sendContent(HttpServletRequest request, HttpServletResponse response, HttpResourceFile resource)
			throws IOException {
		String fileName = URLEncoder.encode(resource.getName(), StandardCharsets.UTF_8.name());
		boolean notModified = setCacheHeaders(request, response, fileName, resource.getModified());

		if (notModified) {
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		setContentHeaders(response, fileName, resource.getContent().length);

		writeContent(response, resource);
	}

	private boolean setCacheHeaders(HttpServletRequest request, HttpServletResponse response, String fileName, long lastModified) {
		String eTag = String.format(ETAG_HEADER, fileName, lastModified);
		response.setHeader("ETag", eTag);
		response.setDateHeader("Last-Modified", lastModified);
		response.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_IN_MILLIS);

		return notModified(request, eTag, lastModified);
	}

	private boolean notModified(HttpServletRequest request, String eTag, long lastModified) {
		String ifNoneMatch = request.getHeader("If-None-Match");

		if (ifNoneMatch != null) {
			String[] matches = ifNoneMatch.split("\\s*,\\s*");
			Arrays.sort(matches);
			return (Arrays.binarySearch(matches, eTag) > -1 || Arrays.binarySearch(matches, "*") > -1);
		}
		else {
			long ifModifiedSince = request.getDateHeader("If-Modified-Since");
			return (ifModifiedSince + ONE_SECOND_IN_MILLIS > lastModified);
		}
	}

	private void setContentHeaders(HttpServletResponse response, String fileName, long contentLength) {
		response.setHeader("Content-Type", getServletContext().getMimeType(fileName));
		response.setHeader("Content-Disposition", String.format(CONTENT_DISPOSITION_HEADER, fileName));

		if (contentLength != -1) {
			response.setHeader("Content-Length", String.valueOf(contentLength));
		}
	}

	private void writeContent(HttpServletResponse response, HttpResourceFile resource) throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(resource.getContent())) {
			long length = inputStream.transferTo(response.getOutputStream());

			response.setHeader("Content-Length", String.valueOf(length));
		}
	}

	private void sendError(ServletResponse response) throws IOException {
		int status = HttpServletResponse.SC_NOT_FOUND;
		String message = "Not Found";

		if (response instanceof HttpServletResponse) {
			final TemplateHtmlBuilder htmlBuilder = new TemplateHtmlBuilder(message, "", "");

			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setContentType("text/html");
			httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
			httpResponse.setStatus(status);
			httpResponse.getWriter().write(htmlBuilder.toString());
		}
		else {
			response.setContentType("text/plain");
			response.getWriter().write(String.valueOf(status));
			response.getWriter().flush();
		}
	}
}
