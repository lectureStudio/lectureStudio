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

package org.lecturestudio.broadcast.service;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.lecturestudio.broadcast.service.validator.MessageValidator;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.MessageService;

@Path("/message")
@RequestScoped
public class MessageResource extends ServiceBase {

	private final MessageValidator messageValidator;


	@Inject
	MessageResource(MessageValidator validator) {
		messageValidator = validator;
	}

	@Context
	public void setSse(Sse sse) {
		setSseEventBuilder(sse.newEventBuilder());
	}

	@Path("start/{classroomId}")
	@POST
	@RolesAllowed({ "admin", "lecturer" })
	@Transactional
	public Response startService(@PathParam("classroomId") String classroomId)
			throws Exception {
		return super.startService(classroomId, new MessageService());
	}

	@Path("stop/{classroomId}/{serviceId}")
	@POST
	@RolesAllowed({ "admin", "lecturer" })
	@Transactional
	public Response stopService(@PathParam("classroomId") String classroomId,
			@PathParam("serviceId") String serviceId) throws Exception {
		return super.stopService(classroomId, serviceId, MessageService.class);
	}

	@Path("subscribe/{serviceId}")
	@GET
	@RolesAllowed({ "admin", "lecturer" })
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void subscribe(@Context SseEventSink eventSink,
			@PathParam("serviceId") String serviceId) {
		registerSseSink(eventSink, serviceId, MessageService.class);
	}

	@Path("post")
	@POST
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMessage(@Context HttpServletRequest request, Message message) {
		String serviceId = message.getServiceId();

		MessageService service = classroomDataService.getServiceById(serviceId, MessageService.class);

		// Validate input.
		Response response = messageValidator.validate(service, message);

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			String remoteAddress = request.getRemoteAddr();

			// Notify service provider endpoint.
			sendWebMessage(new MessengerMessage(message, remoteAddress, new Date()),
					serviceId);
		}

		return response;
	}
}
