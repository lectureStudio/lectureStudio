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

package org.lecturestudio.media.playback;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.recording.EventExecutor;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.media.video.FFmpegFrameGrabber;
import org.lecturestudio.media.video.VideoPlayer;

/**
 * Executes recorded events from files during media playback.
 * This class manages the playback of recorded actions, handling the timing and
 * synchronization of events with audio and video content. It processes page changes,
 * annotations, and screen actions (video) in a synchronized manner.
 * The executor maintains page timing information to enable seeking by time or page number,
 * and coordinates with a VideoPlayer to handle video content during playback.
 *
 * @author Alex Andres
 */
public class FileEventExecutor extends EventExecutor {

	private static final Logger LOG = LogManager.getLogger(FileEventExecutor.class);

	/** The synchronization state that tracks timing for audio, video and events. */
	private final SyncState syncState;

	/** Controller for handling tool interactions and document manipulations. */
	private final ToolController toolController;

	/** List of pages with their associated playback actions and timestamps. */
	private final List<RecordedPage> recordedPages;

	/** Video player component responsible for handling video playback. */
	private final VideoPlayer videoPlayer;

	/** Stack of playback actions to be executed during playback. */
	private Stack<PlaybackAction> playbacks;

	/** Maps page numbers to their corresponding timestamp values in milliseconds. */
	private Map<Integer, Integer> pageChangeEvents;

	/** Thread that handles the sequential execution of playback events. */
	private EventThread thread;

	/** The currently active screen action that contains video playback information. */
	private ScreenAction activeScreenAction;


	/**
	 * Constructs a new FileEventExecutor for processing recorded events during playback.
	 *
	 * @param toolController The controller that handles tool interactions and document manipulations.
	 * @param recordedPages  List of pages with their associated actions and timestamps.
	 * @param videoPlayer    The video player component for handling video content.
	 * @param syncState      The state that tracks timing for audio, video and events.
	 */
	public FileEventExecutor(ToolController toolController, List<RecordedPage> recordedPages, VideoPlayer videoPlayer,
							 SyncState syncState) {
		this.toolController = toolController;
		this.recordedPages = recordedPages;
		this.videoPlayer = videoPlayer;
		this.syncState = syncState;
	}

	@Override
	public long getElapsedTime() {
		return syncState.getAudioTime();
	}

	@Override
	public int getPageNumber(int seekTime) {
		return getTimeTablePage(seekTime);
	}

	@Override
	public synchronized int seekByTime(int seekTime) throws ExecutableException {
		int pageNumber = getTimeTablePage(seekTime);

		seek(pageNumber, seekTime);

		return pageNumber;
	}

	@Override
	public synchronized Integer seekByPage(int pageNumber) throws ExecutableException {
		Integer timestamp = pageChangeEvents.get(pageNumber);

		seek(pageNumber, timestamp);

		return timestamp;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		playbacks = new Stack<>();
		pageChangeEvents = new LinkedHashMap<>();

		boolean videoLibLoaded = false;

		for (RecordedPage recPage : recordedPages) {
			pageChangeEvents.put(recPage.getNumber(), recPage.getTimestamp());

			for (PlaybackAction action : recPage.getPlaybackActions()) {
				if (action.getType() == ActionType.SCREEN) {
					fixScreenAction((ScreenAction) action, recPage.getNumber());

					if (!videoLibLoaded) {
						// Load native libraries in advance to improve user experience when the first video is going to
						// be played and therefore avoid ui lagging.
						videoLibLoaded = true;

                        try {
                            FFmpegFrameGrabber.tryLoad();
                        }
						catch (FFmpegFrameGrabber.Exception e) {
                            throw new ExecutableException(e);
                        }
                    }
				}
			}
		}

		getPlaybackActions(0);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		ExecutableState state = getPreviousState();

		if (state == ExecutableState.Initialized || state == ExecutableState.Stopped) {
			thread = new EventThread(() -> {
				try {
					executeEvents();
				}
				catch (InterruptedException e) {
					// Preserve interruption state.
					Thread.currentThread().interrupt();
				}
				catch (ExecutableException e) {
					LOG.error("Stop event executor failed.", e);
				}
				catch (Exception e) {
					LOG.error("Execute action failed.", e);
				}
			});
			thread.setName("EventExecutor-Thread");
			thread.start();

			startVideoPlayer();
		}
		else if (state == ExecutableState.Suspended) {
			// Interrupt the Thread in case it was sleeping to play new annotations again.
			if (thread.getState() == Thread.State.TIMED_WAITING) {
				thread.interrupt();
			}
			thread.signal();

			startVideoPlayer();
		}
	}

