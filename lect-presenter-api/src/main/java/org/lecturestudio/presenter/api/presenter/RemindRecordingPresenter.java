/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.presenter.command.StartRecordingCommand;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.RemindRecordingView;

public class RemindRecordingPresenter extends Presenter<RemindRecordingView> {

	private final RecordingService recordingService;


	@Inject
	protected RemindRecordingPresenter(ApplicationContext context,
			RemindRecordingView view, RecordingService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		Dictionary dict = context.getDictionary();

		view.setType(NotificationType.QUESTION);
		view.setTitle(dict.get("recording.notification.title"));
		view.setMessage(dict.get("recording.notification.message"));
		view.setOnRecord(this::record);
		view.setOnClose(this::close);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void record() {
		close();

		PresenterContext pContext = (PresenterContext) context;

		CompletableFuture.runAsync(() -> {
			context.getEventBus().post(new StartRecordingCommand(() -> {
				try {
					recordingService.start();
				}
				catch (ExecutableException e) {
					throw new CompletionException(e);
				}

				pContext.setRecordingStarted(true);
			}));
		})
		.exceptionally(e -> {
			handleRecordingStateError(e);
			pContext.setRecordingStarted(false);
			return null;
		});
	}

	private void handleRecordingStateError(Throwable e) {
		Throwable cause = nonNull(e.getCause()) ?
				e.getCause().getCause() :
				null;

		if (cause instanceof AudioDeviceNotConnectedException ex) {
			context.showError("recording.start.error",
					"recording.start.device.error", ex.getDeviceName());
			logException(e, "Start recording failed");
		}
		else {
			handleException(e, "Start recording failed",
					"recording.start.error");
		}
	}
}
