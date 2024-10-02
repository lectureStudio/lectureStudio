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

import java.time.ZonedDateTime;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import org.lecturestudio.broadcast.service.validator.MessageValidator;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.Classroom;
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

		Classroom classroom = classroomDataService.getByContextPath("");
		MessageService service = classroom.getServices()
				.stream()
				.filter(MessageService.class::isInstance)
				.map(MessageService.class::cast)
				.findFirst().orElse(null);

		// Validate input.
		Response response = messageValidator.validate(service, message);

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			String remoteAddress = request.getRemoteAddr();

			// Notify service provider endpoint.
			sendWebMessage(new MessengerMessage(message, remoteAddress,
							ZonedDateTime.now()),
					serviceId);
		}

		return response;
	}
}