	@Override
	protected void suspendInternal() throws ExecutableException {
		suspendVideoPlayer();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		thread.shutdown();

		disposeVideoPlayer();

		getPlaybackActions(0);
	}

	@Override
	protected void destroyInternal() {
		playbacks.clear();
		pageChangeEvents.clear();
	}

	@Override
	protected void executeEvents() throws Exception {
		while (nonNull(thread)) {
			if (!thread.isRunning()) {
				return;
			}

			ExecutableState state = getState();

			if (state == ExecutableState.Starting || state == ExecutableState.Started) {
				long time = getElapsedTime();
				long sleep = 1;

				synchronized (playbacks) {
					if (!playbacks.isEmpty()) {
						// Get the next action for execution.
						PlaybackAction action = playbacks.peek();

						if (time >= action.getTimestamp()) {
							action.execute(toolController);

							// Remove the executed action.
							playbacks.pop();

							if (action.getType() == ActionType.SCREEN) {
								initVideoPlayer((ScreenAction) action);
								startVideoPlayer();
							}

							if (!playbacks.empty()) {
								// Time to wait until the next action.
								PlaybackAction nextAction = playbacks.peek();
								sleep = (nextAction.getTimestamp() - action.getTimestamp()) / 2;
							}

							syncState.setEventNumber(syncState.getEventNumber() + 1);
						}
						else {
							// Relieve the CPU.
							sleep = action.getTimestamp() - time;
						}
					}
					else if (syncState.getPageNumber() < recordedPages.size() - 1) {
						// Get actions for the next page.
						getPlaybackActions(syncState.getPageNumber() + 1);
					}
				}

				// Fail-safe.
				if (sleep < 1) {
					sleep = 1;
				}

				// Relieve the CPU.
				try {
					thread.sleep(sleep);
				}
				catch (InterruptedException e) {
					// Ignore
				}
			}
			else if (state == ExecutableState.Suspended) {
				thread.await();
			}
		}

		if (!stopped()) {
			stop();
		}
	}

