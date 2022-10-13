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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.presenter.command.NotificationCommand;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.command.StartCourseFeatureCommand;
import org.lecturestudio.presenter.api.presenter.command.StartStreamCommand;
import org.lecturestudio.web.api.model.ScreenSource;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.stream.model.Course;

@Singleton
public class StreamService {

	private final static Logger LOG = LogManager.getLogger(StreamService.class);

	private final PresenterContext context;

	private final EventBus eventBus;

	private final WebRtcStreamService streamService;

	private final WebService webService;


	@Inject
	public StreamService(PresenterContext context,
			WebRtcStreamService streamService, WebService webService) {
		this.context = context;
		this.eventBus = context.getEventBus();
		this.streamService = streamService;
		this.webService = webService;
	}

	public void enableStream(boolean enable) {
		if (enable) {
			startStream();
		}
		else {
			stopStream();
		}
	}

	public ScreenSource getScreenSource() {
		return streamService.getScreenSource();
	}

	public void setScreenSource(ScreenSource screenSource) {
		streamService.setScreenSource(screenSource);
	}

	public ExecutableState getScreenShareState() {
		return streamService.getScreenShareState();
	}

	public void enableScreenSharing(boolean enable) {
		CompletableFuture.runAsync(() -> {
			if (enable) {
				startScreenSharing();
			}
			else {
				stopScreenSharing();
			}
		});
	}

	public void enableStreamCamera(boolean enable) {
		CompletableFuture.runAsync(() -> {
			if (enable) {
				startStreamCamera();
			}
			else {
				stopStreamCamera();
			}
		});
	}

	public void enableMessenger(boolean enable) {
		CompletableFuture.runAsync(() -> {
			if (enable) {
				startMessenger();
			}
			else {
				stopMessenger();
			}
		});
	}

	public void startQuiz(Quiz quiz) {
		if (streamService.started()) {
			startQuizInternal(quiz);
		}
		else {
			Course course = webService.hasActiveService() ?
					context.getCourse() :
					null;

			eventBus.post(new StartCourseFeatureCommand(course, () -> {
				startQuizInternal(quiz);
			}));
		}
	}

	public void stopQuiz() {
		CompletableFuture.runAsync(() -> {
			try {
				webService.stopQuiz();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Stop quiz failed", "quiz.stop.error");
			return null;
		});
	}

	private void startQuizInternal(Quiz quiz) {
		CompletableFuture.runAsync(() -> {
			try {
				webService.startQuiz(quiz);
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Start quiz failed", "quiz.start.error");
			return null;
		});
	}

	private void startStream() {
		Course course = webService.hasActiveService() ?
				context.getCourse() :
				null;

		eventBus.post(new StartStreamCommand(course, (streamContext) -> {
			CompletableFuture.runAsync(() -> {
				try {
					streamService.start();
				}
				catch (ExecutableException e) {
					throw new CompletionException(e);
				}

				if (streamContext.getMessengerEnabled()) {
					context.setMessengerStarted(true);
				}
			})
			.exceptionally(e -> {
				handleServiceError(e, "Start stream failed", "stream.start.error");

				context.setStreamStarted(false);

				return null;
			});
		}));
	}

	private void stopStream() {
		CompletableFuture.runAsync(() -> {
			if (!streamService.started()) {
				return;
			}

			try {
				streamService.stop();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}

			context.setMessengerStarted(false);
		})
		.exceptionally(e -> {
			handleServiceError(e, "Stop stream failed", "stream.stop.error");
			return null;
		});
	}

	private void startScreenSharing() {
		try {
			streamService.startScreenShare();
		}
		catch (ExecutableException e) {
			handleException(e, "Start screen-share failed", "stream.screen.share.error");
		}
	}

	private void stopScreenSharing() {
		try {
			streamService.stopScreenShare();
		}
		catch (ExecutableException e) {
			handleException(e, "Stop screen-share failed", "stream.screen.share.error");
		}
	}

	private void startStreamCamera() {
		try {
			streamService.startCameraStream();
		}
		catch (ExecutableException e) {
			handleException(e, "Start camera stream failed", "stream.start.error");
		}
	}

	private void stopStreamCamera() {
		try {
			streamService.stopCameraStream();
		}
		catch (ExecutableException e) {
			handleException(e, "Stop camera stream failed", "stream.stop.error");
		}
	}

	private void startMessenger() {
		if (streamService.started()) {
			startMessengerInternal();
		}
		else {
			Course course = webService.hasActiveService() ?
					context.getCourse() :
					null;

			eventBus.post(new StartCourseFeatureCommand(course, () -> {
				startMessengerInternal();
			}));
		}
	}

	private void startMessengerInternal() {
		try {
			webService.startMessenger();
		}
		catch (ExecutableException e) {
			handleServiceError(e, "Start messenger failed", "messenger.start.error");

			context.setMessengerStarted(false);
		}
	}

	private void stopMessenger() {
		try {
			webService.stopMessenger();
		}
		catch (ExecutableException e) {
			handleServiceError(e, "Stop messenger failed", "messenger.stop.error");
		}
	}

	private void handleServiceError(Throwable error, String errorMessage, String title) {
		String message = null;

		if (NetUtils.isSocketTimeout(error.getCause())) {
			message = "service.timeout.error";
		}

		handleException(error, errorMessage, title, message);
	}

	protected void handleException(Throwable throwable, String throwMessage, String title) {
		handleException(throwable, throwMessage, title, null);
	}

	final protected void handleException(Throwable throwable, String throwMessage, String title, String message) {
		logException(throwable, throwMessage);

		showError(title, message);
	}

	final protected void logException(Throwable throwable, String throwMessage) {
		requireNonNull(throwable);
		requireNonNull(throwMessage);

		LOG.error(throwMessage, throwable);
	}

	final protected void showError(String title, String message) {
		requireNonNull(title);

		if (context.getDictionary().contains(title)) {
			title = context.getDictionary().get(title);
		}
		if (context.getDictionary().contains(message)) {
			message = context.getDictionary().get(message);
		}

		context.getEventBus().post(new NotificationCommand(NotificationType.ERROR, title, message));
	}
}
