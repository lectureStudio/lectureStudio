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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.presenter.api.util.HtmlMessageLogger;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.messenger.MessengerConfig;

public class MessageFeatureWebService extends FeatureServiceBase {

	/** Received message consumer. */
	private final Consumer<MessengerMessage> messageConsumer = this::onMessage;

	/** The web service client. */
	private final MessageFeatureService webService;

	/** A message logger. */
	private HtmlMessageLogger logger;

	private MessengerConfig config;


	/**
	 * Creates a new {@link MessageFeatureWebService}.
	 *
	 * @param context        The application context.
	 * @param featureService The message web feature service.
	 */
	public MessageFeatureWebService(ApplicationContext context,
									MessageFeatureService featureService, MessengerConfig config) {
		super(context);

		this.webService = featureService;
		this.config = config;
	}

	@Override
	protected void initInternal() {

	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			serviceId = webService.startMessenger(courseId, config.getMessengerMode());

			//webService.addMessageListener(MessengerMessage.class, messageConsumer);
			webService.addStompMessageListener(MessengerMessage.class, messageConsumer);
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		createLogFile();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			webService.removeMessageListener(MessengerMessage.class, messageConsumer);
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
		logMessage(message);

		// Forward message to UI.
		context.getEventBus().post(message);
	}

	/**
	 * Creates a new log file. The log file is parsed and written in the HTML
	 * format.
	 */
	private void createLogFile() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH.mm");
		String date = dateFormat.format(new Date());

		String name = date + ".html";
		File messengerLog = new File(context.getDataLocator().toAppDataPath(name));

		logger = new HtmlMessageLogger(messengerLog);
	}

	/**
	 * Adds a message to the log file.
	 *
	 * @param message The descriptive message.
	 */
	private void logMessage(MessengerMessage message) {
		logger.logMessage(message.getRemoteAddress(), message.getDate(),
				message.getMessage().getText());
	}
}
