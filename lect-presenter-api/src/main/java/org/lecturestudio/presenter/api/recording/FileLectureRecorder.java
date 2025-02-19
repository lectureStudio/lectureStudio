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

package org.lecturestudio.presenter.api.recording;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioDeviceChangeListener;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioFrame;
import org.lecturestudio.core.audio.AudioMixer;
import org.lecturestudio.core.audio.AudioRecorder;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.audio.sink.ProxyAudioSink;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.*;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;

public class FileLectureRecorder extends LectureRecorder {

	private final RecordingBackup backup;

	private final ApplicationContext context;

	private final AudioSystemProvider audioSystemProvider;

	private final AudioConfiguration audioConfig;

	private final AudioDeviceChangeListener deviceChangeListener;

	private final DocumentService documentService;

	private IdleTimer idleTimer;

	private AudioRecorder audioRecorder;

	private AudioMixer audioMixer;

	private AudioFormat audioFormat;

	private SlideRecorder slideRecorder;

	private int bytesConsumed = 0;

	private int pageRecordingTimeout = 2000;


	public FileLectureRecorder(ApplicationContext context, AudioSystemProvider audioSystemProvider,
							   DocumentService documentService, AudioConfiguration audioConfig,
							   String recDir) throws IOException {
		this.context = context;
		this.audioSystemProvider = audioSystemProvider;
		this.documentService = documentService;
		this.audioConfig = audioConfig;
		this.backup = new RecordingBackup(recDir);
		this.deviceChangeListener = new AudioDeviceChangeListener() {

			@Override
			public void deviceConnected(AudioDevice device) {
				// Not needed here.
			}

			@Override
			public void deviceDisconnected(AudioDevice device) {
				handleDisconnectedDevice(device);
			}
		};
	}

	public void setPageRecordingTimeout(int timeoutMs) {
		pageRecordingTimeout = timeoutMs;
	}

	@Subscribe
	public void onEvent(final RecordActionEvent event) {
		if (initialized() || suspended() || stopped()) {
			slideRecorder.addPendingAction(event.getAction(), getElapsedTime());
		}
		if (!started()) {
			return;
		}

		// Force pending page change.
		handlePageChange();

		PlaybackAction action = event.getAction();

		if (action != null) {
			addPlaybackAction(action);
		}
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		if (initialized() || suspended() || stopped()) {
			slideRecorder.setPendingPage(event.getPage());
		}
		if (!started()) {
			return;
		}

		if (event.getType() == PageEvent.Type.CREATED) {
			addPage(event.getPage(), 0);
		}
		else if (event.getType() == PageEvent.Type.SELECTED) {
			runIdleTimer();
		}
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		Page currentPage = event.getDocument().getCurrentPage();

		if (initialized() || suspended() || stopped()) {
			slideRecorder.setPendingPage(currentPage);
		}
		if (!started()) {
			return;
		}
		if (event.selected() || event.replaced()) {
			addPage(currentPage, 0);
		}
	}

	@Override
	public synchronized long getElapsedTime() {
		if (audioFormat == null) {
			return 0;
		}

		float bytesPerSecond = AudioUtils.getBytesPerSecond(audioFormat);
		return (int) ((bytesConsumed / bytesPerSecond) * 1000);
	}

	public String getBestRecordingName() {
		return slideRecorder.getBestRecordingName();
	}

	public void writeRecording(File destFile, ProgressCallback progressCallback) throws IOException, NoSuchAlgorithmException {
		if (destFile == null) {
			throw new NullPointerException("No destination file provided.");
		}

		File audioFile = new File(backup.getAudioFile());

		float bps = AudioUtils.getBytesPerSecond(audioFormat);
		long duration = (long) ((audioFile.length() - 44) / bps * 1000);

		Document recordedDocument = slideRecorder.getRecordedDocument();
		recordedDocument.setTitle(FileUtils.stripExtension(destFile.getName()));

		try (RandomAccessAudioStream audioStream = new RandomAccessAudioStream(audioFile)) {
			audioStream.reset();

			RecordingHeader fileHeader = new RecordingHeader();
			fileHeader.setDuration(duration);

			Recording recording = new Recording();
			recording.setRecordingHeader(fileHeader);
			recording.setRecordedAudio(new RecordedAudio(audioStream));
			recording.setRecordedEvents(new RecordedEvents(slideRecorder.getRecordedPages()));
			recording.setRecordedDocument(new RecordedDocument(recordedDocument));

			RecordingFileWriter.write(recording, destFile, progressCallback);

			// Delete backup files since they are not needed anymore.
			discard();
		}
	}