	private void seek(int pageNumber, int timeMillis) throws ExecutableException {
		RecordedPage recPage = recordedPages.get(pageNumber);

		if (recPage.getNumber() == pageNumber) {
			getPlaybackActions(pageNumber);

			// Find actions for execution on the given page.
			while (!playbacks.isEmpty()) {
				PlaybackAction action = playbacks.peek();

				if (timeMillis >= action.getTimestamp()) {
					try {
						action.execute(toolController);
					}
					catch (Exception e) {
						LOG.error("Execute action failed.", e);
					}

					playbacks.pop();

					if (action.getType() == ActionType.SCREEN) {
						initVideoPlayer((ScreenAction) action);

						if (videoPlayer.initialized() || videoPlayer.started() || videoPlayer.suspended()) {
							// Get a video frame.
							try {
								videoPlayer.seekToVideoKeyFrame(timeMillis);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
					else {
						// Clear frames if this is not a video section at the current timestamp.
						if (isVideoSection(timeMillis)) {
							videoPlayer.clearFrames();
							activeScreenAction = null;
						}
					}

					syncState.setEventNumber(syncState.getEventNumber() + 1);
				}
				else {
					// Nothing more to execute.
					break;
				}
			}
		}
	}

	private synchronized void getPlaybackActions(int pageNumber) {
		RecordedPage recPage = recordedPages.get(pageNumber);

		// Add page change event.
		PlaybackAction action = new PageAction(0, pageNumber);
		action.setTimestamp(recPage.getTimestamp());

		playbacks.clear();
		playbacks.push(action);
		playbacks.addAll(recPage.getPlaybackActions());

		if (!playbacks.empty()) {
			Collections.reverse(playbacks);
		}

		syncState.setPageNumber(pageNumber);
		syncState.setEventNumber(0);
	}

	private int getTimeTablePage(int seekTime) {
		int page = 0;

		for (Integer pageNumber : pageChangeEvents.keySet()) {
			int timestamp = pageChangeEvents.get(pageNumber);

			if (seekTime == timestamp) {
				page = pageNumber;
				break;
			}
			else if (seekTime < timestamp) {
				break;
			}
			page = pageNumber;
		}

		return page;
	}

	private boolean isVideoSection(long timeMs) {
		return nonNull(activeScreenAction)
				&& (activeScreenAction.getTimestamp() > timeMs
					|| activeScreenAction.getTimestamp() + activeScreenAction.getVideoLength() < timeMs);
	}

	private void fixScreenAction(ScreenAction action, int pageNumber) {
        if (pageNumber >= recordedPages.size() - 1) {
            return;
        }

		// Fix overlapping screen actions into the next page.
		RecordedPage recPage = recordedPages.get(pageNumber + 1);
		if (nonNull(recPage) && (action.getTimestamp() + action.getVideoLength()) > recPage.getTimestamp()) {
			action.setVideoLength(recPage.getTimestamp() - action.getTimestamp());
		}
	}

	private void startVideoPlayer() throws ExecutableException {
		if (isNull(activeScreenAction)) {
			return;
		}

		if (isVideoSection(getElapsedTime())) {
			// Skip if this is not a video section at the current timestamp.
			return;
		}
		if (videoPlayer.stopped() || videoPlayer.suspended() || videoPlayer.initialized()) {
			videoPlayer.start();
		}
	}

	private void suspendVideoPlayer() throws ExecutableException {
		if (videoPlayer.started()) {
			videoPlayer.suspend();
		}
	}

	private void disposeVideoPlayer() throws ExecutableException {
		if (videoPlayer.started() || videoPlayer.suspended()) {
			videoPlayer.stop();
		}
		if (videoPlayer.initialized() || videoPlayer.stopped()) {
			videoPlayer.destroy();
		}
	}

	private void initVideoPlayer(ScreenAction action) throws ExecutableException {
		activeScreenAction = action;

		ApplicationContext context = toolController.getApplicationContext();
		File videoFile = videoPlayer.getVideoFile();

		if (nonNull(videoFile)
				&& videoFile.getName().equals(action.getFileName())
				&& action.getTimestamp() == videoPlayer.getReferenceTimestamp()) {
			// Already initialized.
			return;
		}

		disposeVideoPlayer();

		videoPlayer.setVideoFile(action.getFileName());
		videoPlayer.setVideoOffset(action.getVideoOffset());
		videoPlayer.setVideoLength(action.getVideoLength());
		videoPlayer.setVideoContentSize(action.getVideoDimension());
		videoPlayer.setReferenceTimestamp(action.getTimestamp());
		videoPlayer.setSyncState(syncState);

		if (!videoPlayer.getVideoFile().exists()) {
			context.showError("executor.video.error", "executor.video.error.not.found",
					videoPlayer.getVideoFile());
			// Do not initialize the video player.
			return;
		}

		try {
			videoPlayer.init();
		}
		catch (ExecutableException e) {
			LOG.error("Failed to initialize video playback", e);

			context.showError("executor.video.error", "executor.video.error.init");
		}
	}



	/**
	 * Thread implementation that handles the execution of playback events.
	 * This class provides mechanisms for controlling thread execution with
	 * wait/signal patterns and a clean shutdown procedure.
	 * It encapsulates a ReentrantLock and Condition to facilitate thread
	 * coordination during playback state changes (start, suspend, stop).
	 */
	private static class EventThread extends Thread {

		private final ReentrantLock lock;

		private final Condition condition;

		private volatile boolean running;


		EventThread(Runnable runnable) {
			super(runnable);

			lock = new ReentrantLock();
			condition = lock.newCondition();
			running = true;
		}

		boolean isRunning() {
			return running;
		}

		void shutdown() {
			running = false;

			lock.lock();

			try {
				if (lock.hasWaiters(condition)) {
					signal();
				}
			}
			finally {
				lock.unlock();
			}
		}

		void await() {
			lock.lock();

			try {
				condition.await();
			}
			catch (InterruptedException e) {
				// Ignore
			}
			finally {
				lock.unlock();
			}
		}

		void signal() {
			lock.lock();

			try {
				condition.signalAll();
			}
			finally {
				lock.unlock();
			}
		}
	}
}
