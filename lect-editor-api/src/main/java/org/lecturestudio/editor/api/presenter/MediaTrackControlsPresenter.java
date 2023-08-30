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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.model.ZoomConstraints;
import org.lecturestudio.editor.api.presenter.command.AdjustAudioCommand;
import org.lecturestudio.editor.api.presenter.command.ReplacePageCommand;
import org.lecturestudio.editor.api.presenter.command.SplitAndSaveRecordingCommand;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.view.MediaTrackControlsView;
import org.lecturestudio.media.search.SearchService;
import org.lecturestudio.media.search.SearchState;

public class MediaTrackControlsPresenter extends Presenter<MediaTrackControlsView> {

	private final ViewContextFactory viewFactory;

	private final RecordingFileService recordingService;

	private final RecordingPlaybackService playbackService;

	private final SearchService searchService;

	private final ZoomConstraints zoomConstraints;

	private SearchState searchState;


	@Inject
	MediaTrackControlsPresenter(ApplicationContext context, MediaTrackControlsView view,
								ViewContextFactory viewFactory,
								RecordingFileService recordingService,
								RecordingPlaybackService playbackService,
								SearchService searchService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.recordingService = recordingService;
		this.playbackService = playbackService;
		this.searchService = searchService;
		this.zoomConstraints = new ZoomConstraints(1, 50);
	}

	@Override
	public void initialize() {
		EditorContext editorContext = (EditorContext) context;
		BooleanProperty canSplitAndSaveRecordingProperty = new BooleanProperty(false);

		view.setOnAdjustVolume(this::adjustAudio);
		view.setOnCut(this::cut);
		view.setOnCollapseSelection(this::collapseSelection);
		view.setOnDeletePage(this::deletePage);
		view.setOnReplacePage(this::replacePage);
		view.setOnUndo(this::undo);
		view.setOnRedo(this::redo);
		view.setOnImportRecording(this::importRecording);
		view.setOnSearch(this::search);
		view.setOnPreviousFoundPage(this::previousFoundPage);
		view.setOnNextFoundPage(this::nextFoundPage);
		view.setOnZoomIn(this::zoomIn);
		view.setOnZoomOut(this::zoomOut);
		view.setOnSplitAndSaveRecording(this::splitAndSaveRecording);
		view.bindZoomLevel(zoomConstraints, editorContext.trackZoomLevelProperty());
		view.bindCanCut(editorContext.canCutProperty());
		view.bindCanDeletePage(editorContext.canDeletePageProperty());
		view.bindCanRedo(editorContext.canRedoProperty());
		view.bindCanUndo(editorContext.canUndoProperty());
		view.bindCanSplitAndSaveRecording(canSplitAndSaveRecordingProperty);

		editorContext.primarySelectionProperty().addListener((observable, oldValue, newValue) -> {
			canSplitAndSaveRecordingProperty.set(0 < newValue && newValue < 1);
		});

		context.getEventBus().register(this);
	}

	@Override
	public void destroy() {
		context.getEventBus().unregister(this);
	}

	@Subscribe
	public void onEvent(DocumentEvent event) {
		if (event.created()) {
			searchService.createIndex(event.getDocument())
				.exceptionally(throwable -> {
					logException(throwable, "Create search index failed");
					return null;
				});
		}
		else if (event.closed()) {
			searchService.destroyIndex(event.getDocument())
				.exceptionally(throwable -> {
					logException(throwable, "Destroy search index failed");
					return null;
				});
		}
	}

	private void undo() {
		recordingService.undoChanges()
			.exceptionally(throwable -> {
				handleException(throwable, "Undo recording changes failed", "recording.undo.error");
				return null;
			});
	}

	private void redo() {
		recordingService.redoChanges()
			.exceptionally(throwable -> {
				handleException(throwable, "Redo recording changes failed", "recording.redo.error");
				return null;
			});
	}

	private void adjustAudio() {
		EditorContext editorContext = (EditorContext) context;
		double position = editorContext.getPrimarySelection();

		context.getEventBus().post(new AdjustAudioCommand(position));
	}

