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

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.web.api.message.MessengerDirectMessage;
import org.lecturestudio.web.api.message.MessengerMessage;

public class MessageFeatureWebService extends FeatureServiceBase {

	/** Received message consumer. */
	private final Consumer<MessengerMessage> messageConsumer = this::onMessage;

	private final Consumer<MessengerDirectMessage> directMessageConsumer = this::onDirectMessage;

	/** The web service client. */
	private final MessageFeatureService webService;


	/**
	 * Creates a new {@link MessageFeatureWebService}.
	 *
	 * @param context        The application context.
	 * @param featureService The message web feature service.
	 */
	public MessageFeatureWebService(PresenterContext context,
			MessageFeatureService featureService) {
		super(context);

		this.webService = featureService;
	}

	@Override
	protected void initInternal() {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			serviceId = webService.startMessenger(courseId);

			webService.addMessageListener(MessengerDirectMessage.class, directMessageConsumer);
			webService.addMessageListener(MessengerMessage.class, messageConsumer);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			webService.removeMessageListener(MessengerMessage.class, messageConsumer);
			webService.removeMessageListener(MessengerDirectMessage.class, directMessageConsumer);
			webService.stopMessenger(courseId);
			// Stop receiving message events.
			webService.close();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() {

	}

	private void onMessage(MessengerMessage message) {
		// Forward message to interested recipients.
		context.getEventBus().post(message);
	}

	private void onDirectMessage(MessengerDirectMessage message) {
		// Forward message to interested recipients.
		context.getEventBus().post(message);
	}
}
