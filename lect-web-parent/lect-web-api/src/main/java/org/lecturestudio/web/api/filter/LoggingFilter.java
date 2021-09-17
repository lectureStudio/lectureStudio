package org.lecturestudio.web.api.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * @author Alex Andres
 * Class which implements an Logging Filter for easy error analysis
 */
@Provider
public class LoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        StringBuilder builder = new StringBuilder("----- <Response> ------------\n");
        builder.append("  Status: ").append(responseContext.getStatus()).append("\n");

        if (MediaType.APPLICATION_JSON_TYPE.equals(responseContext.getMediaType())) {
            byte[] content = responseContext.getEntityStream().readAllBytes();

            builder.append("  Body: " + new String(content, StandardCharsets.UTF_8)).append("\n");

            responseContext.setEntityStream(new ByteArrayInputStream(content));
        }

        builder.append("----- </Response> ----------\n");

        System.out.println(builder.toString());
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        StringBuilder builder = new StringBuilder("----- <Request> ------------\n");
        builder.append("  Method: ").append(requestContext.getMethod()).append("\n");
        builder.append("  URI: ").append(requestContext.getUri()).append("\n");
        builder.append("----- </Request> ----------\n");

        System.out.println(builder.toString());
    }

}

