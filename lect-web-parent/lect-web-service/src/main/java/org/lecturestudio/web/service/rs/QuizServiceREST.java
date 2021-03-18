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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.web.api.connector.RelayConnectors;
import org.lecturestudio.web.api.connector.server.Connector;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.message.WebPacket;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.QuizService;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.service.model.WebSessions;
import org.lecturestudio.web.service.restrict.IpRestricted;
import org.lecturestudio.web.api.transactions.Transactional;
import org.lecturestudio.web.service.validator.QuizAnswerValidator;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

@Path("/quiz")
@ApplicationScoped
public class QuizServiceREST extends ServiceBase {

	@Inject
	private QuizAnswerValidator quizAnswerValidator;


	@Path("/start")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Transactional
	public Response startService(
			@Context HttpServletRequest request,
			@Multipart("classroom") Classroom classroom,
			@Multipart("service") QuizService service) throws Exception
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
			@Multipart("service") QuizService service) throws Exception
	{
		return super.stopService(classroom, service);
	}

	@Path("/post")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@IpRestricted
	@Transactional
	public Response postQuizAnswer(@Context HttpServletRequest request, QuizAnswer quizAnswer) {
		String serviceId = quizAnswer.getServiceId();

		QuizService service = classroomDataService.getServiceById(serviceId, QuizService.class);

		// Validate input.
		Response response = quizAnswerValidator.validate(request, service, quizAnswer);

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			String remoteAddress = request.getRemoteAddr();

			service.getHosts().add(remoteAddress.hashCode());

			// Notify service provider endpoint.
			WebSessions.KeyPair key = new WebSessions.KeyPair(service.getContextPath(), service.getClass());
			RelayConnectors session = sessions.getWebSession(key);

			WebPacket packet = new WebPacket(new QuizAnswerMessage(quizAnswer, remoteAddress));

			for (Connector connector : session.getProviderConnectors().getConnectors()) {
				connector.send(packet);
			}
		}

		return response;
	}

	@Path("/file/upload")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response webUpload(
			@Context HttpServletRequest request,
			@Multipart("file") Attachment file,
			@Multipart("classroom") Classroom classroom) throws Exception
	{
		String fileName = file.getContentDisposition().getParameter("filename");

		if (!inWhiteList(fileName)) {
			throw new Exception("File type not supported.");
		}

		String baseDir = request.getServletContext().getRealPath("/");
		String webapps = new File(baseDir).getParentFile().getAbsolutePath();
		String classroomsDir = config.classroomsDir;
		String classroomName = classroom.getShortName();
		String classroomDir = Paths.get(webapps, "ROOT", classroomsDir, classroomName).toString();
		String docPath = classroomDir + File.separator + fileName;

		FileUtils.create(classroomDir);
		Files.copy(file.getObject(InputStream.class), Paths.get(docPath), StandardCopyOption.REPLACE_EXISTING);

		return Response.ok().build();
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
