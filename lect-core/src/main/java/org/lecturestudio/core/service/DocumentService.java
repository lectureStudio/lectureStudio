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

package org.lecturestudio.core.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.*;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

@Singleton
public class DocumentService {

	private static final Logger LOG = LogManager.getLogger(DocumentService.class);

	private final ApplicationContext context;

	private final DocumentList documents;


	@Inject
	public DocumentService(ApplicationContext context) {
		this.context = context;
		this.documents = new DocumentList();
	}

	/**
	 * @return A list of all opened documents.
	 */
	public DocumentList getDocuments() {
		return documents;
	}

	/**
	 * Creates and selects a new whiteboard.
	 */
	public CompletableFuture<Document> addWhiteboard() {
		return CompletableFuture.supplyAsync(() -> {
			Document whiteboard;

			try {
				whiteboard = createWhiteboard();
			}
			catch (Exception e) {
				throw new CompletionException("Create whiteboard failed", e);
			}

			addDocument(whiteboard);
			selectDocument(whiteboard);

			return whiteboard;
		});
	}

	/**
	 * Creates and selects a new whiteboard.
	 *
	 * @param templatePath The file path of the template document.
	 */
	public CompletableFuture<Document> addWhiteboard(String templatePath) {
		final File file = new File(nonNull(templatePath) ? templatePath : "");

		return CompletableFuture.supplyAsync(() -> {
			Document whiteboard;

			try {
				whiteboard = createWhiteboard(file);
			}
			catch (Exception e) {
				throw new CompletionException("Create whiteboard failed", e);
			}

			addDocument(whiteboard);
			selectDocument(whiteboard);

			return whiteboard;
		});
	}

	/**
	 * Opens a whiteboard document. If no whiteboards are present, a new
	 * whiteboard is created. If more than one whiteboard is present, then the
	 * first whiteboard in the document list will be opened.
	 */
	public CompletableFuture<Document> openWhiteboard(String templatePath) {
		final File file = new File(nonNull(templatePath) ? templatePath : "");

		return CompletableFuture.supplyAsync(() -> {
			// Search for a opened whiteboard.
			Document whiteboard = documents.getFirstWhiteboard();

			// If there isn't any whiteboard, then create one.
			if (isNull(whiteboard)) {
				try {
					whiteboard = createWhiteboard(file);
				}
				catch (IOException e) {
					throw new CompletionException("Create whiteboard failed", e);
				}

				addDocument(whiteboard);
			}

			selectDocument(whiteboard);

			return whiteboard;
		});
	}

	/**
	 * Toggles whiteboard visibility. If a whiteboard is opened and visible, the
	 * last non-whiteboard document will be shown.
	 */
	public void toggleWhiteboard() {
		Document selectedDocument = documents.getSelectedDocument();

		if (isNull(selectedDocument) || !selectedDocument.isWhiteboard()) {
			openWhiteboard("").join();
		}
		else {
			selectDocument(documents.getLastNonWhiteboard());
		}
	}

	/**
	 * Opens a document and sets it as the active document.
	 *
	 * @param docFile the document file.
	 */
	public CompletableFuture<Document> openDocument(File docFile) {
		return CompletableFuture.supplyAsync(() -> {
			Optional<Document> fileDoc = documents.getDocumentByFile(docFile);

			if (fileDoc.isPresent()) {
				return fileDoc.get();
			}

			Document doc;

			try {
				doc = new Document(docFile);
			}
			catch (IOException e) {
				throw new CompletionException("Open document failed", e);
			}

			addDocument(doc);
			selectDocument(doc);

			updateRecentDocuments(doc);
			return doc;
		});
	}

	public void addDocument(Document doc) {
		if (documents.add(doc)) {
			context.getEventBus().post(new DocumentEvent(doc,
					DocumentEvent.Type.CREATED));
		}
	}

	public boolean removeDocument(Document doc) {
		boolean removed = documents.remove(doc);

		if (removed) {
			context.getEventBus().post(new DocumentEvent(doc,
					DocumentEvent.Type.CLOSED));
		}

		return removed;
	}

	public void removeAndCloseDocument(Document doc) {
		if (removeDocument(doc)) {
			// Release resources.
			// NOTE: do not close due to "save documents on app close".
			// doc.close();
		}
	}

	public void replaceDocument(Document newDoc) {
		replaceDocument(documents.getSelectedDocument(), newDoc);
	}

