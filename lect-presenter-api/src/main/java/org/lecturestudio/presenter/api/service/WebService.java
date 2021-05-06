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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.QuizStateEvent;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.web.api.model.AuthState;
import org.lecturestudio.web.api.model.Classroom;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.service.AuthService;
import org.lecturestudio.web.api.service.ClassroomProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;

/**
 * The {@code WebService} maintains different web services, like {@link QuizWebService}
 * or {@link MessageWebService}. This class is the interface between the UI and the HTTP
 * application server. The {@link LocalBroadcaster} allows to run the web services on
 * a local machine and act as an standalone server.
 *
 * @author Alex Andres
 */
@Singleton
public class WebService extends ExecutableBase {

	private final ApplicationContext context;

	private final LocalBroadcaster localBroadcaster;

	private final DocumentService documentService;

	private final List<WebServiceBase> startedServices;

	private Quiz lastQuiz;

	private String classroomId;

	private ClassroomProviderService classroomWebService;


	@Inject
	public WebService(ApplicationContext context, DocumentService documentService, LocalBroadcaster localBroadcaster) {
		this.context = context;
		this.documentService = documentService;
		this.localBroadcaster = localBroadcaster;
		this.startedServices = new ArrayList<>();
	}

	/**
	 * Starts the message service and the HTTP server if no quiz service is
	 * running at that time.
	 *
	 * @throws ExecutableException if the message service could not be started.
	 */
	public void startMessenger() throws ExecutableException {
		var service = getService(MessageWebService.class);

		if (nonNull(service) && service.started()) {
			throw new ExecutableException("Message service is already running.");
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Starting));

		startLocalBroadcaster();

		startService(new MessageWebService(context));

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Started));
	}

	/**
	 * Stops the message service. If no quiz has been started this call will
	 * also stop the HTTP server.
	 *
	 * @throws ExecutableException if the message service could not be stopped.
	 */
	public void stopMessenger() throws ExecutableException {
		var service = getService(MessageWebService.class);

		if (isNull(service)) {
			return;
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Stopping));

		stopService(service);

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Stopped));
	}

	/**
	 * Writes quiz results to the provided file paths. Each file may have its own
	 * individual file format. If the file format is not supported, the file will
	 * be skipped.
	 *
	 * @param files The files to write to with individual file formats.
	 * @param callback The callback to be invoked on write progress.
	 *
	 * @throws IOException if the quiz results could not be written to the files.
	 * @throws NullPointerException if no quiz service is running.
	 */
	public void saveQuizResult(List<String> files, ProgressCallback callback) throws IOException {
		var service = getService(QuizWebService.class);

		if (isNull(service)) {
			throw new NullPointerException("Quiz service is not running");
		}

		service.saveQuizResult(files, callback);
	}

	/**
	 * Starts the quiz service and the HTTP server if no message service is
	 * running at that time.
	 *
	 * @throws ExecutableException if the quiz service could not be started.
	 */
	public void startQuiz(Quiz quiz) throws ExecutableException {
		var service = getService(QuizWebService.class);

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Starting));

		// Allow only one quiz running at a time.
		if (isNull(service)) {
			startLocalBroadcaster();

			service = new QuizWebService(context, documentService);
		}
		else {
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
		var service = getService(QuizWebService.class);

		if (isNull(service)) {
			return;
		}

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Stopping));

		lastQuiz = null;

		stopService(service);

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Stopped));
	}

	/**
	 * Get's the started and active quiz.
	 *
	 * @return the active quiz.
	 */
	public Quiz getStartedQuiz() {
		return lastQuiz;
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

	private void startLocalBroadcaster() throws ExecutableException {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		String broadcastAddress = netConfig.getBroadcastAddress();
		Integer broadcastPort = netConfig.getBroadcastPort();

		if (NetUtils.isLocalAddress(broadcastAddress, broadcastPort)) {
			localBroadcaster.start();
		}
		else {
			stopLocalBroadcaster();
		}
	}

	private void stopLocalBroadcaster() throws ExecutableException {
		if (localBroadcaster.getState() == ExecutableState.Started) {
			localBroadcaster.stop();
			localBroadcaster.destroy();

			classroomId = null;
		}
	}

	private void startService(WebServiceBase service) throws ExecutableException {
		updateClassroom();

		service.setClassroomId(classroomId);
		service.start();

		if (!startedServices.contains(service)) {
			startedServices.add(service);
		}
	}

	private void stopService(WebServiceBase service) throws ExecutableException {
		service.stop();
		service.destroy();

		startedServices.remove(service);

		//stopLocalBroadcaster();
	}

	private <T> T getService(Class<T> cls) {
		return startedServices.stream()
				.filter(cls::isInstance)
				.map(cls::cast)
				.findFirst()
				.orElse(null);
	}

	private void updateClassroom() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();

		Classroom classroom = new Classroom();
		classroom.setName(config.getClassroomName());
		classroom.setShortName(config.getClassroomShortName());
		classroom.setIpFilterRules(netConfig.getIpFilter().getRules());

		if (isNull(classroomId)) {
			createClassroomService();

			classroomId = classroomWebService.getClassrooms().stream()
					.filter(room -> room.getShortName().equals(classroom.getShortName()))
					.map(Classroom::getUuid).map(UUID::toString).findFirst()
					.orElse(null);

			if (isNull(classroomId)) {
				classroomId = classroomWebService.createClassroom(classroom);
			}
		}
		else {
			classroom.setUuid(UUID.fromString(classroomId));

			classroomWebService.updateClassroom(classroom);
		}
	}

	private void createClassroomService() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		String broadcastAddress = netConfig.getBroadcastAddress();
		int broadcastPort = netConfig.getBroadcastTlsPort();

		ServiceParameters params = new ServiceParameters();
		params.setUrl(String.format("https://%s:%d", broadcastAddress, broadcastPort));

		AuthService authService = new AuthService(params);
		AuthState.getInstance().setToken(authService.authenticate());

		classroomWebService = new ClassroomProviderService(params);
	}
}
