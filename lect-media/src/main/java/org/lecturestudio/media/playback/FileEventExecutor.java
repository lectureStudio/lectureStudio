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

import static java.util.Objects.nonNull;

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
import org.lecturestudio.core.audio.SyncState;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.recording.EventExecutor;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;

public class FileEventExecutor extends EventExecutor {

	private static final Logger LOG = LogManager.getLogger(FileEventExecutor.class);

	private final ToolController toolController;

	private final List<RecordedPage> recordedPages;

	private Stack<PlaybackAction> playbacks;

	private Map<Integer, Integer> pageChangeEvents;

	private final SyncState syncState;

	private EventThread thread;


	public FileEventExecutor(ToolController toolController, List<RecordedPage> recordedPages, SyncState syncState) {
		this.toolController = toolController;
		this.recordedPages = recordedPages;
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
	public synchronized int seekByTime(int seekTime) {
		int pageNumber = getTimeTablePage(seekTime);

		seek(pageNumber, seekTime);

		return pageNumber;
	}

	@Override
	public synchronized Integer seekByPage(int pageNumber) {
		Integer timestamp = pageChangeEvents.get(pageNumber);

		seek(pageNumber, timestamp);

		return timestamp;
	}

	@Override
	protected void initInternal() {
		playbacks = new Stack<>();
		pageChangeEvents = new LinkedHashMap<>();

		for (RecordedPage recPage : recordedPages) {
			pageChangeEvents.put(recPage.getNumber(), recPage.getTimestamp());
		}

		getPlaybackActions(0);
	}

	@Override
	protected void startInternal() {
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
		}
		else if (state == ExecutableState.Suspended) {
			// Interrupt the Thread in case it was sleeping in order to play new annotations again
			if (thread.getState() == Thread.State.TIMED_WAITING) {
				thread.interrupt();
			}
			thread.signal();
		}
	}

	@Override
	protected void stopInternal() {
		thread.shutdown();

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
						// Get next action for execution.
						PlaybackAction action = playbacks.peek();

						if (time >= action.getTimestamp()) {
							action.execute(toolController);

							// Remove executed action.
							playbacks.pop();

							if (!playbacks.empty()) {
								// Time to wait until next action.
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

				// Fail safe.
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

	private void seek(int pageNumber, int timeMillis) {
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
