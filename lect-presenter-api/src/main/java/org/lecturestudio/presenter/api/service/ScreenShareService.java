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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.web.api.model.ScreenSource;

@Singleton
public class ScreenShareService {

	private static final Logger LOG = LogManager.getLogger(ScreenShareService.class);

	private final Map<ScreenSource, ScreenRecorderService> recorderServices = new HashMap<>();

	private final PresenterContext context;

	private final AudioSystemProvider audioSystemProvider;

	private ScreenRecorderService activeRecorderService;

	private ScreenCaptureService screenCaptureService;


	@Inject
	public ScreenShareService(PresenterContext context,
			AudioSystemProvider audioSystemProvider) {
		this.context = context;
		this.audioSystemProvider = audioSystemProvider;
	}

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
	}

	public boolean isScreenCaptureActive() {
		return nonNull(screenCaptureService) && screenCaptureService.started();
	}
}
