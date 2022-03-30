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

import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.service.ProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamRestClient;
import org.lecturestudio.web.api.stream.model.Course;

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
	 * Gets a list of all courses associated with a user. The user must be
	 * authenticated with a token to the API.
	 *
	 * @return A list of all courses associated with a user.
	 */
	public List<Course> getCourses() {
		return streamRestClient.getCourses();
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
	public void acceptSpeechRequest(long requestId) {
		streamRestClient.acceptSpeechRequest(requestId);
	}

	/**
	 * Reject a speech request with the corresponding ID.
	 *
	 * @param requestId The request ID.
	 */
	public void rejectSpeechRequest(long requestId) {
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
}
