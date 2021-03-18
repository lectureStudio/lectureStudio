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

import com.google.common.eventbus.Subscribe;

import java.io.File;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.model.ZoomConstraints;
import org.lecturestudio.editor.api.presenter.command.AdjustAudioCommand;
import org.lecturestudio.media.search.SearchService;
import org.lecturestudio.media.search.SearchState;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.view.MediaTrackControlsView;

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
		this.zoomConstraints = new ZoomConstraints(1, 10);
	}

	@Override
	public void initialize() {
		EditorContext editorContext = (EditorContext) context;

		view.setOnCut(this::cut);
		view.setOnDeletePage(this::deletePage);
		view.setOnUndo(this::undo);
		view.setOnRedo(this::redo);
		view.setOnImportRecording(this::importRecording);
		view.setOnSearch(this::search);
		view.setOnPreviousFoundPage(this::previousFoundPage);
		view.setOnNextFoundPage(this::nextFoundPage);
		view.setOnAdjustAudio(this::adjustAudio);
		view.setOnZoomIn(this::zoomIn);
		view.setOnZoomOut(this::zoomOut);
		view.bindZoomLevel(zoomConstraints, editorContext.trackZoomLevelProperty());
		view.bindCanCut(editorContext.canCutProperty());
		view.bindCanDeletePage(editorContext.canDeletePageProperty());
		view.bindCanRedo(editorContext.canRedoProperty());
		view.bindCanUndo(editorContext.canUndoProperty());

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

	private void deletePage() {
		EditorContext editorContext = (EditorContext) context;
		Recording recording = recordingService.getSelectedRecording();
		double timeNorm = editorContext.getPrimarySelection();
		int time = (int) (timeNorm * recording.getRecordedAudio().getAudioStream().getLengthInMillis());
		int pageIndex = recording.getPageIndex(time, 0);

		recordingService.deletePage(pageIndex)
				.exceptionally(throwable -> {
					handleException(throwable, "Delete page failed", "delete.page.error");
					return null;
				});
	}

	private void importRecording() {
		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.addExtensionFilter("Lecture Recordings", "presenter");

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

	private Double adjustValue = null;

	private void adjustAudio(double delta) {
		if (adjustValue != null) {
			delta = adjustValue - delta;
		}
		else {
			adjustValue = delta;
		}

		context.getEventBus().post(new AdjustAudioCommand(delta));
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
