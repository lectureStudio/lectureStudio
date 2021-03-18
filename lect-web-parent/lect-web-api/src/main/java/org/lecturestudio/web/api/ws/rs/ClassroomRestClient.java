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

import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.ws.ClassroomServiceClient;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.databind.JsonProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassroomRestClient extends RestClientBase implements ClassroomServiceClient {

	private static final Logger LOG = LogManager.getLogger(ClassroomRestClient.class);

	private static final String servicePath = "/bcast/ws/classroom";


	public ClassroomRestClient(ConnectionParameters parameters) throws Exception {
		super(parameters, servicePath);
	}

	@Override
	public List<Classroom> getClassrooms() throws Exception {
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("list").request().accept(MediaType.APPLICATION_JSON).get(Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Get classrooms: {}", responseToString(response));
		}

		checkForError(response);

		return response.readEntity(new GenericType<List<Classroom>>() {});
	}

}
