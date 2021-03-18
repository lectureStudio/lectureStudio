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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.data.ClassroomDataService;

@Path("/classroom")
@ApplicationScoped
public class ClassroomServiceREST {

	@Inject
	private ClassroomDataService classroomDataService;


	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response classroom() {
		Classroom classroom = classroomDataService.getByContextPath("");

		return Response.ok().entity(classroom).build();
	}

	@Path("/list")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response classrooms() {
		List<Classroom> classrooms = classroomDataService.getAll();

		return Response.ok().entity(classrooms).build();
	}
}
