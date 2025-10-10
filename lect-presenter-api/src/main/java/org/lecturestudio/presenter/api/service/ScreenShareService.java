/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.eventbus.Subscribe;

import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;
import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.ScreenCapturer;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;
import org.lecturestudio.web.api.model.ScreenSource;

/**
 * Service responsible for managing screen sharing, recording, and capturing functionality.
 * <p>
 * This service coordinates between different screen sources, managing the lifecycle
 * of screen recorder services and screen capture services. It ensures that:
 * <ul>
 *   <li>Only one recorder service is active at any given time</li>
 *   <li>Recording and capture services are properly initialized and managed</li>
 *   <li>Resources are cleaned up when recordings or captures are stopped</li>
 * </ul>
 * <p>
 * The service maintains a registry of screen recorder services, each associated with a
 * specific screen source, allowing users to switch between sources while preserving state.
 *
 * @author Alex Andres
 */
@Singleton
public class ScreenShareService {

	private static final Logger LOG = LogManager.getLogger(ScreenShareService.class);

	/** Ensures thread-safe operations when accessing or modifying the video frame. */
	private final Object frameLock = new Object();

	/** Map associating screen sources with their respective recorder services. */
	private final Map<ScreenSource, ScreenRecorderService> recorderServices = new HashMap<>();

	/** The presenter context providing access to application configuration and event bus. */
	private final PresenterContext context;

	/** Provider for audio system functionality used during screen recording. */
	private final AudioSystemProvider audioSystemProvider;

	/** Reference to the currently active screen recorder service. Only one recorder service can be active at a time. */
	private ScreenRecorderService activeRecorderService;

	/** Service responsible for capturing and sharing screen content without recording. */
	private ScreenCaptureService screenCaptureService;

	/** Stores the most recently captured video frame. */
	private VideoFrame lastFrame;


	/**
	 * Constructs a new ScreenShareService with the specified context and audio system provider.
	 *
	 * @param context             The presenter context used for event bus access and configuration.
	 * @param audioSystemProvider The provider for audio system functionality.
	 */
	@Inject
	public ScreenShareService(PresenterContext context, AudioSystemProvider audioSystemProvider) {
		this.context = context;
		this.audioSystemProvider = audioSystemProvider;

		context.getEventBus().register(this);
	}

	@Subscribe
	public void onEvent(LocalScreenVideoFrameEvent event) {
		setVideoFrame(event.getFrame());
	}

	/**
	 * Starts screen recording using the provided share context.
	 * <p>
	 * This method creates a new screen recorder service for the specified source if one
	 * doesn't exist yet. If the service for the specified source is already running,
	 * this method returns without taking any action.
	 * <p>
	 * If another recorder service is currently active, it will be suspended before
	 * starting the new recording. The newly started service becomes the active recorder.
	 *
	 * @param shareContext The context containing screen source and configuration for recording.
	 */
	public void startScreenRecording(ScreenShareContext shareContext) {
		ScreenSource source = shareContext.getSource();
		ScreenRecorderService service = recorderServices.get(source);

		if (isNull(service)) {
			service = new ScreenRecorderService(context, audioSystemProvider);

			// Register the recorder for the given source.
			recorderServices.put(source, service);
		}

		if (service.started()) {
			return;
		}

		// Suspend the potentially active screen recorder.
		if (service != activeRecorderService && nonNull(activeRecorderService)
				&& activeRecorderService.started()) {
			try {
				activeRecorderService.suspend();
			}
			catch (ExecutableException e) {
				LOG.error("Suspend screen-recorder failed", e);
			}
		}

		try {
			service.setScreenShareContext(shareContext);
			service.start();

			activeRecorderService = service;

			context.getEventBus().post(new RecordActionEvent(service.getScreenAction()));
		}
		catch (ExecutableException e) {
			LOG.error("Stop screen-recorder failed", e);
		}
	}

	/**
	 * Suspends the active screen recording if one exists and is running.
	 * <p>
	 * This method checks if there is an active recorder service that is currently running.
	 * If such a service exists, it attempts to suspend it. If an exception occurs during
	 * the suspension process, it is caught and logged.
	 * <p>
	 * If there is no active recorder service, or if it's not running, this method returns
	 * without taking any action.
	 */
	public void suspendScreenRecording() {
		if (isNull(activeRecorderService) || !activeRecorderService.started()) {
			return;
		}

		try {
			activeRecorderService.suspend();
		}
		catch (ExecutableException e) {
			LOG.error("Suspend screen-recorder failed", e);
		}
	}

	/**
	 * Stops the active screen recording and removes it from the service registry.
	 * <p>
	 * This method checks if there is an active recorder service that is either
	 * suspended or started. If such a service exists, it attempts to stop it.
	 * If an exception occurs during the stopping process, it is caught and logged.
	 * After stopping, the service is removed from the recorder services registry.
	 * <p>
	 * If there is no active recorder service, or if it's neither suspended nor
	 * started, this method returns without taking any action.
	 */
	public void stopScreenRecording() {
		if (isNull(activeRecorderService) || !(activeRecorderService.suspended()
				|| activeRecorderService.started())) {
			return;
		}

		try {
			activeRecorderService.stop();
		}
		catch (ExecutableException e) {
			LOG.error("Stop screen-recorder failed", e);
		}

		ScreenSource source = activeRecorderService.getScreenShareContext()
				.getSource();

		recorderServices.remove(source);
	}