	private void cut() {
		EditorContext editorContext = (EditorContext) context;

		double timeSelect1 = editorContext.getLeftSelection();
		double timeSelect2 = editorContext.getRightSelection();

		double start = Math.min(timeSelect1, timeSelect2);
		double end = Math.max(timeSelect1, timeSelect2);

		recordingService.cut(start, end)
				.exceptionally(throwable -> {
					handleException(throwable, "Cut recording failed", "cut.recording.error");
					return null;
				});
	}

	private void collapseSelection() {
		EditorContext editorContext = (EditorContext) context;
		double primaryValue = editorContext.getPrimarySelection();

		editorContext.setLeftSelection(primaryValue);
		editorContext.setRightSelection(primaryValue);
	}

	private void deletePage() {
		EditorContext editorContext = (EditorContext) context;
		double timeNorm = editorContext.getPrimarySelection();

		recordingService.deletePage(timeNorm)
				.exceptionally(throwable -> {
					handleException(throwable, "Delete page failed", "delete.page.error");
					return null;
				}).join();
	}

	private void replacePage() {
		final String pathContext = EditorContext.SLIDES_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.pdf"),
				EditorContext.SLIDES_EXTENSION);

		File file = fileChooser.showOpenFile(view);

		if (nonNull(file)) {
			config.getContextPaths().put(pathContext, file.getParent());

			CompletableFuture.runAsync(() -> {
				Document doc;

				try {
					doc = new Document(file);
				}
				catch (IOException e) {
					throw new CompletionException(e);
				}

				context.getEventBus().post(new ReplacePageCommand(doc));
			})
			.exceptionally(throwable -> {
				handleException(throwable, "Open document failed",
						"open.document.error", file.getPath());
				return null;
			});
		}
	}

	private void importRecording() {
		final String pathContext = EditorContext.RECORDING_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.recording"),
				EditorContext.RECORDING_EXTENSION);

		File file = fileChooser.showOpenFile(view);

		if (nonNull(file)) {
			EditorContext editorContext = (EditorContext) context;

			recordingService.importRecording(file, editorContext.getPrimarySelection())
					.exceptionally(throwable -> {
						handleException(throwable, "Open recording failed", "open.recording.error", file.getPath());
						return null;
					});
		}
	}

	/**
	 * Splits the recording at the position marked by the primary selector.
	 * The selected part (left or right of the primary selector) gets cut out and saved into a separate file.
	 */
	private void splitAndSaveRecording() {
		CompletableFuture.runAsync(() -> {
					long duration = playbackService.getDuration().getMillis();
					long selectedTimeMs = (long) (((EditorContext) context).getPrimarySelection() * duration);

					Interval<Long> begin = new Interval<>(0L, selectedTimeMs);
					Interval<Long> end = new Interval<>(selectedTimeMs, duration);

					context.getEventBus().post(new SplitAndSaveRecordingCommand(begin, end));
				})
				.exceptionally(throwable -> {
					handleException(throwable, "Splitting and saving recording failed",
							"recording.split.error");
					return null;
				});
	}


	private void search(String text) {
		if (isNull(text) || text.isEmpty() || text.isBlank()) {
			view.setSearchState(null);
		}
		else {
			searchService.searchIndex(text)
					.thenAccept(searchResult -> {
						searchState = new SearchState(searchResult);

						view.setSearchState(searchState);
					})
				.exceptionally(throwable -> {
					logException(throwable, "Search page index failed");
					return null;
				});
		}
	}

	private void previousFoundPage() {
		int pageIndex = searchState.selectPreviousIndex();

		try {
			playbackService.selectPage(pageIndex);
		}
		catch (Exception e) {
			handleException(e, "Select page failed", "select.recording.page.error");
		}

		view.setSearchState(searchState);
	}

	private void nextFoundPage() {
		int pageIndex = searchState.selectNextIndex();

		try {
			playbackService.selectPage(pageIndex);
		}
		catch (Exception e) {
			handleException(e, "Select page failed", "select.recording.page.error");
		}

		view.setSearchState(searchState);
	}

	private void zoomIn() {
		EditorContext editorContext = (EditorContext) context;
		double level = editorContext.getTrackZoomLevel();

		editorContext.setTrackZoomLevel(zoomConstraints.getValue(level + 1));
	}

	private void zoomOut() {
		EditorContext editorContext = (EditorContext) context;
		double level = editorContext.getTrackZoomLevel();

		editorContext.setTrackZoomLevel(zoomConstraints.getValue(level - 1));
	}
}