	public void replaceDocument(Document oldDoc, Document newDoc) {
		if (documents.replace(oldDoc, newDoc)) {
			context.getEventBus().post(new DocumentEvent(oldDoc, newDoc,
					DocumentEvent.Type.REPLACED));
		}
	}

	public void replaceDocument(Document oldDoc, Document newDoc,
			boolean copyAnnotations) {
		if (documents.replace(oldDoc, newDoc)) {
			if (copyAnnotations) {
				int pageCount = Math.min(oldDoc.getPageCount(), newDoc.getPageCount());

				for (int i = 0; i < pageCount; i++) {
					Page newPage = newDoc.getPage(i);
					Page oldPage = oldDoc.getPage(i);

					newPage.copy(oldPage);

					copyPagePresentation(oldPage, newPage, ViewType.User);
					copyPagePresentation(oldPage, newPage, ViewType.Presentation);
					copyPagePresentation(oldPage, newPage, ViewType.Preview);
				}
			}

			context.getEventBus().post(new DocumentEvent(oldDoc, newDoc,
					DocumentEvent.Type.REPLACED));
		}
	}

	public void selectDocument(Document doc) {
		Document oldDoc = documents.getSelectedDocument();

		if (documents.select(doc)) {
			context.getEventBus().post(new DocumentEvent(oldDoc, doc,
					DocumentEvent.Type.SELECTED));
		}
	}

	public void selectLastDocument() {
		selectDocument(documents.getSelectedDocument());
	}

	public void closeAllDocuments() {
		closeDocuments(documents.asList());
	}

	public void closeSelectedDocument() {
		removeAndCloseDocument(documents.getSelectedDocument());
		selectLastDocument();
	}

	public void closeDocument(Document document) {
		if (isNull(document)) {
			return;
		}

		removeAndCloseDocument(document);

		selectLastDocument();
	}

	public void closeDocuments(List<Document> documents) {
		if (isNull(documents) || documents.isEmpty()) {
			return;
		}

		for (Document doc : documents) {
			removeAndCloseDocument(doc);
		}

		selectLastDocument();
	}

	/**
	 * Creates a page on the active whiteboard. Does nothing if the active document
	 * is not a whiteboard.
	 */
	public Page createWhiteboardPage() {
		Document selectedDocument = documents.getSelectedDocument();

		if (nonNull(selectedDocument) && selectedDocument.isWhiteboard()) {
			Page page = selectedDocument.createPage();

			context.getEventBus().post(new PageEvent(page, PageEvent.Type.CREATED));

			selectPage(selectedDocument, page.getPageNumber());

			return page;
		}

		throw new IllegalArgumentException("No whiteboard selected");
	}

	/**
	 * Removes the selected page on the active whiteboard. Does nothing if the
	 * selected Document is not a whiteboard. If this leaded to an empty
	 * whiteboard, a new blank page is set as the first page of the whiteboard.
	 */
	public void deleteWhiteboardPage() {
		Document selectedDocument = documents.getSelectedDocument();

		if (isNull(selectedDocument) || !selectedDocument.isWhiteboard()) {
			throw new IllegalArgumentException("No whiteboard selected");
		}

		if (selectedDocument.getPageCount() > 0) {
			Page selectedPage = selectedDocument.getCurrentPage();
			int pageNumber = selectedPage.getPageNumber();

			if (selectedDocument.removePage(selectedPage)) {
				context.getEventBus().post(new PageEvent(selectedPage,
						PageEvent.Type.REMOVED));

				// Check if the removed page was selected.
				if (pageNumber == selectedDocument.getPages().size()) {
					pageNumber--;
				}

				if (selectedDocument.getPageCount() == 0) {
					Page page = selectedDocument.createPage();

					context.getEventBus().post(new PageEvent(page,
							PageEvent.Type.CREATED));

					pageNumber = page.getPageNumber();
				}

				selectPage(selectedDocument, pageNumber);
			}
		}
	}

	/**
	 * Selects the given page in the selected document.
	 *
	 * @param page The page to select.
	 */
	public void selectPage(Page page) {
		Document selectedDocument = documents.getSelectedDocument();

		if (nonNull(selectedDocument) && page.getDocument() == selectedDocument) {
			selectPage(selectedDocument, page.getPageNumber());
		}
	}

