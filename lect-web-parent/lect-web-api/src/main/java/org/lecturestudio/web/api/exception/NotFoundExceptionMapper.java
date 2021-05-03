package org.lecturestudio.web.api.exception;

import io.quarkus.runtime.TemplateHtmlBuilder;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.core.request.ServerDrivenNegotiation;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	private static final String ACCEPT = "Accept";

	private final static Variant JSON_VARIANT = new Variant(MediaType.APPLICATION_JSON_TYPE, (String) null, null);
	private final static Variant HTML_VARIANT = new Variant(MediaType.TEXT_HTML_TYPE, (String) null, null);

	private final static List<Variant> VARIANTS = List.of(JSON_VARIANT, HTML_VARIANT);

	@Context
	private HttpHeaders headers;


	@Override
	public Response toResponse(NotFoundException exception) {
		Variant variant = selectVariant(headers);

		if (variant == JSON_VARIANT) {
			return Response.status(Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON).build();
		}

		if (variant == HTML_VARIANT) {
			TemplateHtmlBuilder sb = new TemplateHtmlBuilder(
					Status.NOT_FOUND.getReasonPhrase(), "", "");
			return Response.status(Status.NOT_FOUND).entity(sb.toString())
					.type(MediaType.TEXT_HTML_TYPE).build();
		}

		return Response.status(Status.NOT_FOUND).build();
	}

	private static Variant selectVariant(HttpHeaders headers) {
		ServerDrivenNegotiation negotiation = new ServerDrivenNegotiation();
		negotiation.setAcceptHeaders(headers.getRequestHeaders().get(ACCEPT));
		return negotiation.getBestMatch(VARIANTS);
	}
}
