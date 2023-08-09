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

import static java.util.Objects.nonNull;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioFrame;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.RecordingTimeEvent;
import org.lecturestudio.presenter.api.recording.FileLectureRecorder;

@Singleton
public class RecordingService extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(RecordingService.class);

	private final PresenterContext context;

	private final FileLectureRecorder recorder;

	private final CameraRecordingService camRecorder;

	private WebRtcStreamService streamService;

	private IdleTimer recordingTimer;


	@Inject
	public RecordingService(PresenterContext context, FileLectureRecorder recorder, CameraRecordingService camRecorder) {
		this.context = context;
		this.recorder = recorder;
		this.camRecorder = camRecorder;

		setAudioFormat(context.getConfiguration().getAudioConfig().getRecordingFormat());

		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		config.getAudioConfig().recordingMasterVolumeProperty().addListener((observable, oldValue, newValue) -> {
			recorder.setAudioVolume(newValue.doubleValue());
		});
		config.pageRecordingTimeoutProperty().addListener((o, oldValue, newValue) -> {
			if (nonNull(newValue)) {
				recorder.setPageRecordingTimeout(newValue);
			}
		});
	}

	public void SetWebRTC(WebRtcStreamService streamService) {
		this.streamService = streamService;
		camRecorder.setWebRTC(streamService);
	}

	public void addAudioFrame(final AudioFrame event) {
		recorder.addPeerAudio(event);
	}

	public void setAudioFormat(AudioFormat audioFormat) {
		recorder.setAudioFormat(audioFormat);
	}

	public String getBestRecordingName() {
		return recorder.getBestRecordingName();
	}

	public CompletableFuture<Void> writeRecording(File file,
			ProgressCallback callback) {
		return CompletableFuture.runAsync(() -> {
			try {
				recorder.writeRecording(file, callback);
			} catch (Exception e) {
				throw new CompletionException(e);
			}
			if (context.getConfiguration().getCameraRecordingConfig().isCameraEnabled()) {
				try {
					camRecorder.finishVideoRecordingProcess(file);
				} catch (Exception e) {
					throw new CompletionException(e);
				}
			}
		});
	}

	public void discardRecording() throws ExecutableException {
		if (nonNull(recordingTimer)) {
			recordingTimer.stop();
		}

		if (recorder.started() || recorder.suspended()) {
			recorder.stop();
		}

		recorder.discard();
	}

	@Override
	protected void initInternal() throws ExecutableException {
		recorder.init();
		if (context.getConfiguration().getCameraRecordingConfig().isCameraEnabled()) {
			try {
				camRecorder.init();
			} catch (Exception e) {
				LOG.error("Initialization of CameraRecorderService failed", e);
			}
		}

		recordingTimer = new IdleTimer();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (!recorder.started()) {
			if (recorder.error()) {
				// Recover from potential error.
				recorder.stop();
			}
			recorder.start();
			if (context.getConfiguration().getCameraRecordingConfig().isCameraEnabled()) {
				try {
					camRecorder.start();
				} catch (Exception e) {
					LOG.error("Start of CameraRecorderService failed", e);
					recorder.stop();
					throw e;
				}
			}
			recordingTimer.runTask();
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (recorder.started()) {
			recordingTimer.stop();
			recorder.suspend();
			if (context.getConfiguration().getCameraRecordingConfig().isCameraEnabled()) {
				try {
					camRecorder.suspend();
				} catch (Exception e) {
					LOG.error("Suspension of CameraRecorderService failed", e);
					recorder.stop();
					throw e;
				}
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (recorder.started() || recorder.suspended()) {
			recordingTimer.stop();
			recorder.stop();
			if (context.getConfiguration().getCameraRecordingConfig().isCameraEnabled()) {
				try {
					context.setRecordingStarted(false);
					camRecorder.stop();
				} catch (Exception e) {
					LOG.error("Stop of CameraRecorderService failed", e);
					throw e;
				}
			}
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		if (!recorder.destroyed()) {
			recorder.destroy();
			if (context.getConfiguration().getCameraRecordingConfig().isCameraEnabled()) {
				try {
					camRecorder.destroy();
				} catch (Exception e) {
					LOG.error("Destruction of CameraRecorderService failed", e);
				}
			}
		}
	}

	private void fireTimeChanged() {
		Time time = new Time(recorder.getElapsedTime());

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