	/**
	 * Selects the next page in the selected document.
	 */
	public void selectNextPage() {
		Document doc = documents.getSelectedDocument();

		if (nonNull(doc)) {
			int currentPage = doc.getCurrentPageNumber();

			selectPage(doc, currentPage + 1);
		}
	}

	/**
	 * Selects the previous page in the selected Document.
	 */
	public void selectPreviousPage() {
		Document doc = documents.getSelectedDocument();

		if (nonNull(doc)) {
			int currentPage = doc.getCurrentPageNumber();

			selectPage(doc, currentPage - 1);
		}
	}

	public void selectPage(int pageNumber) {
		Document doc = documents.getSelectedDocument();

		if (nonNull(doc)) {
			selectPage(doc, pageNumber);
		}
	}

	public void selectNotesPosition(NotesPosition pos) {
		Document doc = documents.getSelectedDocument();

		if (nonNull(doc)) {
			doc.setSplitSlideNotesPosition(pos);
			doc.calculateCropBox();
		}
		context.getEventBus().post(new PageEvent(doc.getCurrentPage(),
				PageEvent.Type.SELECTED));
	}

	private void selectPage(Document document, int pageNumber) {
		int currentPageNumber = document.getCurrentPageNumber();

		if (document.selectPage(pageNumber)) {
			Page oldPage = document.getPage(currentPageNumber);
			Page newPage = document.getPage(pageNumber);

			context.getEventBus().post(new PageEvent(newPage, oldPage,
					PageEvent.Type.SELECTED));
		}
	}

	/**
	 * Creates a new whiteboard from a template document.
	 *
	 * @param templateFile The file path of the template document.
	 *
	 * @return a newly created whiteboard document.
	 *
	 * @throws IOException if the whiteboard could not be created.
	 */
	private Document createWhiteboard(File templateFile) throws IOException {
		String name = "Whiteboard-" + documents.getWhiteboardCount();
		Document whiteboard;

		if (templateFile.exists()) {
			whiteboard = new TemplateDocument(templateFile);
			whiteboard.setTitle(name);
			whiteboard.setDocumentType(DocumentType.WHITEBOARD);
			whiteboard.createPage();
		}
		else {
			whiteboard = createWhiteboard();
		}

		return whiteboard;
	}

	/**
	 * Creates a new whiteboard.
	 *
	 * @return a newly created whiteboard document.
	 *
	 * @throws IOException if the whiteboard could not be created.
	 */
	private Document createWhiteboard() throws IOException {
		Document prevDoc = getDocuments().getSelectedDocument();
		String name = "Whiteboard-" + documents.getWhiteboardCount();

		Document whiteboard = new Document();
		whiteboard.setTitle(name);
		whiteboard.setDocumentType(DocumentType.WHITEBOARD);

		if (nonNull(prevDoc)) {
			Rectangle2D pageRect = prevDoc.getPage(0).getPageRect();
			whiteboard.setPageSize(new Dimension2D(
					pageRect.getWidth(),
					pageRect.getHeight()));
		}

		whiteboard.createPage();

		return whiteboard;
	}

	private void updateRecentDocuments(Document doc) {
		Configuration config = context.getConfiguration();
		List<RecentDocument> docs = config.getRecentDocuments();

		RecentDocument recentDoc = new RecentDocument();
		recentDoc.setDocumentName(doc.getName());
		recentDoc.setDocumentPath(doc.getFile().getPath());
		recentDoc.setLastModified(new Date(System.currentTimeMillis()));

		// Remove not existing documents.
		Iterator<RecentDocument> iter = docs.iterator();
		while (iter.hasNext()) {
			RecentDocument d = iter.next();
			File file = new File(d.getDocumentPath());

			if (!file.exists()) {
				iter.remove();
			}
		}

		docs.remove(recentDoc);

		if (docs.size() > 4) {
			docs.remove(docs.size() - 1);
		}

		docs.add(0, recentDoc);

		saveConfiguration();
	}

	private void saveConfiguration() {
		try {
			context.saveConfiguration();
		}
		catch (Exception e) {
			LOG.error("Save configuration failed", e);
		}
	}

	private void copyPagePresentation(Page oldPage, Page newPage, ViewType viewType) {
		PresentationParameterProvider presentation = context.getPagePropertyProvider(viewType);

		PresentationParameter param = presentation.getParameter(newPage);
		param.copy(presentation.getParameter(oldPage));
	}
}
