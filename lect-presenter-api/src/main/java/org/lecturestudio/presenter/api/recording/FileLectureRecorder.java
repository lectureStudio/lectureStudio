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

import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.configuration.AudioConfiguration;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.bus.AudioBus;
import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.sink.WavFileSink;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.RecordingHeader;
import org.lecturestudio.core.recording.file.RecordingFileWriter;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.RecordedDocument;
import org.lecturestudio.core.recording.RecordedEvents;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.avdev.AVdevAudioInputDevice;
import org.lecturestudio.media.avdev.AvdevAudioRecorder;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;

public class FileLectureRecorder extends LectureRecorder {

	private static final Logger LOG = LogManager.getLogger(FileLectureRecorder.class);

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final Stack<RecordedPage> recordedPages = new Stack<>();

	private final Map<Page, RecordedPage> addedPages = new LinkedHashMap<>();

	private final RecordingBackup backup;

	private final AudioConfiguration audioConfig;

	private final DocumentService documentService;

	private final PendingActions pendingActions;

	private IdleTimer idleTimer;

	private AvdevAudioRecorder audioRecorder;

	private AudioSink audioSink;

	private AudioFormat audioFormat;

	private Document recordedDocument;

	private int bytesConsumed = 0;

	private int pageRecordingTimeout = 2000;


	public FileLectureRecorder(DocumentService documentService, AudioConfiguration audioConfig, String recDir) throws IOException {
		this.documentService = documentService;
		this.audioConfig = audioConfig;
		this.backup = new RecordingBackup(recDir);
		this.pendingActions = new PendingActions();
	}

	public void setPageRecordingTimeout(int timeoutMs) {
		pageRecordingTimeout = timeoutMs;
	}

