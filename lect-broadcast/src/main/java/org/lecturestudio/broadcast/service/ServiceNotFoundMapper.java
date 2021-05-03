package org.lecturestudio.broadcast.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.lecturestudio.web.api.exception.ServiceNotFoundException;

@Provider
public class ServiceNotFoundMapper implements ExceptionMapper<ServiceNotFoundException> {

	@Override
	public Response toResponse(ServiceNotFoundException e) {
		return Response.status(Response.Status.BAD_REQUEST)
				.entity(e.getMessage()).build();
	}
}
