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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.QuizService;
import org.lecturestudio.web.api.model.quiz.QuizAnswer;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.QuizServiceClient;
import org.lecturestudio.web.api.ws.databind.JsonProvider;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuizRestClient extends ExecutableRestClient<QuizService> implements QuizServiceClient {

	private static final Logger LOG = LogManager.getLogger(QuizRestClient.class);

	private static final String servicePath = "/bcast/ws/quiz";


	public QuizRestClient(ConnectionParameters parameters) throws Exception {
		super(parameters, servicePath);
	}

	@Override
	public void postQuizAnswer(QuizAnswer answer) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Post quiz");
		}

		List<Attachment> attachments = new ArrayList<>();
		attachments.add(new Attachment("answer", MediaType.APPLICATION_JSON, answer));

		MultipartBody multiPart = new MultipartBody(attachments);
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("post").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Post quiz: {}", responseToString(response));
		}

		checkForError(response);
	}

	@Override
	public void sendQuizFile(Classroom classroom, String filePath) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending web file: {}", filePath);
		}

		File docFile = new File(filePath);
		InputStream fileStream = new FileInputStream(docFile);

		List<Attachment> attachments = new ArrayList<>();
		attachments.add(new Attachment("classroom", MediaType.APPLICATION_JSON, classroom));
		attachments.add(new Attachment("file", fileStream, new ContentDisposition("attachment;filename=" + docFile.getName())));

		MultipartBody multiPart = new MultipartBody(attachments);
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("file").path("upload").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending web file: {}", responseToString(response));
		}

		checkForError(response);

		fileStream.close();
	}

}
