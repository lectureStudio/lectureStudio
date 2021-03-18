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

package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.video.VideoRenderProgressEvent;
import org.lecturestudio.editor.api.video.VideoRenderStateEvent;
import org.lecturestudio.editor.api.video.VideoRenderer;
import org.lecturestudio.editor.api.view.VideoExportProgressView;
import org.lecturestudio.editor.api.web.WebVectorExport;
import org.lecturestudio.editor.api.web.WebVideoExport;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.swing.DefaultRenderContext;

public class VideoExportProgressPresenter extends Presenter<VideoExportProgressView> {

	private final RecordingFileService recordingService;

	private VideoRenderer videoRenderer;


	@Inject
	VideoExportProgressPresenter(ApplicationContext context, VideoExportProgressView view,
			RecordingFileService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		ApplicationBus.register(this);

		view.setOnCancel(this::cancel);
		view.setOnClose(this::close);

		CompletableFuture.runAsync(() -> {
			Recording recording = recordingService.getSelectedRecording();
			RenderConfiguration config = ((EditorContext) context).getRenderConfiguration();

			videoRenderer = new VideoRenderer(context, recording, config);
			videoRenderer.addStateListener((oldState, newState) -> {
				if (newState == ExecutableState.Started) {
					setCloseable(false);
				}
				else if (newState == ExecutableState.Stopped) {
					setCloseable(true);
				}
			});

			try {
				videoRenderer.start();
			}
			catch (Exception e) {
				handleException(e, "Video rendering failed", "recording.render.error");
			}

			if (config.getWebVectorExport()) {
				createWebVectorExport(recording);
			}
			if (config.getWebVideoExport()) {
				createWebVideoExport(recording);
			}
		});
	}

	@Override
	public void destroy() {
		cancel();

		ApplicationBus.unregister(this);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	@Subscribe
	public void onEvent(final VideoRenderProgressEvent event) {
		final Time current = event.getCurrentTime();
		final Time total = event.getTotalTime();

		view.setTimeProgress(current, total);
		view.setPageProgress(event.getPageNumber(), event.getPageCount());
		view.setProgress(current.getMillis() / (double) total.getMillis());
	}

	@Subscribe
	public void onEvent(final VideoRenderStateEvent event) {
		Dictionary dict = context.getDictionary();
		String message = null;

		if (event.getState() == VideoRenderStateEvent.State.RENDER_AUDIO) {
			message = dict.get("recording.render.audio");

			view.setTimeProgress(event.getCurrentTime(), event.getTotalTime());
		}
		else if (event.getState() == VideoRenderStateEvent.State.RENDER_VIDEO) {
			message = dict.get("recording.render.video");
		}
		else if (event.getState() == VideoRenderStateEvent.State.PASS_1) {
			message = MessageFormat.format(dict.get("recording.render.video.pass"), 1);
		}
		else if (event.getState() == VideoRenderStateEvent.State.PASS_2) {
			message = MessageFormat.format(dict.get("recording.render.video.pass"), 2);
		}
		else if (event.getState() == VideoRenderStateEvent.State.FINISHED) {
			message = dict.get("recording.render.finished");

			view.setFinished();
		}

		if (nonNull(message)) {
			view.setTitle(message);
		}
	}

	private void cancel() {
		if (nonNull(videoRenderer) && (videoRenderer.started() || videoRenderer.suspended())) {
			try {
				videoRenderer.stop();

				view.setTitle(context.getDictionary().get("recording.render.canceled"));
				view.setCanceled();
			}
			catch (ExecutableException e) {
				handleException(e, "Stop video rendering failed", "recording.render.error");
			}
		}
	}

	private void createWebVectorExport(Recording recording) {
		RenderConfiguration config = ((EditorContext) context).getRenderConfiguration();

		File outputFile = config.getOutputFile();
		String webExportPath = FileUtils.stripExtension(outputFile.getPath());
		File outputFolder = Paths.get(webExportPath).getParent().resolve("vector").toFile();

		WebVectorExport vectorExport = new WebVectorExport();
		vectorExport.setTitle("Web Player");
		vectorExport.setName(FileUtils.stripExtension(outputFile.getName()));

		try {
			vectorExport.export(recording, outputFolder);
		}
		catch (Exception e) {
			handleException(e, "Video rendering failed", "recording.render.error");
		}
	}

	private void createWebVideoExport(Recording recording) {
		RenderConfiguration config = ((EditorContext) context).getRenderConfiguration();

		String webExportPath = FileUtils.stripExtension(config.getOutputFile().getPath());
		File webExportFile = new File(webExportPath + ".html");

		WebVideoExport webExport = new WebVideoExport();
		webExport.setTitle("Web Player");
		webExport.setVideoSource(config.getOutputFile().getName());

		try {
			EditorContext renderContext = new EditorContext(null, null,
					context.getConfiguration(), context.getDictionary(),
					new EventBus(), new EventBus());

			webExport.setRenderController(new RenderController(renderContext, new DefaultRenderContext()));
			webExport.export(recording, webExportFile);
		}
		catch (Exception e) {
			handleException(e, "Video rendering failed", "recording.render.error");
		}
	}
}
