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

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.screencapture.ScreenCaptureFormat;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.event.RecordingTimeEvent;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Objects.nonNull;

@Singleton
public class RecordingService extends ExecutableBase {

	private final ApplicationContext context;

	private final FileLectureRecorder fileLectureRecorder;
	private IdleTimer recordingTimer;


	@Inject
	public RecordingService(ApplicationContext context, FileLectureRecorder fileLectureRecorder) {
		this.context = context;
		this.fileLectureRecorder = fileLectureRecorder;

		setAudioFormat(context.getConfiguration().getAudioConfig().getRecordingFormat());
		setScreenCaptureFormat(context.getConfiguration().getScreenCaptureConfig().getRecordingFormat());

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		config.getAudioConfig().recordingMasterVolumeProperty().addListener((observable, oldValue, newValue)
				-> fileLectureRecorder.setAudioVolume(newValue.doubleValue()));
		config.pageRecordingTimeoutProperty().addListener((o, oldValue, newValue) -> {
			if (nonNull(newValue)) {
				fileLectureRecorder.setPageRecordingTimeout(newValue);
			}
		});
	}

	public void setAudioFormat(AudioFormat audioFormat) {
		fileLectureRecorder.setAudioFormat(audioFormat);
	}

	public void setScreenCaptureFormat(ScreenCaptureFormat format) {
		fileLectureRecorder.setScreenCaptureFormat(format);
	}

	public CompletableFuture<Void> writeRecording(File file, ProgressCallback callback) {
		return CompletableFuture.runAsync(() -> {
			try {
				fileLectureRecorder.writeRecording(file, callback);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	public void discardRecording() throws ExecutableException {
		if (nonNull(recordingTimer)) {
			recordingTimer.stop();
		}

		if (fileLectureRecorder.started() || fileLectureRecorder.suspended()) {
			fileLectureRecorder.stop();
		}

		fileLectureRecorder.discard();
	}

	public void startScreenCapture() throws ExecutableException {
		if (fileLectureRecorder.started()) {
			fileLectureRecorder.startScreenCapture();
		}
	}

	public void stopScreenCapture() throws ExecutableException {
		if (fileLectureRecorder.started()) {
			fileLectureRecorder.stopScreenCapture();
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		fileLectureRecorder.init();
		recordingTimer = new IdleTimer();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (!fileLectureRecorder.started()) {
			if (fileLectureRecorder.error()) {
				// Recover from potential error.
				fileLectureRecorder.stop();
			}

			fileLectureRecorder.start();
			recordingTimer.runTask();
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (fileLectureRecorder.started()) {
			fileLectureRecorder.suspend();
			recordingTimer.stop();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (fileLectureRecorder.started() || fileLectureRecorder.suspended()) {
			recordingTimer.stop();
			fileLectureRecorder.stop();
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		if (!fileLectureRecorder.destroyed()) {
			fileLectureRecorder.destroy();
		}
	}

	private void fireTimeChanged() {
		Time time = new Time(fileLectureRecorder.getElapsedTime());

		context.getEventBus().post(new RecordingTimeEvent(time));
	}



	private class IdleTimer extends Timer {

		private static final int IDLE_TIME = 1000;

		private TimerTask idleTask;


		void runTask() {
			idleTask = new TimerTask() {

				@Override
				public void run() {
					fireTimeChanged();
				}
			};

			schedule(idleTask, 0, IDLE_TIME);
		}

		void stop() {
			if (idleTask != null) {
				idleTask.cancel();
			}

			purge();
		}
	}
}
