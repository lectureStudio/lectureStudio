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

package org.lecturestudio.web.api.ws.rs;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.ClassroomService;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.ExecutableServiceClient;
import org.lecturestudio.web.api.ws.databind.JsonProvider;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ExecutableRestClient<T extends ClassroomService> extends RestClientBase implements ExecutableServiceClient<T> {

	private static final Logger LOG = LogManager.getLogger(ExecutableRestClient.class);


	public ExecutableRestClient(ConnectionParameters parameters, String servicePath) throws Exception {
		super(parameters, servicePath);
	}

	@Override
	public Classroom startService(Classroom classroom, T service) throws Exception {
		List<Attachment> attachments = new ArrayList<>();
		attachments.add(new Attachment("classroom", MediaType.APPLICATION_JSON, classroom));
		attachments.add(new Attachment("service", MediaType.APPLICATION_JSON, service));

		MultipartBody multiPart = new MultipartBody(attachments);
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("start").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Start {}: {}", service.getClass().getSimpleName(), responseToString(response));
		}

		checkForError(response);

		Classroom resClassroom = response.readEntity(Classroom.class);

		if (resClassroom == null) {
			throw new Exception("No classroom response received.");
		}

		return resClassroom;
	}

	@Override
	public Classroom stopService(Classroom classroom, T service) throws Exception {
		List<Attachment> attachments = new ArrayList<>();
		attachments.add(new Attachment("classroom", MediaType.APPLICATION_JSON, classroom));
		attachments.add(new Attachment("service", MediaType.APPLICATION_JSON, service));

		MultipartBody multiPart = new MultipartBody(attachments);
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("stop").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Stop service: {}", responseToString(response));
		}

		checkForError(response);

		Classroom resClassroom = response.readEntity(Classroom.class);

		if (resClassroom == null) {
			throw new Exception("No classroom response received.");
		}

		return resClassroom;
	}
}
