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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.presenter.AboutPresenter;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.CloseApplicationCommand;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.service.RecentDocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.MenuView;

public class MenuPresenter extends Presenter<MenuView> {

	private final EventBus eventBus;

	private final ViewContextFactory viewFactory;

	private final RecentDocumentService recentDocumentService;

	private final RecordingFileService recordingService;


	@Inject
	MenuPresenter(ApplicationContext context, MenuView view,
				  ViewContextFactory viewFactory,
				  RecentDocumentService recentDocumentService,
				  RecordingFileService recordingService) {
		super(context, view);

		this.eventBus = context.getEventBus();
		this.viewFactory = viewFactory;
		this.recentDocumentService = recentDocumentService;
		this.recordingService = recordingService;
	}

	@Override
	public void initialize() {
		EditorContext editorContext = (EditorContext) context;

		eventBus.register(this);

		view.setDocument(null);
		view.setRecentDocuments(recentDocumentService.getRecentDocuments());

		view.setOnOpenRecording(this::selectNewRecording);
		view.setOnOpenRecording(this::openRecording);
		view.setOnCloseRecording(this::closeSelectedRecording);
		view.setOnSaveRecordingAs(this::saveRecording);
		view.setOnExportAudio(this::exportAudio);
		view.setOnImportAudio(this::importAudio);
		view.setOnExit(this::exit);

		view.bindCanCut(editorContext.canCutProperty());
		view.bindCanRedo(editorContext.canRedoProperty());
		view.bindCanUndo(editorContext.canUndoProperty());
		view.setOnUndo(this::undo);
		view.setOnRedo(this::redo);
		view.setOnCut(this::cut);
		view.setOnDeletePage(this::deletePage);
		view.setOnSettings(this::showSettingsView);

		view.bindFullscreen(context.fullscreenProperty());

		view.setOnOpenLog(this::showLog);
		view.setOnOpenAbout(this::showAboutView);
	}

	@Override
	public void destroy() {
		eventBus.unregister(this);
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (event.created() || event.closed()) {
			view.setRecentDocuments(recentDocumentService.getRecentDocuments());
		}

		Document doc = event.closed() ? null : event.getDocument();

		view.setDocument(doc);
	}

	public void openRecording(File file) {
		recordingService.openRecording(file)
			.thenRun(() -> {
				RecentDocument recentDoc = new RecentDocument();
				recentDoc.setDocumentName(FileUtils.stripExtension(file.getName()));
				recentDoc.setDocumentPath(file.getAbsolutePath());
				recentDoc.setLastModified(new Date());

				recentDocumentService.add(recentDoc);
			})
			.exceptionally(throwable -> {
				handleException(throwable, "Open recording failed", "open.recording.error", file.getPath());
				return null;
			});
	}

	public void closeSelectedRecording() {
		recordingService.closeSelectedRecording();
	}

	private void saveRecording() {
		final String pathContext = EditorContext.RECORDING_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		String title = recordingService.getSelectedRecording().getRecordedDocument().getDocument().getTitle();
		String fileName = title + "-edit";

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter(dict.get("file.description.recording"),
				EditorContext.RECORDING_EXTENSION);
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.setInitialFileName(fileName);

		File file = fileChooser.showSaveFile(view);

		if (nonNull(file)) {
			ProgressDialogView progressView = viewFactory.getInstance(ProgressDialogView.class);
			progressView.setMessageTitle(context.getDictionary().get("save.recording"));
			progressView.setParent(view);
			progressView.open();

			recordingService.saveRecording(file, progressView::setProgress)
				.thenRun(() -> {
					progressView.setMessageTitle(context.getDictionary().get("save.recording.success"));
				})
				.exceptionally(throwable -> {
					handleException(throwable, "Save recording failed", "save.recording.error", file.getPath());
					return null;
				});
		}
	}

	public void exit() {
		eventBus.post(new CloseApplicationCommand());
	}

	public void undo() {
		recordingService.undoChanges()
			.exceptionally(throwable -> {
				handleException(throwable, "Undo recording changes failed", "recording.undo.error");
				return null;
			});
	}

	public void redo() {
		recordingService.redoChanges()
			.exceptionally(throwable -> {
				handleException(throwable, "Redo recording changes failed", "recording.redo.error");
				return null;
			});
	}

	public void showSettingsView() {
		eventBus.post(new ShowPresenterCommand<>(SettingsPresenter.class));
	}

	public void showLog() {
		try {
			Desktop.getDesktop().open(new File(
					context.getDataLocator().getAppDataPath()));
		}
		catch (IOException e) {
			handleException(e, "Open log path failed", "generic.error");
		}
	}

	public void showAboutView() {
		eventBus.post(new ShowPresenterCommand<>(AboutPresenter.class));
	}

	private void selectNewRecording() {
		final String pathContext = EditorContext.RECORDING_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.recording"),
				EditorContext.RECORDING_EXTENSION);

		File selectedFile = fileChooser.showOpenFile(view);

		if (nonNull(selectedFile)) {
			openRecording(selectedFile);
		}
	}

	private void exportAudio() {
		final String pathContext = EditorContext.WAV_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Map<String, String> contextPaths = config.getContextPaths();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.wav"),
				EditorContext.WAV_EXTENSION);

		File file = fileChooser.showSaveFile(view);

		if (nonNull(file)) {
			contextPaths.put(pathContext, file.getParent());

			ProgressDialogView progressView = viewFactory.getInstance(ProgressDialogView.class);
			progressView.setMessageTitle(context.getDictionary().get("export.audio"));
			progressView.setParent(view);
			progressView.open();

			recordingService.exportAudio(file, progressView::setProgress)
				.exceptionally(throwable -> {
					handleException(throwable, "Export audio failed", "export.audio.error", file.getPath());
					return null;
				});
		}
	}

	private void importAudio() {
		final String pathContext = EditorContext.WAV_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Map<String, String> contextPaths = config.getContextPaths();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.wav"),
				EditorContext.WAV_EXTENSION);

		File file = fileChooser.showOpenFile(view);

		if (nonNull(file)) {
			contextPaths.put(pathContext, file.getParent());

			recordingService.importAudio(file)
				.exceptionally(throwable -> {
					handleException(throwable, "Import audio failed", "import.audio.error", file.getPath());
					return null;
				});
		}
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
}