	/**
	 * Stops all active screen recordings and clears associated resources.
	 * <p>
	 * This method iterates through all registered screen recorder services and
	 * attempts to stop any that are either suspended or actively running.
	 * Any exceptions that occur during the stopping process are caught and logged.
	 * After all services are stopped, the active recorder service reference is
	 * cleared, and all service registrations are removed.
	 */
	public void stopAllScreenRecordings() {
		for (ScreenRecorderService service : recorderServices.values()) {
			try {
				if (service.suspended() || service.started()) {
					service.stop();
				}
			}
			catch (ExecutableException e) {
				LOG.error("Stop screen-recorder failed", e);
			}
		}

		activeRecorderService = null;
		recorderServices.clear();
	}

	/**
	 * Starts a screen capture process using the provided share context.
	 * <p>
	 * This method initializes the screen capture service if it doesn't exist yet.
	 * If a screen capture is already in progress, it will be stopped before starting
	 * a new capture. The screen source and frame rate are configured based on the
	 * provided share context.
	 *
	 * @param shareContext The context containing screen source and profile settings for the capture.
	 *
	 * @throws ExecutableException If the screen capture fails to start.
	 */
	public void startScreenCapture(ScreenShareContext shareContext)
			throws ExecutableException {
		if (isNull(screenCaptureService)) {
			screenCaptureService = new ScreenCaptureService(context);
		}
		if (screenCaptureService.started()) {
			screenCaptureService.stop();
		}

		try {
			screenCaptureService.setScreenSource(shareContext.getSource());
			screenCaptureService.setFrameRate(shareContext.getProfile().getFramerate());
			screenCaptureService.start();
		}
		catch (ExecutableException e) {
			LOG.error("Start local screen share failed", e);
			throw e;
		}
	}

	/**
	 * Stops the active screen capture process. If no screen capture service exists,
	 * or it's not running, this method returns without action.
	 * Any exceptions that occur during the stopping process are caught and logged.
	 */
	public void stopScreenCapture() {
		if (isNull(screenCaptureService) || !screenCaptureService.started()) {
			return;
		}

		try {
			screenCaptureService.stop();
		}
		catch (ExecutableException e) {
			LOG.error("Stop local screen share failed", e);
		}

		disposeLastVideoFrame();
	}

	/**
	 * Checks if screen capture is currently active.
	 *
	 * @return {@code true} if the screen-capture service exists and is running.
	 */
	public boolean isScreenCaptureActive() {
		return nonNull(screenCaptureService) && screenCaptureService.started();
	}

	/**
	 * Checks if a specific screen source is available on the system.
	 * <p>
	 * This method verifies whether a given screen source (either a window or screen)
	 * is currently available by querying the appropriate desktop capturer.
	 *
	 * @param source The screen source to check for availability.
	 *
	 * @return {@code true} if a matching source with the same title is found in the system.
	 */
	public boolean isScreenSourceAvailable(ScreenSource source) {
		DesktopCapturer desktopCapturer = source.isWindow() ? new WindowCapturer() : new ScreenCapturer();
		DesktopSource desktopSource = new DesktopSource(source.getTitle(), source.getId());

		boolean found = desktopCapturer.getDesktopSources().stream()
				.anyMatch(_desktopSource -> _desktopSource.equals(desktopSource));

		desktopCapturer.dispose();

		return found;
	}

	/**
	 * Retrieves the most recent video frame captured by the screen capture service.
	 * <p>
	 * This method provides access to the last frame that was captured during an active
	 * screen sharing session. If the screen capture service isn't initialized or running,
	 * this method will return null.
	 *
	 * @return The most recent {@code VideoFrame} captured by the screen capture service,
	 *         or {@code null} if no screen-capture service exists.
	 */
	public VideoFrame getLastCapturedVideoFrame() {
		synchronized (frameLock) {
			return lastFrame;
		}
	}

	private void setVideoFrame(VideoFrame videoFrame) {
		VideoFrameBuffer buffer = videoFrame.buffer;

		// This prevents processing and storing frames with insufficient visual content,
		// e.g., when a window was minimized.
		if (buffer.getWidth() <= 10 || buffer.getHeight() <= 10) {
			return;
		}

		synchronized (frameLock) {
			if (nonNull(lastFrame)) {
				lastFrame.release();
			}
			lastFrame = videoFrame.copy();
		}
	}

	private void disposeLastVideoFrame() {
		synchronized (frameLock) {
			if (nonNull(lastFrame)) {
				lastFrame.release();
				lastFrame = null;
			}
		}
	}
}