	@Subscribe
	public void onEvent(final RecordActionEvent event) {
		if (initialized() || suspended()) {
			addPendingAction(event.getAction());
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
		if (initialized() || suspended()) {
			pendingActions.setPendingPage(event.getPage());
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

		if (initialized() || suspended()) {
			pendingActions.setPendingPage(currentPage);
		}
		if (!started()) {
			return;
		}
		if (event.selected()) {
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
		String name = null;

		for (Page page : addedPages.keySet()) {
			Document doc = page.getDocument();

			if (doc.isPDF() && nonNull(doc.getName())) {
				// Return the name of the first used PDF document.
				return doc.getName();
			}

			name = doc.getName();
		}

		return name;
	}

	public void writeRecording(File destFile, ProgressCallback progressCallback) throws Exception {
		if (destFile == null) {
			throw new NullPointerException("No destination file provided.");
		}

		File audioFile = new File(backup.getAudioFile());

		float bps = AudioUtils.getBytesPerSecond(audioFormat);
		long duration = (long) ((audioFile.length() - 44) / bps * 1000);

		recordedDocument.setTitle(FileUtils.stripExtension(destFile.getName()));

		RandomAccessAudioStream audioStream = new RandomAccessAudioStream(audioFile);
		audioStream.reset();

		RecordingHeader fileHeader = new RecordingHeader();
		fileHeader.setDuration(duration);

		Recording recording = new Recording();
		recording.setRecordingHeader(fileHeader);
		recording.setRecordedAudio(new RecordedAudio(audioStream));
		recording.setRecordedEvents(new RecordedEvents(recordedPages));
		recording.setRecordedDocument(new RecordedDocument(recordedDocument));

		RecordingFileWriter.write(recording, destFile, progressCallback);

		// Delete backup files since they are not needed anymore.
		discard();
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

	@Override
	protected void initInternal() {
		pendingActions.initialize();

		ApplicationBus.register(this);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		String deviceName = audioConfig.getCaptureDeviceName();

		AudioBus.register(this);

		ExecutableState prevState = getPreviousState();

		if (prevState == ExecutableState.Initialized || prevState == ExecutableState.Stopped) {
			if (!AudioUtils.hasCaptureDevice(audioConfig.getSoundSystem(), deviceName)) {
				throw new AudioDeviceNotConnectedException("Audio device %s is not connected", deviceName, deviceName);
			}

			clear();

			backup.open();

			// need to create a new sink
			try {
				audioSink = new WavFileSink(new File(backup.getAudioFile()));
				audioSink.setAudioFormat(audioFormat);
				audioSink.open();
			}
			catch (IOException e) {
				throw new ExecutableException("Could not create audio sink.", e);
			}

			try {
				recordedDocument = new Document();
			}
			catch (IOException e) {
				throw new ExecutableException("Could not create document.", e);
			}

			if (nonNull(audioRecorder)) {
				audioRecorder.stop();
			}

			AudioInputDevice inputDevice = AudioUtils.getAudioInputDevice(audioConfig.getSoundSystem(), deviceName);
			Double deviceVolume = audioConfig.getRecordingVolume(deviceName);
			double masterVolume = audioConfig.getMasterRecordingVolume();
			double volume = nonNull(deviceVolume) ? deviceVolume : masterVolume;

			audioRecorder = new AvdevAudioRecorder((AVdevAudioInputDevice) inputDevice);
			audioRecorder.setAudioFormat(audioFormat);
			audioRecorder.setAudioVolume(volume);
			audioRecorder.setSink((data, length) -> {
				audioSink.write(data, 0, length);
				bytesConsumed += length;
			});
			audioRecorder.start();

			// Record the first page.
			Page firstPage = documentService.getDocuments().getSelectedDocument().getCurrentPage();
			recordPage(firstPage, 0);
		}
		else if (prevState == ExecutableState.Suspended) {
			Page pendingPage = pendingActions.getPendingPage();

			if (nonNull(pendingPage)) {
				if (isDuplicate(pendingPage)) {
					insertPendingActions(recordedPages.peek(), pendingPage);
				}
				else {
					insertPage(pendingPage, 0);
				}
			}

			audioRecorder.start();
		}
	}

	@Override
	protected void stopInternal() {
		AudioBus.unregister(this);

		if (nonNull(audioRecorder)) {
			audioRecorder.stop();
		}
		if (nonNull(audioSink)) {
			try {
				audioSink.close();
			}
			catch (IOException e) {
				LOG.error("Close audio sink failed", e);
			}
		}

		backup.close();

		bytesConsumed = 0;
	}

	@Override
	protected void suspendInternal() {
		if (getPreviousState() == ExecutableState.Started) {
			audioRecorder.pause();

			pendingActions.setPendingPage(getLastRecordedPage());
		}
	}

	@Override
	protected void destroyInternal() {
		ApplicationBus.unregister(this);

		clear();
	}

	@Override
	protected void fireStateChanged() {
		ApplicationBus.post(new RecordingStateEvent(getState()));
	}

	private void addPendingAction(PlaybackAction action) {
		if (isNull(action)) {
			return;
		}

		action.setTimestamp((int) getElapsedTime());

		pendingActions.addPendingAction(action);
	}

	private void clear() {
		if (nonNull(recordedDocument)) {
			synchronized (recordedDocument) {
				recordedDocument.close();
				recordedDocument = null;
			}
		}

		addedPages.clear();
		recordedPages.clear();
	}

	private void addPage(Page page, long openTime) {
		if (!started()) {
			return;
		}

		insertPage(page, openTime);
	}

	private void insertPage(Page page, long openTime) {
		// Do not add equal pages consecutively.
		if (isDuplicate(page) || openTime < 0) {
			return;
		}

		long timestamp = getElapsedTime() - openTime;

		recordPage(page, timestamp);
	}

	private void insertPendingActions(RecordedPage recPage, Page page) {
		List<PlaybackAction> actions = pendingActions.getPendingActions(page);

		for (PlaybackAction action : actions) {
			recPage.addPlaybackAction(action.clone());
		}
	}

	private void insertPendingPageActions(RecordedPage recPage, Page page) {
		List<PlaybackAction> actions = pendingActions.getPendingActions(page);

		for (PlaybackAction action : actions) {
			StaticShapeAction staticAction = new StaticShapeAction(action.clone());
			recPage.addStaticAction(staticAction);
		}

		pendingActions.clearPendingActions(page);
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
		recordedPages.peek().addPlaybackAction(action);
	}

	private void recordPage(Page page, long timestamp) {
		CompletableFuture.runAsync(() -> {
			int pageNumber = recordedPages.size();

			RecordedPage recPage = new RecordedPage();
			recPage.setTimestamp((int) timestamp);
			recPage.setNumber(pageNumber);

			// Copy all actions, if the page was previously annotated and visited again.
			if (addedPages.containsKey(page)) {
				RecordedPage rPage = addedPages.get(page);

				if (rPage != null) {
					for (StaticShapeAction action : rPage.getStaticActions()) {
						recPage.addStaticAction(action.clone());
					}
					for (PlaybackAction action : rPage.getPlaybackActions()) {
						StaticShapeAction staticAction = new StaticShapeAction(action.clone());
						recPage.addStaticAction(staticAction);
					}
				}
			}
			if (pendingActions.hasPendingActions(page)) {
				insertPendingPageActions(recPage, page);
			}

			synchronized (recordedDocument) {
				try {
					recordedDocument.createPage(page);
				}
				catch (Exception e) {
					LOG.error("Create page failed", e);
					return;
				}

				// Update page to last recorded page relation.
				addedPages.remove(page);
				addedPages.put(page, recPage);

				recordedPages.push(recPage);

				// Write backup.
				backup.writeDocument(recordedDocument);
				backup.writePages(recordedPages);
			}
		}, executor).join();
	}

	private Page getLastRecordedPage() {
		Set<Page> pageSet = addedPages.keySet();
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

	private boolean isDuplicate(Page page) {
		return page.equals(getLastRecordedPage());
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
