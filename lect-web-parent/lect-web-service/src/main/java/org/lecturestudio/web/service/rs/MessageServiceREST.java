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

package org.lecturestudio.web.service.rs;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.connector.RelayConnectors;
import org.lecturestudio.web.api.connector.server.Connector;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.WebPacket;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.MessageService;
import org.lecturestudio.web.service.model.WebSessions;
import org.lecturestudio.web.service.restrict.IpRestricted;
import org.lecturestudio.web.api.transactions.Transactional;
import org.lecturestudio.web.service.validator.MessageValidator;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;

@Path("/message")
@ApplicationScoped
public class MessageServiceREST extends ServiceBase {

	@Inject
	private MessageValidator messageValidator;


	@Path("/start")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Transactional
	public Response startService(
			@Context HttpServletRequest request,
			@Multipart("classroom") Classroom classroom,
			@Multipart("service") MessageService service) throws Exception
	{
		return super.startService(request, classroom, service);
	}

	@Path("/stop")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Transactional
	public Response stopService(
			@Multipart("classroom") Classroom classroom,
			@Multipart("service") MessageService service) throws Exception
	{
		return super.stopService(classroom, service);
	}

	@Path("/post")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@IpRestricted
	public Response postMessage(@Context HttpServletRequest request, Message message) {
		String serviceId = message.getServiceId();

		MessageService service = classroomDataService.getServiceById(serviceId, MessageService.class);

		// Validate input.
		Response response = messageValidator.validate(service, message);

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			String remoteAddress = request.getRemoteAddr();

			// Notify service provider endpoint.
			WebSessions.KeyPair key = new WebSessions.KeyPair(service.getContextPath(), service.getClass());
			RelayConnectors session = sessions.getWebSession(key);

			WebPacket packet = new WebPacket(new MessengerMessage(message.getText(), remoteAddress, new Date()));

			for (Connector connector : session.getProviderConnectors().getConnectors()) {
				connector.send(packet);
			}
		}

		return response;
	}
}
