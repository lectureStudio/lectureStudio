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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.ProgressPresenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;
import org.lecturestudio.swing.renderer.PdfDocumentRenderer;

public class SaveDocumentsPresenter extends Presenter<SaveDocumentsView> {

	private final DocumentRecorder documentRecorder;

	private final ViewContextFactory viewFactory;

	private final List<Document> selectedDocuments;

	private final SimpleDateFormat dateFormat;

	private final StringProperty savePath;


	@Inject
	SaveDocumentsPresenter(ApplicationContext context, SaveDocumentsView view,
			ViewContextFactory viewFactory, DocumentRecorder documentRecorder) {
		super(context, view);

		this.documentRecorder = documentRecorder;
		this.viewFactory = viewFactory;
		this.dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		this.selectedDocuments = new ArrayList<>();
		this.savePath = new StringProperty();
	}

	@Override
	public void initialize() {
		for (Document doc : documentRecorder.getRecordedDocuments()) {
			SaveDocumentOptionView optionView = createDocumentOptionView(doc);

			view.addDocumentOptionView(optionView);

			optionView.select();
		}

		final String pathContext = PresenterContext.SLIDES_TO_PDF_CONTEXT;
		Configuration config = context.getConfiguration();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		savePath.set(dirPath.resolve(getFileName(null)).toString());

		view.setSavePath(savePath);
		view.setOnClose(this::close);
		view.setOnMerge(this::saveSelectedDocuments);
		view.setOnSelectPath(this::selectSavePath);
	}

	private void saveSelectedDocuments() {
		File file = new File(savePath.get());

		saveDocuments(selectedDocuments, file, true);
	}

	private void saveDocument(Document doc) {
		final String pathContext = PresenterContext.SLIDES_TO_PDF_CONTEXT;
		Configuration config = context.getConfiguration();
		Dictionary dict = context.getDictionary();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter(dict.get("file.description.pdf"),
				PresenterContext.SLIDES_EXTENSION);
		fileChooser.setInitialFileName(getFileName(doc.getName()));
		fileChooser.setInitialDirectory(dirPath.toFile());

		File selectedFile = fileChooser.showSaveFile(view);

		if (nonNull(selectedFile)) {
			saveDocuments(List.of(doc), selectedFile, false);
		}
	}

	private void selectDocument(Document doc) {
		selectedDocuments.add(doc);
	}

	private void deselectDocument(Document doc) {
		selectedDocuments.remove(doc);
	}

	private void selectSavePath() {
		Dictionary dict = context.getDictionary();
		File initPath = new File(savePath.get());

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter(dict.get("file.description.pdf"),
				PresenterContext.SLIDES_EXTENSION);
		fileChooser.setInitialFileName(initPath.getName());
		fileChooser.setInitialDirectory(initPath.getParentFile());

		File selectedFile = fileChooser.showSaveFile(view);

		if (nonNull(selectedFile)) {
			savePath.set(selectedFile.getAbsolutePath());
		}
	}

	private String getFileName(String docName) {
		if (isNull(docName)) {
			docName = context.getDictionary().get("document.save.lecture");
		}

		String date = dateFormat.format(new Date());

		return docName + "-" + date + ".pdf";
	}

	private void saveDocuments(List<Document> documents, File file, boolean autoClose) {
		Configuration config = context.getConfiguration();
		config.getContextPaths().put(PresenterContext.SLIDES_TO_PDF_CONTEXT,
				file.getParent());

		context.getEventBus().post(new ShowPresenterCommand<>(ProgressPresenter.class) {
			@Override
			public void execute(ProgressPresenter presenter) {
				ProgressView progressView = presenter.getView();
				progressView.setTitle(context.getDictionary().get("save.documents.saving"));
				progressView.setMessage(file.getAbsolutePath());
				progressView.setOnViewShown(() -> {
					saveAsync(progressView, documents, file);
				});

				if (autoClose) {
					progressView.setOnClose(() -> close());
				}
			}
		});
	}

	private SaveDocumentOptionView createDocumentOptionView(Document doc) {
		SaveDocumentOptionView optionView = viewFactory.getInstance(SaveDocumentOptionView.class);
		optionView.setDocumentTitle(doc.getName());
		optionView.setOnSaveDocument(() -> {
			saveDocument(doc);
		});
		optionView.setOnSelectDocument(() -> {
			selectDocument(doc);
		});
		optionView.setOnDeselectDocument(() -> {
			deselectDocument(doc);
		});

		return optionView;
	}

	private void saveAsync(ProgressView progressView, List<Document> documents, File file) {
		CompletableFuture.runAsync(() -> {
			PdfDocumentRenderer documentRenderer = new PdfDocumentRenderer();
			documentRenderer.setDocuments(documents);
			documentRenderer.setPages(documentRecorder.getRecordedPages());
			documentRenderer.setParameterProvider(documentRecorder.getRecordedParamProvider());
			documentRenderer.setProgressCallback(progressView::setProgress);
			documentRenderer.setOutputFile(file);

			try {
				documentRenderer.start();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.thenRun(() -> {
			PresenterContext presenterContext = (PresenterContext) context;
			presenterContext.setHasRecordedChanges(false);

			progressView.setTitle(context.getDictionary().get("save.documents.success"));
		})
		.exceptionally(throwable -> {
			logException(throwable, "Write document to PDF failed");

			progressView.setError(context.getDictionary().get("document.save.error"));
			return null;
		});
	}
}