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

package org.lecturestudio.editor.api.video;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.EventExecutor;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.NextPageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;

public class VideoEventExecutor extends EventExecutor {

	private final VideoRendererView renderView;

	private final ToolController toolController;

	private final EventBus eventBus;

	private final Stack<PlaybackAction> playbacks;

	private Document document;

	private List<RecordedPage> recordedPages;

	private RecordingRenderProgressEvent progressEvent;

	private BiConsumer<BufferedImage, RecordingRenderProgressEvent> frameConsumer;

	private int pageNumber;

	private int duration;

	private long time;

	private float frameRate;

	private long frames;


	public VideoEventExecutor(VideoRendererView renderView, ToolController toolController, EventBus eventBus) {
		this.renderView = renderView;
		this.toolController = toolController;
		this.eventBus = eventBus;
		this.playbacks = new Stack<>();
	}

	public void setRecordedPages(List<RecordedPage> pageList) {
		this.recordedPages = pageList;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setFrameRate(int rate) {
		this.frameRate = rate;
	}

	public void setFrameConsumer(BiConsumer<BufferedImage, RecordingRenderProgressEvent> frameConsumer) {
		this.frameConsumer = frameConsumer;
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		renderView.setPage(event.getPage());
	}

	@Override
	public int getPageNumber(int timeMillis) {
		return 0;
	}

	@Override
	public int seekByTime(int timeMillis) {
		return 0;
	}

	@Override
	public Integer seekByPage(int pageNumber) {
		return null;
	}

	@Override
	public long getElapsedTime() {
		return time;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		time = 0;
		frames = 0;
		pageNumber = 0;

		renderView.setPage(document.getPage(pageNumber));

		getPlaybackActions(pageNumber);

		toolController.init();

		progressEvent = new RecordingRenderProgressEvent();
		progressEvent.setCurrentTime(new Time(0));
		progressEvent.setTotalTime(new Time(duration));
		progressEvent.setPageCount(document.getPageCount());
	}

	@Override
	protected void startInternal() throws ExecutableException {
		toolController.start();

		ExecutableState state = getPreviousState();
		
		if (state == ExecutableState.Initialized || state == ExecutableState.Stopped) {
			eventBus.register(this);

			try {
				executeEvents();
			}
			catch (Exception e) {
				eventBus.unregister(this);

				throw new ExecutableException(e);
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		eventBus.unregister(this);

		toolController.stop();
		renderView.dispose();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		toolController.destroy();
	}

	@Override
	protected void executeEvents() throws Exception {
		int timeStep = (int) (1000 / frameRate);

		while (getElapsedTime() < duration) {
			ExecutableState state = getState();

			if (state == ExecutableState.Starting || state == ExecutableState.Started) {
				long time = getElapsedTime();

				synchronized (playbacks) {
					// Execute all events for the current time period.
					while (true) {
						if (!playbacks.isEmpty()) {
							// Get next action for execution.
							PlaybackAction action = playbacks.peek();

							if (time < action.getTimestamp()) {
								break;
							}

							action.execute(toolController);

							// Remove executed action.
							playbacks.pop();
						}
						else if (pageNumber < recordedPages.size() - 1) {
							// Get actions for the next page.
							getPlaybackActions(++pageNumber);
						}
						else {
							break;
						}
					}

					renderFrame(this.time);

					this.time += timeStep;
				}

				// Relieve the CPU.
				Thread.sleep(1);
			}
			else {
				break;
			}
		}
	}

	private void renderFrame(long timestamp) {
		if (timestamp == 0) {
			timestamp = 1;
		}

		float currentFps = frames / (timestamp / 1000f);

		if (currentFps > frameRate) {
			// Drop frame.
			return;
		}

		if (nonNull(frameConsumer)) {
			progressEvent.getCurrentTime().setMillis(timestamp);
			progressEvent.setPageNumber(document.getCurrentPageNumber() + 1);

			frameConsumer.accept(renderView.renderCurrentFrame(), progressEvent);
		}

		frames++;
	}

	private void getPlaybackActions(int pageNumber) {
		RecordedPage recPage = recordedPages.get(pageNumber);

		playbacks.clear();

		// Add page change event.
		if (pageNumber != 0) {
			PlaybackAction action = new NextPageAction();
			action.setTimestamp(recPage.getTimestamp());
			playbacks.push(action);
		}

		playbacks.addAll(recPage.getPlaybackActions());

		if (playbacks.size() > 0) {
			Collections.reverse(playbacks);
		}

		this.pageNumber = pageNumber;
	}
}
