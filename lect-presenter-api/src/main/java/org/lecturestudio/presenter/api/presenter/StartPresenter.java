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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.nonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.StartView;

public class StartPresenter extends Presenter<StartView> {

	@Inject
	private ViewContextFactory viewFactory;

	@Inject
	private DocumentService documentService;


	@Inject
	StartPresenter(ApplicationContext context, StartView view) {
		super(context, view);
	}

	public void openDocument(File documentFile) {
		documentService.openDocument(documentFile)
			.exceptionally(throwable -> {
				handleException(throwable, "Open document failed",
						"open.document.error", documentFile.getPath());
				return null;
			});
	}

	public void openWhiteboard() {
		documentService.addWhiteboard();
	}

	public void openRecentDocument(RecentDocument doc) {
		File file = new File(doc.getDocumentPath());

		if (!file.isDirectory()) {
			openDocument(file);
		}
	}

	private void selectNewDocument() {
		final String pathContext = PresenterContext.SLIDES_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Map<String, String> contextPaths = config.getContextPaths();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.pdf"),
				PresenterContext.SLIDES_EXTENSION);

		File selectedFile = fileChooser.showOpenFile(view);

		if (nonNull(selectedFile)) {
			contextPaths.put(pathContext, selectedFile.getParent());

			openDocument(selectedFile);
		}
	}

	@Override
	public void initialize() {
		ObservableList<RecentDocument> recentDocuments = context.getConfiguration()
				.getRecentDocuments();
		recentDocuments.addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<RecentDocument> list) {
				view.setRecentDocuments(list);
			}
		});

		view.setOnOpenRecentDocument(this::openRecentDocument);
		view.setOnOpenDocument(this::selectNewDocument);
		view.setOnOpenWhiteboard(this::openWhiteboard);
		view.setRecentDocuments(recentDocuments);
	}
}
