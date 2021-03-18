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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.QuizStateEvent;
import org.lecturestudio.presenter.api.net.LocalBroadcaster;
import org.lecturestudio.web.api.model.quiz.Quiz;

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

	private Quiz lastQuiz;

	private QuizWebService quizWebService;

	private MessageWebService messageWebService;


	@Inject
	public WebService(ApplicationContext context, DocumentService documentService, LocalBroadcaster localBroadcaster) {
		this.context = context;
		this.documentService = documentService;
		this.localBroadcaster = localBroadcaster;
	}

	/**
	 * Starts the message service and the HTTP server if no quiz service is
	 * running at that time.
	 *
	 * @throws ExecutableException if the message service could not be started.
	 */
	public void startMessenger() throws ExecutableException {
		if (nonNull(messageWebService) && messageWebService.started()) {
			throw new ExecutableException("Message service is already running.");
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Starting));

		startLocalBroadcaster();

		messageWebService = new MessageWebService(context);
		messageWebService.start();

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Started));
	}

	/**
	 * Stops the message service. If no quiz has been started this call will
	 * also stop the HTTP server.
	 *
	 * @throws ExecutableException if the message service could not be stopped.
	 */
	public void stopMessenger() throws ExecutableException {
		if (isNull(messageWebService)) {
			return;
		}

		context.getEventBus().post(new MessengerStateEvent(ExecutableState.Stopping));

		messageWebService.stop();
		messageWebService.destroy();
		messageWebService = null;

		stopLocalBroadcaster();

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
		if (isNull(quizWebService)) {
			throw new NullPointerException("Quiz service is not running");
		}

		quizWebService.saveQuizResult(files, callback);
	}

	/**
	 * Starts the quiz service and the HTTP server if no message service is
	 * running at that time.
	 *
	 * @throws ExecutableException if the quiz service could not be started.
	 */
	public void startQuiz(Quiz quiz) throws ExecutableException {
		context.getEventBus().post(new QuizStateEvent(ExecutableState.Starting));

		Document oldQuizDoc = null;

		// Allow only one quiz running at a time.
		if (isNull(quizWebService)) {
			startLocalBroadcaster();

			quizWebService = new QuizWebService(context);
		}
		else {
			oldQuizDoc = quizWebService.getQuizDocument();

			quizWebService.stop();
			quizWebService.destroy();
		}

		quizWebService.setQuiz(quiz);
		quizWebService.start();

		// Add quiz document.
		Document newQuizDoc = quizWebService.getQuizDocument();

		// Replace quiz document in silent-mode.
		if (documentService.getDocuments().contains(oldQuizDoc)) {
			documentService.getDocuments().replace(oldQuizDoc, newQuizDoc);
		}
		else {
			documentService.addDocument(newQuizDoc);
		}

		documentService.selectDocument(newQuizDoc);

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
		if (isNull(quizWebService)) {
			return;
		}

		context.getEventBus().post(new QuizStateEvent(ExecutableState.Stopping));

		// Remove quiz document.
		Document quizDoc = quizWebService.getQuizDocument();

		if (nonNull(quizDoc)) {
			quizDoc.close();

			documentService.closeDocument(quizDoc);
		}

		lastQuiz = null;

		// Stop service.
		quizWebService.stop();
		quizWebService.destroy();
		quizWebService = null;

		stopLocalBroadcaster();

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
		if (nonNull(messageWebService) && messageWebService.started()) {
			messageWebService.stop();

			stopLocalBroadcaster();
		}
		if (nonNull(quizWebService) && quizWebService.started()) {
			quizWebService.stop();

			stopLocalBroadcaster();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		if (nonNull(messageWebService)) {
			messageWebService.destroy();
		}
		if (nonNull(quizWebService)) {
			quizWebService.destroy();
		}
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
		}
	}
}
