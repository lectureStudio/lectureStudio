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

import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.MessageService;
import org.lecturestudio.web.api.ws.ConnectionParameters;
import org.lecturestudio.web.api.ws.MessageServiceClient;
import org.lecturestudio.web.api.ws.databind.JsonProvider;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageRestClient extends ExecutableRestClient<MessageService> implements MessageServiceClient {

	private static final Logger LOG = LogManager.getLogger(MessageRestClient.class);

	private static final String servicePath = "/bcast/ws/message";


	public MessageRestClient(ConnectionParameters parameters) throws Exception {
		super(parameters, servicePath);
	}

	@Override
	public void postMessage(Message message) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Post message");
		}

		List<Attachment> attachments = new ArrayList<>();
		attachments.add(new Attachment("message", MediaType.APPLICATION_JSON, message));

		MultipartBody multiPart = new MultipartBody(attachments);
		WebTarget target = getWebTarget(JsonProvider.class);
		Response response = target.path("post").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), Response.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Post message: {}", responseToString(response));
		}

		checkForError(response);
	}

}
