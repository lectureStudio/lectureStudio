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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.presenter.api.event.ScreenShareStateEvent;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;
import org.lecturestudio.web.api.model.ScreenSource;

public class ScreenCaptureService extends ExecutableBase {

	private final ApplicationContext context;

	private int frameRate = 20;

	private ScreenSource source;

	private DesktopCapturer capturer;

	private ScheduledFuture<?> future;


	public ScreenCaptureService(ApplicationContext context) {
		this.context = context;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public ScreenSource getScreenSource() {
		return source;
	}

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
		capturer.selectSource(new DesktopSource(source.getTitle(), source.getId()));
		capturer.setFocusSelectedSource(true);
		capturer.start((result, videoFrame) -> {
			context.getEventBus().post(new LocalScreenVideoFrameEvent(videoFrame));
		});

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
