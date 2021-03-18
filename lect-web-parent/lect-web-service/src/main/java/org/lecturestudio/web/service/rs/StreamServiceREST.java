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

import static java.util.Objects.isNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.StreamService;
import org.lecturestudio.web.api.transactions.Transactional;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

@Path("/stream")
@ApplicationScoped
public class StreamServiceREST extends ServiceBase {

	@Path("/start")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Transactional
	public Response startService(
			@Context HttpServletRequest request,
			@Multipart("classroom") Classroom classroom,
			@Multipart("service") StreamService service) throws Exception
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
			@Multipart("service") StreamService service) throws Exception
	{
		return super.stopService(classroom, service);
	}

	@Path("/document/upload")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response uploadFile(
			@Context HttpServletRequest request,
			@Multipart("file") Attachment file,
			@Multipart("classroom") Classroom classroom) throws Exception
	{
		String fileName = file.getContentDisposition().getParameter("filename");

		if (!inWhiteList(fileName)) {
			throw new Exception("File type not supported.");
		}

		String shortName = classroom.getShortName();

		Classroom contextClassroom = classroomDataService.getByContextPath(shortName);

		if (isNull(contextClassroom)) {
			throw new Exception("Classroom does not exist.");
		}

		String classroomDir = getClassroomDir(request, shortName);
		String docPath = classroomDir + File.separator + fileName;

		FileUtils.create(classroomDir);
		Files.copy(file.getObject(InputStream.class), Paths.get(docPath), StandardCopyOption.REPLACE_EXISTING);

		// Sync documents between provider and the session.
		contextClassroom.setDocuments(classroom.getDocuments());

		classroomDataService.update(contextClassroom);

		return Response.ok().build();
	}

	@Path("/document/get")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.MULTIPART_FORM_DATA})
	@Produces({MediaType.APPLICATION_OCTET_STREAM,MediaType.APPLICATION_JSON})
	public Response getFile(
			@Context HttpServletRequest request,
			MultivaluedMap<String, String> formParams) throws Exception {
		String contextPath = formParams.getFirst("classroomName");
		String fileName = formParams.getFirst("fileName");

		Classroom classroom = classroomDataService.getByContextPath(contextPath);

		if (isNull(classroom)) {
			throw new Exception("Classroom does not exist.");
		}

		String classroomDir = getClassroomDir(request, contextPath);
		String docPath = classroomDir + File.separator + fileName;

		File file = new File(docPath);

		StreamingOutput output = stream -> {
			final FileInputStream fileStream = new FileInputStream(file);
			final FileChannel inputChannel = fileStream.getChannel();
			final WritableByteChannel outputChannel = Channels.newChannel(stream);

			try {
				inputChannel.transferTo(0, inputChannel.size(), outputChannel);
			}
			finally {
				stream.flush();
				stream.close();

				inputChannel.close();
				outputChannel.close();
				fileStream.close();
			}
		};

		return Response.ok(output).build();
	}

	private String getClassroomDir(HttpServletRequest request, String classroomName) {
		String baseDir = request.getServletContext().getRealPath("/");
		String classroomsDir = config.classroomsDir;
		classroomsDir = baseDir + File.separator + classroomsDir;
		classroomsDir = classroomsDir + File.separator + classroomName;

		return classroomsDir;
	}

	private boolean inWhiteList(String fileName) {
		for (String ext : config.fileWhitelist) {
			if (fileName.toLowerCase().endsWith("." + ext)) {
				return true;
			}
		}
		return false;
	}
}
