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
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.presenter.command.SaveRecordingCommand;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.QuitRecordingView;

public class QuitRecordingPresenter extends Presenter<QuitRecordingView> {

	private final RecordingService recordingService;

	/** The action that is executed when the saving process has been aborted. */
	private Action abortAction;

	/** The action that is executed when the user has decided to discard the recording. */
	private Action discardRecordingAction;

	/** The action that is executed when the recording should be saved. */
	private Action saveRecordingAndQuitAction;


	@Inject
	QuitRecordingPresenter(ApplicationContext context, QuitRecordingView view, RecordingService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		try {
			if (recordingService.started()) {
				recordingService.suspend();
			}
		}
		catch (ExecutableException e) {
			handleException(e, "Pause recording failed", "recording.pause.error");
			return;
		}

		setOnAbort(() -> {
			try {
				if (recordingService.suspended()) {
					recordingService.start();
				}
			}
			catch (ExecutableException e) {
				Throwable cause = nonNull(e.getCause()) ? e.getCause().getCause() : null;

				if (cause instanceof AudioDeviceNotConnectedException) {
					var ex = (AudioDeviceNotConnectedException) cause;
					showError("recording.start.error", "recording.start.device.error", ex.getDeviceName());
					logException(e, "Start recording failed");
				}
				else {
					handleException(e, "Start recording failed", "recording.start.error");
				}
			}
			finally {
				close();
			}
		});

		view.setOnAbort(this::abort);
		view.setOnDiscardRecording(this::discardRecording);
		view.setOnSaveRecording(this::saveRecording);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnAbort(Action action) {
		abortAction = Action.concatenate(abortAction, action);
	}

	public void setOnDiscardRecording(Action action) {
		discardRecordingAction = Action.concatenate(discardRecordingAction, action);
	}

	public void setOnSaveRecording(Action action) {
		saveRecordingAndQuitAction = Action.concatenate(saveRecordingAndQuitAction, action);
	}

	private void abort() {
		abortAction.execute();
	}

	private void discardRecording() {
		try {
			recordingService.discardRecording();

			close();

			if (nonNull(discardRecordingAction)) {
				discardRecordingAction.execute();
			}
		}
		catch (ExecutableException e) {
			handleException(e, "Discard recording failed", "recording.discard.error");
		}
	}

	private void saveRecording() {
		try {
			recordingService.stop();
		}
		catch (ExecutableException e) {
			handleException(e, "Stop recording failed", "recording.stop.error");
			return;
		}
		finally {
			close();
		}

		context.getEventBus().post(new SaveRecordingCommand(saveRecordingAndQuitAction));
	}
}