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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.util.IOUtils;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.StreamService;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.StreamServiceClient;
import org.lecturestudio.web.api.ws.databind.JsonProvider;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamRestClient extends ExecutableRestClient<StreamService> implements StreamServiceClient {

	private static final Logger LOG = LogManager.getLogger(StreamRestClient.class);

	private static final String servicePath = "/bcast/ws/stream";


	public StreamRestClient(ConnectionParameters parameters) throws Exception {
		super(parameters, servicePath);
	}

	public void sendDocument(Classroom classroom, Document doc) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending document: {}", doc.getName());
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		doc.toOutputStream(outStream);

		InputStream docDataStream = new ByteArrayInputStream(outStream.toByteArray());

		List<Attachment> attachments = new ArrayList<>();
		attachments.add(new Attachment("classroom", MediaType.APPLICATION_JSON, classroom));
		attachments.add(new Attachment("file", docDataStream, new ContentDisposition("attachment;filename=" + doc.getName() + ".pdf")));

		MultipartBody multiPart = new MultipartBody(attachments);
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("document").path("upload").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending document: {}", responseToString(response));
		}

		checkForError(response);

		docDataStream.close();
	}

	@Override
	public void getDocument(String contextPath, String docFile, OutputStream outStream) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Get document: {}", docFile);
		}

		MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
		formData.add("classroomName", contextPath);
		formData.add("fileName", docFile);

		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("document").path("get").request().post(Entity.form(formData), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Getting document: {}", responseToString(response));
		}

		checkForError(response);

		InputStream inputStream = response.readEntity(InputStream.class);
		IOUtils.copy(inputStream, outStream);
		inputStream.close();
	}

}
