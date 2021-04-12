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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.ProgressPresenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.render.RenderService;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.presenter.api.pdf.PdfFactory;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;
import org.lecturestudio.swing.renderer.ArrowRenderer;
import org.lecturestudio.swing.renderer.EllipseRenderer;
import org.lecturestudio.swing.renderer.LineRenderer;
import org.lecturestudio.swing.renderer.PDFTeXRenderer;
import org.lecturestudio.swing.renderer.PDFTextRenderer;
import org.lecturestudio.swing.renderer.RectangleRenderer;
import org.lecturestudio.swing.renderer.StrokeRenderer;
import org.lecturestudio.swing.renderer.TextSelectionRenderer;

public class SaveDocumentsPresenter extends Presenter<SaveDocumentsView> {

	private final DocumentService documentService;

	private final ViewContextFactory viewFactory;

	private final List<Document> selectedDocuments;

	private final SimpleDateFormat dateFormat;

	private final StringProperty savePath;


	@Inject
	SaveDocumentsPresenter(ApplicationContext context, SaveDocumentsView view,
			ViewContextFactory viewFactory, DocumentService documentService) {
		super(context, view);

		this.documentService = documentService;
		this.viewFactory = viewFactory;
		this.dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		this.selectedDocuments = new ArrayList<>();
		this.savePath = new StringProperty();
	}

	@Override
	public void initialize() {
		DocumentRecorder documentRecorder = documentService.getDocumentRecorder();

		for (Document doc : documentRecorder.getRecordedDocuments()) {
			SaveDocumentOptionView optionView = createDocumentOptionView(doc);

			view.addDocumentOptionView(optionView);
		}

		savePath.set(System.getProperty("user.home") + File.separator + getFileName(null));

		view.setSavePath(savePath);
		view.setOnClose(this::close);
		view.setOnMerge(this::saveSelectedDocuments);
		view.setOnSelectPath(this::selectSavePath);
	}

	private void saveSelectedDocuments() {
		File file = new File(savePath.get());

		saveDocuments(selectedDocuments, file);
	}

	private void saveDocument(Document doc) {
		String initFile = getFileName(doc.getName());
		File initDirectory = new File(savePath.get()).getParentFile();

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter("PDF Files", "pdf");
		fileChooser.setInitialFileName(initFile);
		fileChooser.setInitialDirectory(initDirectory);

		File selectedFile = fileChooser.showSaveFile(view);

		if (nonNull(selectedFile)) {
			saveDocuments(List.of(doc), selectedFile);
		}
	}

	private void selectDocument(Document doc) {
		selectedDocuments.add(doc);
	}

	private void deselectDocument(Document doc) {
		selectedDocuments.remove(doc);
	}

	private void selectSavePath() {
		File initPath = new File(savePath.get());

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.addExtensionFilter("PDF Files", "pdf");
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

	private void saveDocuments(List<Document> documents, File file) {
		context.getEventBus().post(new ShowPresenterCommand<>(ProgressPresenter.class) {
			@Override
			public void execute(ProgressPresenter presenter) {
				ProgressView progressView = presenter.getView();
				progressView.setTitle(context.getDictionary().get("save.documents.saving"));
				progressView.setMessage(file.getAbsolutePath());
				progressView.setOnViewShown(() -> {
					saveAsync(progressView, documents, file);
				});
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
		DocumentRecorder documentRecorder = documentService.getDocumentRecorder();
		List<Page> recPages = documentRecorder.getRecordedPages();

		RenderService renderService = new RenderService();
		renderService.registerRenderer(new ArrowRenderer());
		renderService.registerRenderer(new EllipseRenderer());
		renderService.registerRenderer(new LineRenderer());
		renderService.registerRenderer(new PDFTeXRenderer());
		renderService.registerRenderer(new PDFTextRenderer());
		renderService.registerRenderer(new StrokeRenderer());
		renderService.registerRenderer(new RectangleRenderer());
		renderService.registerRenderer(new TextSelectionRenderer());

		CompletableFuture.runAsync(() -> {
			try {
				PdfFactory.writeDocumentsToPDF(renderService, file, documents,
						recPages, progressView::setProgress, true);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		})
		.thenRun(() -> {
			progressView.setTitle(context.getDictionary().get("save.documents.success"));
		})
		.exceptionally(throwable -> {
			logException(throwable, "Write document to PDF failed");

			progressView.setError(context.getDictionary().get("document.save.error"));
			return null;
		});
	}
}