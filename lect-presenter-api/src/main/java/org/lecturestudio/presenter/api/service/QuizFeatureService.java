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

import java.util.function.Consumer;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import org.lecturestudio.presenter.api.client.QuizFeatureClient;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.data.bind.JsonConfig;
import org.lecturestudio.web.api.message.QuizAnswerMessage;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.service.ReactiveProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;

public class QuizFeatureService extends ReactiveProviderService {

	private final QuizFeatureClient featureClient;


	public QuizFeatureService(ServiceParameters parameters, TokenProvider tokenProvider) {
		super(parameters, tokenProvider);

		RestClientBuilder builder = createClientBuilder(parameters);
		builder.property(TokenProvider.class.getName(), tokenProvider);
		builder.register(JsonConfig.class);

		featureClient = builder.build(QuizFeatureClient.class);
	}

	public String startQuiz(long courseId, Quiz quiz) {
		return featureClient.startQuiz(courseId, quiz);
	}

	public void stopQuiz(long courseId) {
		featureClient.stopQuiz(courseId);
	}

	public void subscribe(Consumer<QuizAnswerMessage> onEvent,
			Consumer<Throwable> onError) {
		subscribeSse("/api/publisher/events", QuizAnswerMessage.class, onEvent,
				onError);
	}
}
