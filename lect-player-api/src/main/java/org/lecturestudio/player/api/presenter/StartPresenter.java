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

package org.lecturestudio.player.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.RecentDocumentService;
import org.lecturestudio.core.service.SearchFileService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.media.recording.RecordingFileService;
import org.lecturestudio.player.api.context.PlayerContext;
import org.lecturestudio.player.api.view.StartView;

public class StartPresenter extends Presenter<StartView> {

	private final ViewContextFactory viewFactory;

	private final RecentDocumentService recentDocumentService;

	private final RecordingFileService recordingService;

	private final SearchFileService searchFileService;


	@Inject
	StartPresenter(ApplicationContext context, StartView view,
				   ViewContextFactory viewFactory,
				   RecentDocumentService recentDocumentService,
				   RecordingFileService recordingService,
				   SearchFileService searchFileService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.recentDocumentService = recentDocumentService;
		this.recordingService = recordingService;
		this.searchFileService = searchFileService;
	}

	@Override
	public void initialize() {
		view.setRecentRecording(recentDocumentService.getRecentDocuments());
		view.setOnOpenRecentRecording(this::openRecentRecording);
		view.setOnOpenRecording(this::selectNewRecording);
		view.setOnSearch(this::search);
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

	public void openRecentRecording(RecentDocument doc) {
		File file = new File(doc.getDocumentPath());

		if (!file.isDirectory()) {
			openRecording(file);
		}
	}

	private void selectNewRecording() {
		final String pathContext = PlayerContext.RECORDING_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Map<String, String> contextPaths = config.getContextPaths();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.recording"),
				PlayerContext.RECORDING_EXTENSION, "plr");

		File selectedFile = fileChooser.showOpenFile(view);

		if (nonNull(selectedFile)) {
			contextPaths.put(pathContext, selectedFile.getParent());

			openRecording(selectedFile);
		}
	}

	private void search(String text) {
		if (isNull(text) || text.isEmpty() || text.isBlank()) {
			view.setSearchResult(null);
			return;
		}

		searchFileService.search(System.getProperty("user.home"), "*" + text + "*.presenter")
			.thenAccept(view::setSearchResult)
			.exceptionally(throwable -> {
				logException(throwable, "Search recordings failed");
				return null;
			});
	}
}
