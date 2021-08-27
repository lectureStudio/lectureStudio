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
import org.lecturestudio.presenter.api.client.MessageFeatureClient;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.data.bind.JsonConfig;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.service.ReactiveProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;

public class MessageFeatureService extends ReactiveProviderService {

	private final MessageFeatureClient featureClient;


	public MessageFeatureService(ServiceParameters parameters, TokenProvider tokenProvider) {
		super(parameters, tokenProvider);

		RestClientBuilder builder = createClientBuilder(parameters);
		builder.property(TokenProvider.class.getName(), tokenProvider);
		builder.register(JsonConfig.class);

		featureClient = builder.build(MessageFeatureClient.class);
	}

	public String startMessenger(long courseId) {
		return featureClient.startMessenger(courseId);
	}

	public void stopMessenger(long courseId) {
		featureClient.stopMessenger(courseId);
	}

	public void subscribe(Consumer<MessengerMessage> onEvent,
			Consumer<Throwable> onError) {
		subscribeSse("/api/publisher/events", MessengerMessage.class, onEvent,
				onError);
	}
}
