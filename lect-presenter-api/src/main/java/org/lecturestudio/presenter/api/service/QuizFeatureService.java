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

package org.lecturestudio.presenter.api.service;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import org.lecturestudio.presenter.api.client.QuizFeatureClient;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.message.MessageTransport;
import org.lecturestudio.web.api.service.ReactiveProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;

/**
 * A service that provides quiz functionality for the presenter application.
 * This service extends ReactiveProviderService and interacts with the quiz feature API
 * through a REST client.
 *
 * @author Alex Andres
 */
public class QuizFeatureService extends ReactiveProviderService {

	/** The client used to interact with the quiz feature REST API. */
	private final QuizFeatureClient featureClient;


	/**
	 * Creates a new QuizFeatureService with the specified parameters.
	 *
	 * @param parameters       The service configuration parameters.
	 * @param tokenProvider    The provider for authentication tokens.
	 * @param messageTransport The transport layer for sending/receiving messages.
	 */
	public QuizFeatureService(ServiceParameters parameters,
							  TokenProvider tokenProvider, MessageTransport messageTransport) {
		super(parameters, messageTransport);

		RestClientBuilder builder = createClientBuilder(parameters);
		builder.property(TokenProvider.class.getName(), tokenProvider);

		featureClient = builder.build(QuizFeatureClient.class);
	}

	/**
	 * Starts a quiz for the specified course.
	 *
	 * @param courseId The ID of the course to start the quiz for.
	 * @param data     The quiz data to be sent to the server.
	 *
	 * @return The unique identifier for the started quiz session.
	 */
	public String startQuiz(long courseId, MultipartBody data) {
		return featureClient.startQuiz(courseId, data);
	}

	/**
	 * Stops the currently running quiz for the specified course.
	 *
	 * @param courseId The ID of the course to stop the quiz for.
	 */
	public void stopQuiz(long courseId) {
		featureClient.stopQuiz(courseId);
	}
}
