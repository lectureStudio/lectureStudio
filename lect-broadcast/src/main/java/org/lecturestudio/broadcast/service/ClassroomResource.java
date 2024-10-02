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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.UUID;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import javax.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.lecturestudio.web.api.data.ClassroomDataService;
import org.lecturestudio.web.api.model.Classroom;

@Path("/classroom")
@RolesAllowed({ "admin", "lecturer" })
@RequestScoped
public class ClassroomResource {

	private final ClassroomDataService classroomDataService;


	@Inject
	ClassroomResource(ClassroomDataService dataService) {
		classroomDataService = dataService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public Response createClassroom(Classroom classroom) throws Exception {
		String contextPath = classroom.getShortName();

		if (isNull(contextPath)) {
			contextPath = "";
		}

		if (nonNull(classroomDataService.getByContextPath(contextPath))) {
			throw new Exception("Classroom has already been created");
		}

		// Create new classroom.
		Classroom newClassroom = new Classroom(classroom.getName(), contextPath);
		newClassroom.setUuid(UUID.randomUUID());
		newClassroom.setCreatedTimestamp(System.currentTimeMillis());
		newClassroom.setDocuments(classroom.getDocuments());
		newClassroom.setIpFilterRules(classroom.getIpFilterRules());

		classroomDataService.add(newClassroom);

		return Response.ok().entity(newClassroom.getUuid().toString()).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
	public Response updateClassroom(Classroom classroom) throws Exception {
		Classroom dbClassroom = classroomDataService.getByUuid(classroom.getUuid());

		if (isNull(dbClassroom)) {
			throw new Exception("Classroom does not exist");
		}

		dbClassroom.setName(classroom.getName());
		dbClassroom.setIpFilterRules(classroom.getIpFilterRules());

		return Response.ok().build();
	}

	@Path("{classroomId}")
	@DELETE
	@Transactional
	public Response deleteClassroom(@PathParam("classroomId") String classroomId) throws Exception {
		Classroom classroom = classroomDataService.getByUuid(UUID.fromString(classroomId));

		if (isNull(classroom)) {
			throw new Exception("Classroom does not exist");
		}

		classroomDataService.delete(classroom);

		return Response.ok().build();
	}

	@GET
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassroom(@Context HttpServletRequest request) {
		String contextPath = request.getContextPath();
		Classroom classroom = classroomDataService.getByContextPath(contextPath);

		return Response.ok().entity(classroom).build();
	}

	@Path("list")
	@GET
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public List<Classroom> getClassrooms() {
		return classroomDataService.getAll();
	}
}
