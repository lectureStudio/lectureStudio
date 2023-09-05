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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.nonNull;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.ConfirmStopRecordingView;

public class ConfirmStopRecordingPresenter extends Presenter<ConfirmStopRecordingView> {

	private final RecordingService recordingService;


	@Inject
	ConfirmStopRecordingPresenter(ApplicationContext context,
								  ConfirmStopRecordingView view,
								  RecordingService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		setCloseable(false);

		try {
			if (recordingService.started()) {
				recordingService.suspend();
			}
		}
		catch (ExecutableException e) {
			handleException(e, "Pause recording failed", "recording.pause.error");
			return;
		}

		view.setType(NotificationType.QUESTION);
		view.setTitle(context.getDictionary().get("recording.stop.confirm.title"));
		view.setMessage(context.getDictionary().get("recording.stop.confirm.message"));
		view.setOnStopRecording(this::stopRecording);
		view.setOnContinueRecording(this::continueRecording);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Notification;
	}

	private void continueRecording() {
		try {
			recordingService.start();
		}
		catch (ExecutableException e) {
			Throwable cause = nonNull(e.getCause()) ? e.getCause().getCause() : null;

			if (cause instanceof AudioDeviceNotConnectedException ex) {
				context.showError("recording.start.error", "recording.start.device.error", ex.getDeviceName());
				logException(e, "Start recording failed");
			}
			else {
				handleException(e, "Start recording failed", "recording.start.error");
			}
		}

		setCloseable(true);
		close();
	}

	private void stopRecording() {
		setCloseable(true);
		close();

		try {
			recordingService.stop();

			context.getEventBus().post(new ShowPresenterCommand<>(SaveRecordingPresenter.class));
		}
		catch (ExecutableException e) {
			handleException(e, "Stop recording failed", "recording.stop.error");
		}
	}
}
