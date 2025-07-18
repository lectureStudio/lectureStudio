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

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.ScreenCapturer;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;

import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.presenter.api.event.ScreenShareStateEvent;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;
import org.lecturestudio.web.api.model.ScreenSource;

/**
 * Service responsible for capturing screen or window content as video frames.
 * This service manages the lifecycle of screen/window capture operations and
 * broadcasts captured frames via the application's event bus.
 *
 * @author Alex Andres
 */
public class ScreenCaptureService extends ExecutableBase {

	/** The application context providing access to the event bus and other services. */
	private final ApplicationContext context;

	/** The frame rate for screen capture in frames per second. */
	private int frameRate = 20;

	/** The source screen or window to be captured. */
	private ScreenSource source;

	/** The desktop capturer implementation used to capture frames. */
	private DesktopCapturer capturer;

	/** Scheduled future for regular frame capture tasks. */
	private ScheduledFuture<?> future;


	/**
	 * Creates a new ScreenCaptureService instance.
	 *
	 * @param context The application context providing access to the event bus.
	 */
	public ScreenCaptureService(ApplicationContext context) {
		this.context = context;
	}

	/**
	 * Sets the capture frame rate.
	 *
	 * @param frameRate The frame rate in frames per second.
	 */
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	/**
	 * Gets the current capture frame rate.
	 *
	 * @return The current frame rate in frames per second.
	 */
	public int getFrameRate() {
		return frameRate;
	}

	/**
	 * Gets the currently selected screen source.
	 *
	 * @return The current screen source to capture.
	 */
	public ScreenSource getScreenSource() {
		return source;
	}

	/**
	 * Sets the screen source to be captured.
	 *
	 * @param source The screen source to capture.
	 */
	public void setScreenSource(ScreenSource source) {
		this.source = source;
	}

	@Override
	protected void initInternal() {
		// Nothing to do here
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (isNull(source)) {
			throw new ExecutableException("Source must be set");
		}

		capturer = source.isWindow() ? new WindowCapturer() : new ScreenCapturer();

		try {
			capturer.selectSource(new DesktopSource(source.getTitle(), source.getId()));
			capturer.setFocusSelectedSource(true);
			capturer.start((result, videoFrame) -> {
				// NOTE: Avoid asynchronous access to the VideoFrame, otherwise the app will crash.
				//       For asynchronous access, the VideoFrame must be copied and released after processing.
				context.getEventBus().post(new LocalScreenVideoFrameEvent(videoFrame, BigInteger.ZERO));

				// Release the VideoFrame to avoid memory leaks.
				videoFrame.release();
			});
		}
		catch (Exception | Error e) {
			throw new ExecutableException(e);
		}

		future = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			if (started()) {
				synchronized (capturer) {
					capturer.captureFrame();
				}
			}
		}, 0, 1000 / frameRate, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void stopInternal() {
		future.cancel(true);

		synchronized (capturer) {
			capturer.dispose();
		}
	}

	@Override
	protected void destroyInternal() {
		// Nothing to do here
	}

	@Override
	protected void fireStateChanged() {
		super.fireStateChanged();

		context.getEventBus()
				.post(new ScreenShareStateEvent(getScreenSource(), getState()));
	}
}
