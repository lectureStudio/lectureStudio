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
import java.util.Map;
import java.util.UUID;

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
import org.lecturestudio.web.api.model.UserInfo;
import org.lecturestudio.web.api.model.UserPrivileges;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

/**
 * Streaming API REST client implementation. The user must be authenticated with
 * a token to the API. This client implements course related signaling between
 * publishers and other course participants.
 *
 * @author Alex Andres
 */
@Path("/api/v1/publisher")
@RegisterProviders({
	@RegisterProvider(ApiKeyFilter.class),
	@RegisterProvider(JsonConfigProvider.class)
})
public interface StreamRestClient {

	/**
	 * Get information about the authenticated user.
	 *
	 * @return The user information.
	 */
	@Path("/user")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	UserInfo getUserInfo();

	/**
	 * Get privileges for the registered course user.
	 *
	 * @param courseId The unique course ID of the course of which to retrieve
	 *                 the privileges.
	 *
	 * @return The user privileges.
	 */
	@Path("/user/privileges/{courseId}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	UserPrivileges getUserPrivileges(@PathParam("courseId") long courseId);

	/**
	 * Gets a list of all courses associated with the authenticated user.
	 *
	 * @return A list of all courses.
	 */
	@GET
	@Path("/courses")
	List<Course> getCourses();

	/**
	 * Gets a list of all participants in an active course.
	 *
	 * @param courseId The unique course ID.
	 *
	 * @return A list of all participants in an active course.
	 */
	@GET
	@Path("/participants/{courseId}")
	List<CourseParticipant> getParticipants(@PathParam("courseId") long courseId);

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
	 * Accept a speech request with the corresponding request ID.
	 *
	 * @param requestId The request ID.
	 */
	@POST
	@Path("/speech/accept/{requestId}")
	void acceptSpeechRequest(@PathParam("requestId") UUID requestId);

	/**
	 * Reject a speech request with the corresponding request ID.
	 *
	 * @param requestId The request ID.
	 */
	@POST
	@Path("/speech/reject/{requestId}")
	void rejectSpeechRequest(@PathParam("requestId") UUID requestId);

	/**
	 * Notify course participants whether a course is being recorded or not.
	 *
	 * @param courseId The unique course ID.
	 * @param recorded True if the course is being recorded.
	 */
	@POST
	@Path("/course/recorded/{courseId}/{recorded}")
	void setCourseRecordingState(@PathParam("courseId") long courseId,
			@PathParam("recorded") boolean recorded);

	/**
	 * Notify course participants that a course stream has been started.
	 *
	 * @param courseId The unique course ID of the course that was started.
	 */
	@POST
	@Path("/stream/start/{courseId}")
	void startedStream(@PathParam("courseId") long courseId);

	/**
	 * Notify course participants that a course stream has been restarted. The
	 * restart may have been caused due to an interrupted connection.
	 *
	 * @param courseId The unique course ID of the course that was restarted.
	 */
	@POST
	@Path("/stream/restart/{courseId}")
	void restartedStream(@PathParam("courseId") long courseId);

	/**
	 * Update the media stream state for a given streaming session in a course.
	 * The media state is represented by a mapping {@code MediaType to Boolean}
	 * which indicates whether a stream with the given MediaType is
	 * muted/enabled or not.
	 *
	 * @param courseId The unique course ID.
	 * @param state    The current media stream state.
	 */
	@POST
	@Path("/stream/media/state/{courseId}")
	void updateStreamMediaState(@PathParam("courseId") long courseId,
			Map<org.lecturestudio.core.net.MediaType, Boolean> state);

}
