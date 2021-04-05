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

import java.text.MessageFormat;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderState;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;
import org.lecturestudio.editor.api.video.VideoRenderer;
import org.lecturestudio.editor.api.view.VideoExportProgressView;
import org.lecturestudio.editor.api.web.WebVectorExport;
import org.lecturestudio.editor.api.web.WebVideoExport;
import org.lecturestudio.media.config.RenderConfiguration;

public class VideoExportProgressPresenter extends Presenter<VideoExportProgressView> {

	private final RecordingFileService recordingService;

	private final Stack<RecordingExport> exportStack;

	private final AtomicBoolean canceled = new AtomicBoolean();

	private RecordingExport export;


	@Inject
	VideoExportProgressPresenter(ApplicationContext context, VideoExportProgressView view,
			RecordingFileService recordingService) {
		super(context, view);

		this.recordingService = recordingService;
		this.exportStack = new Stack<>();
	}

	@Override
	public void initialize() throws ExecutableException {
		view.setOnCancel(this::cancel);
		view.setOnClose(this::close);

		Recording recording = recordingService.getSelectedRecording();
		RenderConfiguration config = ((EditorContext) context).getRenderConfiguration();

		// Web video export is dependent on the compressed video.
		boolean renderVideo = config.getVideoExport() || config.getWebVideoExport();

		try {
			// Note the order last-in-first-out.
			if (config.getWebVectorExport()) {
				exportStack.push(createWebVectorExport(recording, config));
			}
			if (config.getWebVideoExport()) {
				exportStack.push(createWebVideoExport(recording, config));
			}
			if (renderVideo) {
				exportStack.push(new VideoRenderer(context, recording, config));
			}
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		CompletableFuture.runAsync(this::run)
				.thenRun(this::done)
				.exceptionally(e -> {
					handleException(e, "Video rendering failed", "recording.render.error");
					return null;
				});
	}

	@Override
	public void destroy() {
		cancel();
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	private void onRenderProgress(RecordingRenderProgressEvent event) {
		final Time current = event.getCurrentTime();
		final Time total = event.getTotalTime();

		view.setTimeProgress(current, total);
		view.setPageProgress(event.getPageNumber(), event.getPageCount());
		view.setProgress(current.getMillis() / (double) total.getMillis());
	}

	private void onRenderState(RecordingRenderState state) {
		Dictionary dict = context.getDictionary();
		String message = null;

		switch (state) {
			case PASS_1:
				message = MessageFormat.format(dict.get("recording.render.video.pass"), 1);
				break;
			case PASS_2:
				message = MessageFormat.format(dict.get("recording.render.video.pass"), 2);
				break;
			case RENDER_AUDIO:
				message = dict.get("recording.render.audio");
				break;
			case RENDER_VIDEO:
				message = dict.get("recording.render.video");
				break;
		}

		if (nonNull(message)) {
			view.setTitle(message);
		}
	}

	private void cancel() {
		canceled.set(true);

		exportStack.clear();

		if (nonNull(export) && export.started()) {
			try {
				export.stop();
			}
			catch (ExecutableException e) {
				handleException(e, "Stop video rendering failed", "recording.render.error");
			}
		}
	}

	private void run() {
		while (!exportStack.isEmpty()) {
			export = exportStack.pop();

			try {
				export.addRenderProgressListener(this::onRenderProgress);
				export.addRenderStateListener(this::onRenderState);
				export.start();

				// When done, clean-up resources.
				export.destroy();
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		}
	}

	private void done() {
		export = null;

		if (canceled.get()) {
			view.setTitle(context.getDictionary().get("recording.render.canceled"));
			view.setCanceled();
		}
		else {
			view.setTitle(context.getDictionary().get("recording.render.finished"));
			view.setFinished();
		}

		setCloseable(true);
	}

	private RecordingExport createWebVectorExport(Recording recording, RenderConfiguration config) {
		WebVectorExport vectorExport = new WebVectorExport(recording, config);
		vectorExport.setTitle("Web Player");

		return vectorExport;
	}

	private RecordingExport createWebVideoExport(Recording recording, RenderConfiguration config) {
		WebVideoExport webExport = new WebVideoExport(recording, config);
		webExport.setTitle("Web Player");

		return webExport;
	}
}
