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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
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
import org.lecturestudio.presenter.api.handler.PreviewStreamHandler;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.presenter.api.presenter.command.StartCourseFeatureCommand;
import org.lecturestudio.presenter.api.presenter.command.StartStreamCommand;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.stream.model.Course;

@Singleton
public class StreamService {

	private static final Logger LOG = LogManager.getLogger(StreamService.class);

	private final PresenterContext context;

	private final EventBus eventBus;

	private final WebRtcStreamService webRtcStreamService;

	private final WebService webService;


	@Inject
	public StreamService(PresenterContext context,
			WebRtcStreamService webRtcStreamService, WebService webService) {
		this.context = context;
		this.eventBus = context.getEventBus();
		this.webRtcStreamService = webRtcStreamService;
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

	public void setScreenShareContext(ScreenShareContext context) {
		webRtcStreamService.setScreenShareContext(context);
	}

	public ExecutableState getScreenShareState() {
		return webRtcStreamService.getScreenShareState();
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
		if (webRtcStreamService.started()) {
			startQuizInternal(quiz);
		}
		else {
			Course course = webService.hasActiveService() ?
					context.getCourse() :
					null;

			eventBus.post(new StartCourseFeatureCommand(course,
					() -> {
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
			String errorMessage = null;

			Throwable cause = e.getCause();
			while (nonNull(cause)) {
				// A quiz may have references to files, e.g. images.
				// Be specific when showing the error message to the user.
				if (cause instanceof FileNotFoundException) {
					errorMessage = cause.getLocalizedMessage();
					break;
				}

				cause = cause.getCause();
			}

			handleServiceError(e, "Start quiz failed", "quiz.start.error", errorMessage);
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
					webService.initMessageTransport();
					webService.startMessageTransport();

					// Add the preview-handler here as it will observe stream
					// state events and execute properly when the stream is running.
					if (streamContext.getPreviewStream()) {
						var previewStreamHandler = new PreviewStreamHandler(context);
						previewStreamHandler.initialize();
					}
					if (!context.getConfiguration().getStreamConfig().getMicrophoneEnabled()) {
						context.getManualStateObserver().setMicrophoneActive(false);
					}

					webRtcStreamService.getStreamConfig().setStartChat(streamContext.getMessengerEnabled());
					webRtcStreamService.start();
				}
				catch (ExecutableException e) {
					throw new CompletionException(e);
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
			if (!webRtcStreamService.started()) {
				return;
			}

			try {
				webRtcStreamService.stop();
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
			webRtcStreamService.startScreenShare();
		}
		catch (ExecutableException e) {
			handleException(e, "Start screen-share failed", "stream.screen.share.error");
		}
	}

	private void stopScreenSharing() {
		try {
			webRtcStreamService.stopScreenShare();
		}
		catch (ExecutableException e) {
			handleException(e, "Stop screen-share failed", "stream.screen.share.error");
		}
	}

	private void startStreamCamera() {
		try {
			webRtcStreamService.startCameraStream();
		}
		catch (ExecutableException e) {
			handleException(e, "Start camera stream failed", "stream.start.error");
		}
	}

	private void stopStreamCamera() {
		try {
			webRtcStreamService.stopCameraStream();
		}
		catch (ExecutableException e) {
			handleException(e, "Stop camera stream failed", "stream.stop.error");
		}
	}

	private void startMessenger() {
		if (webRtcStreamService.started() || webRtcStreamService.getState() == ExecutableState.Starting) {
			startMessengerInternal();
		}
		else {
			Course course = webService.hasActiveService() ?
					context.getCourse() :
					null;

			eventBus.post(new StartCourseFeatureCommand(course,
					() -> {
						// On start.
						startMessengerInternal();
					},
					() -> {
						// On abort.
						context.setMessengerStarted(false);
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

	private void handleServiceError(Throwable error, String errorMessage, String title, String message) {
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
