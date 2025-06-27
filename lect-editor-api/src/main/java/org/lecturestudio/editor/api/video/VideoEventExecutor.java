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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bytedeco.javacv.Frame;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.EventExecutor;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.NextPageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;
import org.lecturestudio.media.video.FrameToBufferedImageConverter;
import org.lecturestudio.media.video.VideoPlayer;

public class VideoEventExecutor extends EventExecutor {

	private final VideoRendererView renderView;

	private final VideoPlayer videoPlayer;

	private final ToolController toolController;

	private final EventBus eventBus;

	private final Stack<PlaybackAction> playbacks;

	private FrameToBufferedImageConverter frameConverter;

	private Document document;

	private List<RecordedPage> recordedPages;

	private RecordingRenderProgressEvent progressEvent;

	private Consumer<Throwable> errorConsumer;

	private BiConsumer<BufferedImage, RecordingRenderProgressEvent> frameConsumer;

	private Frame frame;

	private int pageNumber;

	private int duration;

	private long time;

	private float frameRate;

	private long frames;


	public VideoEventExecutor(VideoRendererView renderView, VideoPlayer videoPlayer, ToolController toolController,
							  EventBus eventBus) {
		this.renderView = renderView;
		this.videoPlayer = videoPlayer;
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

	public void setErrorConsumer(Consumer<Throwable> consumer) {
		this.errorConsumer = consumer;
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

		frameConverter = new FrameToBufferedImageConverter();
		frameConverter.setImageSize(renderView.getImageSize());

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

			CompletableFuture.runAsync(() -> {
				try {
					executeEvents();
				}
				catch (Exception e) {
					eventBus.unregister(this);

					throw new CompletionException(e);
				}
			});
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

		frameConverter.dispose();
	}

	@Override
	protected void executeEvents() throws Exception {
		int timeStep = (int) (1000 / frameRate);

		while (getElapsedTime() < duration) {
			ExecutableState state = getState();

			if (state == ExecutableState.Starting || state == ExecutableState.Started) {
				long startTime = getElapsedTime();

				synchronized (playbacks) {
					// Execute all events for the current time period.
					while (true) {
						if (!playbacks.isEmpty()) {
							// Get the next action for execution.
							PlaybackAction action = playbacks.peek();

							if (startTime < action.getTimestamp()) {
								break;
							}

							action.execute(toolController);

							// Remove executed action.
							playbacks.pop();

							if (action.getType() == ActionType.SCREEN) {
								try {
									initVideoPlayer((ScreenAction) action);
								}
								catch (Exception e) {
									handleError(e, "Init video reader failed");
									return;
								}
							}
						}
						else if (pageNumber < recordedPages.size() - 1) {
							// Get actions for the next page.
							getPlaybackActions(++pageNumber);
						}
						else {
							break;
						}
					}

					try {
						renderFrame(this.time);
					}
					catch (Exception e) {
						handleError(e, "Rendering frame failed");
						return;
					}

					this.time += timeStep;
				}

				// Relieve the CPU.
				Thread.sleep(1);
			}
			else {
				break;
			}
		}

		if (!stopped()) {
			stop();
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

			if (videoPlayer.initialized()) {
				// If we are in a video section, read and render video frames from the corresponding video file.
				try {
					renderVideoFrame(timestamp);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				// Generate the slide frame from the current action and document state.
				frameConsumer.accept(renderView.renderCurrentFrame(), progressEvent);
			}
		}

		frames++;
	}

	private boolean consumeFrame(Frame frame, long timestamp) throws Exception {
		// Use a window of half the framerate to be more in sync.
		double frameTsMargin = Math.round(1000 / videoPlayer.getFrameRate() / 2);
		long frameTs = videoPlayer.calculateTimestamp(frame.timestamp);
		if (frameTs >= timestamp - frameTsMargin) {
			renderView.renderFrameImage(frameConverter.convert(frame));
			frameConsumer.accept(renderView.renderCurrentFrame(), progressEvent);
			return true;
		}
		return false;
	}

	private void renderVideoFrame(long timestamp) throws Exception {
		if (nonNull(frame)) {
			if (consumeFrame(frame, timestamp)) {
				// Frame has been repeated due to the low video frame rate.
				return;
			}
		}

		// Get the video frame with the desired timestamp, otherwise skip over.
		while ((frame = videoPlayer.readVideoFrame()) != null) {
			if (consumeFrame(frame, timestamp)) {
				// Found frame with a suitable timestamp.
				break;
			}
		}

		if (isNull(frame)) {
			// Reached the end of the video.
			renderView.renderPageImage();

			videoPlayer.clearFrames();
			videoPlayer.destroy();
		}
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

		if (!playbacks.isEmpty()) {
			Collections.reverse(playbacks);
		}

		this.pageNumber = pageNumber;
	}

	private void initVideoPlayer(ScreenAction action) throws ExecutableException {
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
		videoPlayer.setReferenceTimestamp(getElapsedTime());
		videoPlayer.init();
	}

	private void disposeVideoPlayer() throws ExecutableException {
		if (videoPlayer.started() || videoPlayer.suspended()) {
			videoPlayer.stop();
		}
		if (videoPlayer.initialized() || videoPlayer.stopped()) {
			videoPlayer.destroy();
		}
	}

	private void handleError(Throwable throwable, String throwMessage) {
		logException(throwable, throwMessage);

		if (nonNull(errorConsumer)) {
			errorConsumer.accept(throwable);
		}
	}
}
