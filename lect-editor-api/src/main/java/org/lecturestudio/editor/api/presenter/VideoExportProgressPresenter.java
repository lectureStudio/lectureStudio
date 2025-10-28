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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.editor.api.recording.RecordingExport;
import org.lecturestudio.editor.api.recording.RecordingRenderState;
import org.lecturestudio.editor.api.recording.RecordingRenderProgressEvent;
import org.lecturestudio.editor.api.view.VideoExportProgressView;

public class VideoExportProgressPresenter extends Presenter<VideoExportProgressView> {

	private final AtomicBoolean canceled = new AtomicBoolean();

	private Stack<RecordingExport> exportStack;

	private RecordingExport export;


	@Inject
	VideoExportProgressPresenter(ApplicationContext context,
			VideoExportProgressView view) {
		super(context, view);
	}

	@Override
	public void initialize() throws ExecutableException {
		view.setOnCancel(this::cancel);
		view.setOnClose(this::close);

		CompletableFuture.runAsync(this::run)
				.thenRun(this::done)
				.exceptionally(e -> {
					handleException(e, "Video rendering failed",
							"recording.render.error");
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

	public void setExportStack(Stack<RecordingExport> stack) {
		this.exportStack = stack;
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
		String message = switch (state) {
			case PASS_1 -> MessageFormat.format(dict.get("recording.render.video.pass"), 1);
			case PASS_2 -> MessageFormat.format(dict.get("recording.render.video.pass"), 2);
			case RENDER_AUDIO -> dict.get("recording.render.audio");
			case RENDER_VIDEO -> dict.get("recording.render.video");
			case RENDER_VECTOR_AUDIO -> dict.get("recording.render.vector.audio");
			case RENDER_VECTOR_VIDEO -> dict.get("recording.render.vector.video");
			case ERROR -> dict.get("recording.render.error");
		};

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
		setCloseable(false);

		while (!exportStack.isEmpty()) {
			// Sequential processing of the export stack.
			export = exportStack.pop();

			CountDownLatch latch = new CountDownLatch(1);

			try {
				export.addRenderProgressListener(this::onRenderProgress);
				export.addRenderStateListener(this::onRenderState);
				export.addStateListener((oldState, newState) -> {
					if (export.stopped()) {
						latch.countDown();
					}
				});
				export.start();

				// Wait until the export process has finished (stopped).
				latch.await();
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
}