	public void discard() {
		backup.clean();
	}

	public void setAudioFormat(AudioFormat format) {
		audioFormat = format;
	}

	public void setAudioVolume(double volume) {
		if (nonNull(audioRecorder)) {
			audioRecorder.setAudioVolume(volume);
		}
	}

	public void addPeerAudio(final AudioFrame frame) {
		if (!started()) {
			return;
		}

		audioMixer.addAudioFrame(frame);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		initSlideRecorder();

		audioConfig.mixAudioStreamsProperty()
				.addListener((o, oldValue, newValue) -> {
					if (nonNull(audioMixer)) {
						audioMixer.setMixAudio(newValue);
					}
				});

		audioSystemProvider.addDeviceChangeListener(deviceChangeListener);

		ApplicationBus.register(this);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		AudioBus.register(this);

		ExecutableState prevState = getPreviousState();

		if (prevState == ExecutableState.Initialized
				|| prevState == ExecutableState.Stopped) {
			String deviceName = audioConfig.getCaptureDeviceName();

			if (!hasRecordingDevice(deviceName)) {
				throw new AudioDeviceNotConnectedException(
						"Audio device %s is not connected", deviceName,
						deviceName);
			}

			backup.open();

			initAudioMixer();
			initAudioRecorder();

			startSlideRecorder();

			// Record the first page.
			Page firstPage = documentService.getDocuments().getSelectedDocument().getCurrentPage();
			recordPage(firstPage);
		}
		else if (prevState == ExecutableState.Suspended) {
			resumeSlideRecorder();
			resumeAudioRecorder();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		AudioBus.unregister(this);

		if (nonNull(audioRecorder)) {
			audioRecorder.stop();
		}

		try {
			audioMixer.stop();
		}
		catch (Exception e) {
			logException(e, "Close audio mixer failed");
		}

		stopSlideRecorder();

		backup.close();

		bytesConsumed = 0;
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		if (getPreviousState() == ExecutableState.Started) {
			if (nonNull(audioRecorder)) {
				audioRecorder.suspend();
			}

			slideRecorder.setPendingPage(getLastRecordedPage());
		}
	}

	@Override
	protected void destroyInternal() {
		audioSystemProvider.removeDeviceChangeListener(deviceChangeListener);

		try {
			ApplicationBus.unregister(this);
		}
		catch (Exception ignored) {
			// Throws an Error in case this class gets destroyed before being
			// initialized. Catches the error, because this is a legal state
			// transition.
		}

		try {
			if (audioMixer != null) {
				audioMixer.destroy();
			}
		}
		catch (Exception e) {
			logException(e, "Destroy audio mixer failed");
		}
	}

	@Override
	protected void fireStateChanged() {
		ApplicationBus.post(new RecordingStateEvent(getState()));
	}

	private void initAudioMixer() throws ExecutableException {
		audioMixer = new AudioMixer();
		audioMixer.setAudioFormat(audioFormat);
		audioMixer.setOutputFile(new File(backup.getAudioFile()));
		audioMixer.setMixAudio(audioConfig.getMixAudioStreams());
		audioMixer.init();
		audioMixer.start();
	}

	private void initAudioRecorder() throws ExecutableException {
		String deviceName = audioConfig.getCaptureDeviceName();
		Double deviceVolume = audioConfig.getRecordingVolume(deviceName);
		double masterVolume = audioConfig.getMasterRecordingVolume();
		double volume = nonNull(deviceVolume) ? deviceVolume : masterVolume;

		if (nonNull(audioRecorder)) {
			audioRecorder.destroy();
		}

		audioRecorder = audioSystemProvider.createAudioRecorder();
		audioRecorder.setAudioProcessingSettings(
				audioConfig.getRecordingProcessingSettings());
		audioRecorder.setAudioDeviceName(deviceName);
		audioRecorder.setAudioVolume(volume);
		audioRecorder.setAudioSink(new ProxyAudioSink(audioMixer) {

			@Override
			public int write(byte[] data, int offset, int length)
					throws IOException {
				bytesConsumed += length;

				return super.write(data, offset, length);
			}
		});
		audioRecorder.start();
	}

	private void resumeAudioRecorder() throws ExecutableException {
		if (nonNull(audioRecorder)) {
			audioRecorder.start();
		}
		else {
			initAudioRecorder();
		}
	}

	private void initSlideRecorder() throws ExecutableException {
		slideRecorder = new SlideRecorder();
		slideRecorder.init();
	}

	public void startSlideRecorder() throws ExecutableException {
		slideRecorder.start();
	}

	private void stopSlideRecorder() throws ExecutableException {
		slideRecorder.stop();
	}

	private void resumeSlideRecorder() throws ExecutableException {
		try {
			slideRecorder.updatePendingPage(getElapsedTime());
		}
		catch (IOException e) {
			throw new ExecutableException(e);
		}
	}

	private void handleDisconnectedDevice(AudioDevice device) {
		String deviceConfigName = audioConfig.getCaptureDeviceName();
		String deviceName = device.getName();

		if (Objects.equals(deviceName, deviceConfigName)) {
			// The recording device has been disconnected.
			// Any operation on the audio recorder is not possible anymore.
			audioRecorder = null;
		}
	}

	private void addPage(Page page, long openTime) {
		if (!started()) {
			return;
		}

		try {
			slideRecorder.insertPage(page, getElapsedTime(), openTime);
		}
		catch (IOException e) {
			logException(e, "Record slide failed");

			context.showError("recording.notification.title", "recording.slide.error");
		}
	}

	private synchronized void addPlaybackAction(PlaybackAction action) {
		if (!started()) {
			return;
		}
		if (isNull(action)) {
			return;
		}

		action.setTimestamp((int) getElapsedTime());

		// Add action to the current page.
		slideRecorder.getRecentRecordedPage().addPlaybackAction(action);
	}

	private void recordPage(Page page) {
		try {
			slideRecorder.recordPage(page, 0);
		}
		catch (IOException e) {
			logException(e, "Record slide failed");

			context.showError("recording.notification.title", "recording.slide.error");
			return;
		}

		// Write backup.
		try {
			backup.writeDocument(slideRecorder.getRecordedDocument());
			backup.writePages(slideRecorder.getRecordedPages());
		}
		catch (Throwable e) {
			logException(e, "Write backup failed");
		}
	}

	private Page getLastRecordedPage() {
		Set<Page> pageSet = slideRecorder.getRecordedPageMap().keySet();
		return pageSet.stream().skip(pageSet.size() - 1).findFirst().orElse(null);
	}

	private void recordCurrentPage(long taskTime) {
		if (!started()) {
			return;
		}

		addPage(documentService.getDocuments().getSelectedDocument().getCurrentPage(), taskTime);
	}

	private void handlePageChange() {
		if (nonNull(idleTimer) && idleTimer.hasRunningTask()) {
			idleTimer.stop();

			recordCurrentPage(idleTimer.getTaskTime());
		}
	}

	private boolean hasRecordingDevice(String deviceName) {
		if (isNull(deviceName)) {
			return false;
		}

		return Arrays.stream(audioSystemProvider.getRecordingDevices())
				.anyMatch(device -> device.getName().equals(deviceName));
	}

	private void runIdleTimer() {
		// Ignore all previous tasks.
		if (nonNull(idleTimer)) {
			idleTimer.stop();
		}

		idleTimer = new IdleTimer(pageRecordingTimeout);
		idleTimer.runIdleTask();
	}



	private class IdleTimer extends Timer {

		private final int idleTime;

		private TimerTask idleTask;

		private long taskStarted = 0;


		IdleTimer(int idleTime) {
			this.idleTime = idleTime;
		}

		void runIdleTask() {
			idleTask = new TimerTask() {

				@Override
				public void run() {
					recordCurrentPage(getTaskTime());
				}
			};

			taskStarted = System.currentTimeMillis();

			schedule(idleTask, idleTime);
		}

		public void stop() {
			cancel();
			purge();

			idleTask = null;
		}

		boolean hasRunningTask() {
			if (idleTask == null) {
				return false;
			}

			return (System.currentTimeMillis() - idleTask.scheduledExecutionTime()) < 0;
		}

		long getTaskTime() {
			return System.currentTimeMillis() - taskStarted;
		}
	}

}
