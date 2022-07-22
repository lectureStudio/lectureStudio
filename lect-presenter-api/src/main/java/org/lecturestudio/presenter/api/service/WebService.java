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

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.QuizStateEvent;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.message.MessageTransport;
import org.lecturestudio.web.api.message.WebSocketStompTransport;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.websocket.WebSocketStompBearerTokenProvider;
import org.lecturestudio.web.api.websocket.WebSocketStompHeaderProvider;

/**
 * The {@code WebService} maintains different web services, like {@link
 * QuizFeatureWebService} or {@link MessageFeatureWebService}. This class is the
 * interface between the UI and the HTTP application server. The {@link
 * LocalBroadcaster} allows to run the web services on a local machine and act
 * as an standalone server.
 *
 * @author Alex Andres
 */
@Singleton
public class WebService extends ExecutableBase {

	private final ApplicationContext context;

	private final LocalBroadcaster localBroadcaster;

	private final DocumentService documentService;

	private final WebServiceInfo webServiceInfo;

	private final List<FeatureServiceBase> startedServices;

	private MessageTransport messageTransport;

	private Quiz lastQuiz;


	@Inject
	public WebService(ApplicationContext context,
			DocumentService documentService,
			LocalBroadcaster localBroadcaster,
			WebServiceInfo webServiceInfo) {
		this.context = context;
		this.documentService = documentService;
		this.localBroadcaster = localBroadcaster;
		this.webServiceInfo = webServiceInfo;
		this.startedServices = new ArrayList<>();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (var webService : startedServices) {
				if (webService.started()) {
					try {
						webService.stop();
					}
					catch (ExecutableException e) {
						// Ignore
					}
				}
			}
		}));
	}

	/**
	 * Starts the message service and the HTTP server if no quiz service is
	 * running at that time.
	 *
	 * @throws ExecutableException if the message service could not be started.
	 */
	public void startMessenger() throws ExecutableException {
		var service = getService(MessageFeatureWebService.class);

		if (nonNull(service) && service.started()) {
			throw new ExecutableException("Message service is already running");
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Starting));

		try {
			initMessageTransport();

			startService(new MessageFeatureWebService((PresenterContext) context,
					createFeatureService(webServiceInfo.getStreamPublisherApiUrl(),
							MessageFeatureService.class)));
		}
		catch (Exception e) {
			throw new ExecutableException("Message service could not be started", e);
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Started));
	}

	/**
	 * Stops the message service. If no quiz has been started this call will
	 * also stop the HTTP server.
	 *
	 * @throws ExecutableException if the message service could not be stopped.
	 */
	public void stopMessenger() throws ExecutableException {
		var service = getService(MessageFeatureWebService.class);

		if (isNull(service)) {
			return;
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Stopping));

		stopService(service);

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Stopped));
	}

	/**
	 * Starts the quiz service and the HTTP server if no message service is
	 * running at that time.
	 *
	 * @throws ExecutableException if the quiz service could not be started.
	 */
	public void startQuiz(Quiz quiz) throws ExecutableException {
		var service = getService(QuizFeatureWebService.class);

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Starting));

		// Allow only one quiz running at a time.
		if (isNull(service)) {
			try {
				initMessageTransport();

				service = new QuizFeatureWebService((PresenterContext) context,
						createFeatureService(webServiceInfo.getStreamPublisherApiUrl(),
								QuizFeatureService.class), documentService);
			}
			catch (Exception e) {
				throw new ExecutableException("Quiz service could not be started");
			}
		}
		else if (!service.stopped()) {
			service.stop();
		}

		service.setQuiz(quiz);

		startService(service);

		lastQuiz = quiz;

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Started));
	}

	/**
	 * Stops the quiz service. If no message service has been started this call
	 * will also stop the HTTP server.
	 *
	 * @throws ExecutableException if the message service could not be stopped.
	 */
	public void stopQuiz() throws ExecutableException {
		var service = getService(QuizFeatureWebService.class);

		if (isNull(service)) {
			return;
		}

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Stopping));

		lastQuiz = null;

		stopService(service);

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Stopped));
	}

	/**
	 * Gets the started and active quiz.
	 *
	 * @return the active quiz.
	 */
	public Quiz getStartedQuiz() {
		return lastQuiz;
	}

	/**
	 * Check whether a web-service is active.
	 *
	 * @return {@code true} if at least one web-service is active.
	 */
	public boolean hasActiveService() {
		for (var webService : startedServices) {
			if (webService.started()) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void initInternal() {

	}

	@Override
	protected void startInternal() {

	}

	@Override
	protected void stopInternal() throws ExecutableException {
		for (var webService : startedServices) {
			if (webService.started()) {
				webService.stop();
			}
		}

		stopLocalBroadcaster();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		for (var webService : startedServices) {
			webService.destroy();
		}

		startedServices.clear();
	}

	private void stopLocalBroadcaster() throws ExecutableException {
		if (localBroadcaster.getState() == ExecutableState.Started) {
			localBroadcaster.stop();
			localBroadcaster.destroy();
		}
	}

	private void startService(FeatureServiceBase service) throws ExecutableException {
		if (messageTransport.getState() != ExecutableState.Started) {
			messageTransport.start();
		}

		PresenterContext pContext = (PresenterContext) context;

		service.setCourseId(pContext.getCourse().getId());
		service.start();

		if (!startedServices.contains(service)) {
			startedServices.add(service);
		}
	}

	private void stopService(FeatureServiceBase service)
			throws ExecutableException {
		service.stop();
		service.destroy();

		startedServices.remove(service);

		if (startedServices.isEmpty()) {
			messageTransport.stop();
			messageTransport.destroy();
		}

		//stopLocalBroadcaster();
	}

	private <T> T getService(Class<T> cls) {
		return startedServices.stream()
				.filter(cls::isInstance)
				.map(cls::cast)
				.findFirst()
				.orElse(null);
	}

	private <T> T createFeatureService(String streamPublisherApiUrl,
			Class<T> cls) throws Exception {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		ServiceParameters streamApiParameters = new ServiceParameters();
		streamApiParameters.setUrl(streamPublisherApiUrl);

		TokenProvider tokenProvider = streamConfig::getAccessToken;

		return cls.getConstructor(ServiceParameters.class, TokenProvider.class,
						MessageTransport.class)
				.newInstance(streamApiParameters, tokenProvider,
						messageTransport);
	}

	private void initMessageTransport() {
		if (isNull(messageTransport) || messageTransport.destroyed()) {
			messageTransport = createStompMessageTransport();
		}
	}

	private MessageTransport createStompMessageTransport() {
		ServiceParameters messageApiParameters = new ServiceParameters();
		messageApiParameters.setUrl(webServiceInfo.getStreamMessageApiUrl());

		PresenterContext pContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();
		TokenProvider tokenProvider = streamConfig::getAccessToken;

		Course course = pContext.getCourse();

		WebSocketStompHeaderProvider headerProvider = new WebSocketStompBearerTokenProvider(tokenProvider);

		return new WebSocketStompTransport(messageApiParameters, headerProvider, course);
	}
}
