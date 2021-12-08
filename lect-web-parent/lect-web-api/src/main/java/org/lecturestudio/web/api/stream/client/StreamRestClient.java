/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.client;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import org.lecturestudio.web.api.client.ApiKeyFilter;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.stream.model.Course;

/**
 * Streaming API REST client implementation. The user must be authenticated with
 * a token to the API.
 *
 * @author Alex Andres
 */
@Path("/api/publisher")
@RegisterProviders({
	@RegisterProvider(ApiKeyFilter.class),
	@RegisterProvider(JsonConfigProvider.class)
})
public interface StreamRestClient {

	/**
	 * Gets a list of all courses associated with a user.
	 *
	 * @return A list of all courses associated with a user.
	 */
	@GET
	@Path("/courses")
	List<Course> getCourses();

	/**
	 * Uploads the provided multipart data.
	 *
	 * @param data The multipart data to upload.
	 *
	 * @return The file name, if successfully uploaded.
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/file/upload")
	String uploadFile(@MultipartForm MultipartBody data);

	/**
	 * Accept a speech request with the corresponding ID.
	 *
	 * @param requestId The request ID.
	 */
	@POST
	@Path("/speech/accept/{requestId}")
	void acceptSpeechRequest(@PathParam("requestId") long requestId);

	/**
	 * Reject a speech request with the corresponding ID.
	 *
	 * @param requestId The request ID.
	 */
	@POST
	@Path("/speech/reject/{requestId}")
	void rejectSpeechRequest(@PathParam("requestId") long requestId);

}
