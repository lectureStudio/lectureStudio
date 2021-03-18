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

import java.util.concurrent.ConcurrentLinkedQueue;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.net.Synchronizer;
import org.lecturestudio.core.recording.EventExecutor;
import org.lecturestudio.core.recording.action.PlaybackAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamEventExecutor extends EventExecutor {

	private static final Logger LOG = LogManager.getLogger(StreamEventExecutor.class);
	
	private final ToolController toolController;
	
	private final ConcurrentLinkedQueue<PlaybackAction> eventQueue;

	private EventQueueExecuter queueRunner;


	public StreamEventExecutor(ToolController toolController) {
		this.toolController = toolController;
		this.eventQueue = new ConcurrentLinkedQueue<>();
	}
	
	public void addAction(PlaybackAction action) {
		if (!started()) {
			return;
		}
		
		eventQueue.offer(action);
	}
	
	@Override
	public long getElapsedTime() {
		return Synchronizer.getAudioTime();
	}

	@Override
	public int getPageNumber(int timeMillis) {
		return 0;
	}
	
	@Override
	public int seekByTime(int seekTime) {
		return 0;
	}
	
	@Override
	public Integer seekByPage(int pageNumber) {
		return null;
	}
	
	@Override
	protected void executeEvents() {
		if (!eventQueue.isEmpty()) {
			// Get next action for execution.
			final PlaybackAction action = eventQueue.peek();
			
			if (Synchronizer.getAudioTime() >= action.getTimestamp()) {
				try {
					action.execute(toolController);
				}
				catch (Exception e) {
					LOG.error("Execute action failed.", e);
				}

				// Remove executed event.
				eventQueue.poll();
			}
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		toolController.init();
		
		queueRunner = new EventQueueExecuter();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		toolController.start();
		queueRunner.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		toolController.stop();
		
		queueRunner.shutdown();
		
		eventQueue.clear();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		toolController.destroy();
	}

	
	/**
	 * Event queue runner. Dispatches events in synchronization with audio timestamps.
	 */
	private class EventQueueExecuter extends Thread {

		private volatile boolean run = true;
		

		@Override
		public void run() {
			while (run) {
				try {
					executeEvents();
				}
				catch (Exception e) {
					LOG.error("Execute action failed.", e);
				}
				
				try {
					Thread.sleep(1);
				}
				catch (InterruptedException e) {
				}
			}
		}

		public void shutdown() {
			run = false;
		}

	}

}
