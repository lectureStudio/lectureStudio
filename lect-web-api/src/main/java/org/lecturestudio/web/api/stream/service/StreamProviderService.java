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

package org.lecturestudio.web.api.stream.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.model.UserInfo;
import org.lecturestudio.web.api.model.UserPrivileges;
import org.lecturestudio.web.api.service.ProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamRestClient;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

/**
 * Service implementation to manage streaming related information with streaming
 * servers. As of the current implementation this service will communicate with
 * a REST API providing a WebRTC streaming interface.
 *
 * @author Alex Andres
 */
public class StreamProviderService extends ProviderService {

	private final StreamRestClient streamRestClient;

	/**
	 * Creates a new {@code StreamService}.
	 *
	 * @param parameters    The service connection parameters.
	 * @param tokenProvider The access token provider.
	 */
	@Inject
	public StreamProviderService(ServiceParameters parameters,
			TokenProvider tokenProvider) {
		RestClientBuilder builder = createClientBuilder(parameters);
		builder.property(TokenProvider.class.getName(), tokenProvider);

		streamRestClient = builder.build(StreamRestClient.class);
	}

	/**
	 * Get information about the authenticated user.
	 *
	 * @return The user information.
	 */
	public UserInfo getUserInfo() {
		return streamRestClient.getUserInfo();
	}

	/**
	 * Get privileges for the authenticated user.
	 *
	 * @param courseId The unique course ID.
	 *
	 * @return The user privileges.
	 */
	public UserPrivileges getUserPrivileges(long courseId) {
		return streamRestClient.getUserPrivileges(courseId);
	}

	/**
	 * Gets a list of all courses associated with a user. The user must be
	 * authenticated with a token to the API.
	 *
	 * @return A list of all courses associated with a user.
	 */
	public List<Course> getCourses() {
		return streamRestClient.getCourses();
	}

	/**
	 * Gets a list of all participants in an active courses.
	 *
	 * @param courseId The unique course ID.
	 *
	 * @return A list of all participants in an active courses.
	 */
	@GET
	@Path("/participants/{courseId}")
	public List<CourseParticipant> getParticipants(
			@PathParam("courseId") long courseId) {
		return streamRestClient.getParticipants(courseId);
	}

	/**
	 * Uploads the provided multipart data.
	 *
	 * @param data The multipart data to upload.
	 *
	 * @return The file name, if successfully uploaded.
	 */
	public String uploadFile(MultipartBody data) {
		return streamRestClient.uploadFile(data);
	}

	/**
	 * Accept a speech request with the corresponding ID.
	 *
	 * @param requestId The request ID.
	 */
	public void acceptSpeechRequest(UUID requestId) {
		streamRestClient.acceptSpeechRequest(requestId);
	}

	/**
	 * Reject a speech request with the corresponding ID.
	 *
	 * @param requestId The request ID.
	 */
	public void rejectSpeechRequest(UUID requestId) {
		streamRestClient.rejectSpeechRequest(requestId);
	}

	/**
	 * Sets the current recording state of the course.
	 *
	 * @param courseId The unique course ID.
	 * @param recorded True if course is being recorded.
	 */
	public void setCourseRecordingState(long courseId, boolean recorded) {
		streamRestClient.setCourseRecordingState(courseId, recorded);
	}

	/**
	 * Notify course participants that a course stream has been started.
	 *
	 * @param courseId The unique course ID of the course that was started.
	 */
	public void startedStream(long courseId) {
		streamRestClient.startedStream(courseId);
	}

	/**
	 * Notify course participants that a course stream has been restarted. The
	 * restart may have been caused due to an interrupted connection.
	 *
	 * @param courseId The unique course ID of the course that was restarted.
	 */
	public void restartedStream(long courseId) {
		streamRestClient.restartedStream(courseId);
	}

	/**
	 * Update the media stream state for a given streaming session in a course.
	 * The media state is represented by a mapping {@code MediaType to Boolean}
	 * which indicates whether a stream with the given MediaType is
	 * muted/enabled or not.
	 *
	 * @param courseId The unique course ID.
	 * @param state    The current media stream state.
	 */
	public void updateStreamMediaState(long courseId,
			Map<MediaType, Boolean> state) {
		streamRestClient.updateStreamMediaState(courseId, state);
	}
}
