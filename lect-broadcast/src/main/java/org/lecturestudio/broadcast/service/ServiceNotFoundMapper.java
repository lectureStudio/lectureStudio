package org.lecturestudio.broadcast.service;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.lecturestudio.web.api.exception.ServiceNotFoundException;

@Provider
public class ServiceNotFoundMapper implements ExceptionMapper<ServiceNotFoundException> {

	@Override
	public Response toResponse(ServiceNotFoundException e) {
		return Response.status(Response.Status.BAD_REQUEST)
				.entity(e.getMessage()).build();
	}
}
