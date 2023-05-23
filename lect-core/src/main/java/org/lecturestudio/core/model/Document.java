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

package org.lecturestudio.core.model;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.util.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents a document. Documents consist of a list Pages and can
 * be Whiteboard-Documents or normal Documents. Documents have a handle so that
 * the PDF-API can associate its specific information with the document.
 *
 * @author Alex Andres
 */
public class Document {

	/**
	 * Logger for {@link Document}.
	 */
	protected static final Logger LOG = LogManager.getLogger(Document.class);

	private final List<DocumentChangeListener> changeListeners = new ArrayList<>();

	/**
	 * A list with all the pages of the document.
	 */
	private final List<Page> pages = new ArrayList<>();

	/**
	 * The opened document file. May be null, if this document is not loaded
	 * from a file.
	 */
	private File file;

	/**
	 * The type of the document.
	 */
	private DocumentType type;

	/**
	 * The size of the pages.
	 */
	private Dimension2D pageSize;

	/**
	 * The PDF document.
	 */
	private PdfDocument pdfDocument;

	/**
	 * The title of the PDF document.
	 */
	private String title;

	/** The index of the current page. */
	private int currentPageNumber = 0;

	/** The unique ID of this document. */
	private UUID uid;


	/**
	 * Create a new {@link Document}.
	 * (Calls {@link #Document(PdfDocument)} with a new {@link PdfDocument})
	 */
	public Document() throws IOException {
		this(new PdfDocument());
	}

	/**
	 * Create a new {@link Document} with the specified file. (Sets
	 * {@link #file} to the specified file and calls
	 * {@link #Document(PdfDocument)} with a new {@link PdfDocument} by calling
	 * {@link PdfDocument#PdfDocument(File)} with the specified file)
	 *
	 * @param file The file.
	 */
	public Document(File file) throws IOException {
		this(new PdfDocument(file));
		this.file = file;
	}

	/**
	 * Create a new {@link Document} with the specified byte array. (Calls
	 * {@link #Document(PdfDocument)} with a new {@link PdfDocument} by calling
	 * {@link PdfDocument#PdfDocument(byte[])} with the specified file)
	 *
	 * @param byteArray The byte array.
	 */
	public Document(byte[] byteArray) throws IOException {
		this(new PdfDocument(byteArray));
	}

	/**
	 * Create a new {@link Document} with the specified PDF document.
	 * (Calls {@link #init(PdfDocument)} with the specified PDF document and
	 * sets the page size to width = 640 and height = 480)
	 *
	 * @param pdfDocument The PDF document.
	 */
	public Document(PdfDocument pdfDocument) {
		init(pdfDocument);
		setUid(UUID.randomUUID());
		setPageSize(new Dimension2D(640, 480));
	}

	/**
	 * Get unique document ID. The unique ID can be used to differentiate
	 * documents of same type but with different internal content.
	 *
	 * @return The unique document ID.
	 */
	public UUID getUid() {
		return uid;
	}

	/**
	 * Set unique document ID. The unique ID can be used to differentiate
	 * documents of same type but with different internal content.
	 *
	 * @param uid The unique document ID.
	 */
	public void setUid(UUID uid) {
		this.uid = uid;
	}

	/**
	 * Get the outline of the document.
	 *
	 * @return The document outline.
	 */
	public DocumentOutline getDocumentOutline() {
		return pdfDocument.getDocumentOutline();
	}

	/**
	 * Get the renderer of the document.
	 *
	 * @return The document renderer.
	 */
	public DocumentRenderer getDocumentRenderer() {
		return pdfDocument.getDocumentRenderer();
	}

	/**
	 * Adds the specified document change listener to {@link #changeListeners}
	 * if it is not already included.
	 *
	 * @param listener The document change listener to add.
	 */
	public void addChangeListener(DocumentChangeListener listener) {
		if (!changeListeners.contains(listener)) {
			changeListeners.add(listener);
		}
	}

	/**
	 * Removes the specified document change listener from
	 * {@link #changeListeners}.
	 *
	 * @param listener The document change listener to remove.
	 */
	public void removeChangeListener(DocumentChangeListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Closes the document and removes all the pages from {@link #pages}.
	 */
	public void close() {
		closeDocument();

		pages.clear();
	}

	/**
	 * Specifies whether the document is closed.
	 *
	 * @return {@code true} if {@link #pdfDocument} equals {@code null},
	 * otherwise {@code false}.
	 */
	public boolean isClosed() {
		return pdfDocument == null;
	}

	private void closeDocument() {
		if (pdfDocument != null) {
			try {
				pdfDocument.close();
				pdfDocument = null;
			}
			catch (IOException e) {
				LOG.error("Close document failed.", e);
			}
		}
	}

	/**
	 * Sets the page size.
	 *
	 * @param size The new page size.
	 */
	public void setPageSize(Dimension2D size) {
		pageSize = size;
	}

	public Dimension2D getPageSize() {
		if (getPageCount() > 0) {
			Rectangle2D rect = getPageRect(0);

			return new Dimension2D(rect.getWidth(), rect.getHeight());
		}
		return pageSize;
	}

	/**
	 * Get the media box of the specified page.
	 *
	 * @param pageIndex The index of the page.
	 *
	 * @return The media box of the specified page.
	 */
	public Rectangle2D getPageRect(int pageIndex) {
		return pdfDocument.getPageMediaBox(pageIndex);
	}

	/**
	 * Get the page text of the specified page.
	 *
	 * @param pageIndex The index of the page.
	 *
	 * @return The page text of the specified page.
	 */
	public String getPageText(int pageIndex) {
		return pdfDocument.getPageText(pageIndex);
	}

	/**
	 * Get the text positions of the specified page.
	 *
	 * @param pageIndex The index of the page.
	 *
	 * @return A list of the text positions.
	 */
	public List<Rectangle2D> getTextPositions(int pageIndex) {
		return pdfDocument.getNormalizedWordPositions(pageIndex);
	}

	/**
	 * Get the file.
	 *
	 * @return The file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Get the path of the file.
	 *
	 * @return The file of the path. (Could return {@code null} if {@link #file}
	 * equals {@code null})
	 */
	public String getFilePath() {
		String path = null;

		if (file != null) {
			path = file.getPath();
		}

		return path;
	}

	/**
	 * Set a new title.
	 *
	 * @param title The new title.
	 */
	public void setTitle(String title) {
		pdfDocument.setTitle(title);
		this.title = title;
	}

	/**
	 * Get the title.
	 *
	 * @return The title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set a new author.
	 *
	 * @param author The new author.
	 */
	public void setAuthor(String author) {
		pdfDocument.setAuthor(author);
	}

	/**
	 * Get the author.
	 *
	 * @return The author.
	 */
	public String getAuthor() {
		return pdfDocument.getAuthor();
	}

	/**
	 * Either the filename is returned, if this document is backed by a file, or
	 * the document title is returned.
	 *
	 * @return The name of this document.
	 */
	public String getName() {
		String name;

    	if (nonNull(file)) {
    		name = file.getName();

    		// Strip file extension.
    		name = FileUtils.stripExtension(name);
    	}
    	else {
    		name = getTitle();
    	}

        return name;
	}

	/**
	 * Returns the page at the given index.
	 *
	 * @param index The page index within the document.
	 *
	 * @return The page at the given index, or null if there is no page at the
	 * given index.
	 */
	public Page getPage(int index) {
		if (index < 0 || index > getPageCount() - 1) {
			return null;
		}

		return pages.get(index);
	}

	/**
	 * Returns a list of all pages in this document.
	 *
	 * @return A list of all pages in this document.
	 */
	public List<Page> getPages() {
		return new ArrayList<>(pages);
	}

	/**
	 * Removes the given page if it is part of this document.
	 *
	 * @param page The page to remove.
	 */
	public boolean removePage(Page page) {
		int pageNumber = getPageIndex(page);
		boolean isSelected = pageNumber == currentPageNumber;

		if (pages.remove(page)) {
			pdfDocument.removePage(pageNumber);

			if (isSelected) {
				currentPageNumber = -1;
			}
			if (isWhiteboard()) {
				// To be removed whe get/setPageNumber() are removed from the Page object.
				// Re-align page numbers.
				for (int i = 0; i < pages.size(); i++) {
					pages.get(i).setPageNumber(i);
				}
			}

			fireRemoveChange(page);

			return true;
		}

		return false;
	}

	/**
	 * Returns the number of pages in this document.
	 *
	 * @return The number of pages in this document.
	 */
	public int getPageCount() {
		return pages.size();
	}

	/**
	 * Returns the page number of the current page in this document.
	 *
	 * @return The page number of the current page in this document.
	 */
	public int getCurrentPageNumber() {
		return currentPageNumber;
	}

	/**
	 * Sets the current page number.
	 *
	 * @param pageNumber The new current page number.
	 *
	 * @return {@code false} if the specified page index is smaller than zero,
	 * bigger than the highest page index or equal to the current page index,
	 * otherwise {@code true}.
	 */
	public boolean selectPage(int pageNumber) {
		if (pageNumber < 0 || pageNumber > getPageCount() - 1
				|| this.currentPageNumber == pageNumber) {
			return false;
		}

		this.currentPageNumber = pageNumber;

		return true;
	}

	/**
	 * Select the specified {@link Page} as the current page.
	 *
	 * @param page The page to be set as the current page.
	 *
	 * @return {@code false} if the index of the specified page is smaller than
	 * zero, bigger than the highest page index or equal to the current page
	 * index, otherwise {@code true}.
	 */
	public boolean selectPage(Page page) {
		return selectPage(getPageIndex(page));
	}

	/**
	 * Creates a new {@link Page} and places it at the tail of {@link #pages}.
	 *
	 * @return The new page.
	 */
	public Page createPage() {
		int pageIndex = pdfDocument.createPage(getPageSize());

		Page newPage = new Page(this, pageIndex);
		addPage(newPage);

		return newPage;
	}

	public Page createPage(Page page) throws IOException {
		return importPage(page, null);
	}

	public Page createPage(Page page, Rectangle2D pageRect) throws IOException {
		return importPage(page, pageRect);
	}

	/**
	 * Set the type of the document.
	 *
	 * @param type The new type of the document.
	 */
	public void setDocumentType(DocumentType type) {
		this.type = type;
	}

	/**
	 * Get the type of the document.
	 *
	 * @return The document type.
	 */
	public DocumentType getType() {
		return type;
	}

	/**
	 * Returns whether the document is a loaded PDF.
	 *
	 * @return {@code true} if {@link #type} equals {@code DocumentType.PDF}.
	 */
	public boolean isPDF() {
		return type == DocumentType.PDF;
	}

	/**
	 * Returns whether the document is a whiteboard.
	 *
	 * @return {@code true} if {@link #type} equals {@code DocumentType.WHITEBOARD}.
	 */
	public boolean isWhiteboard() {
		return type == DocumentType.WHITEBOARD;
	}

	/**
	 * Returns whether the document is a quiz.
	 *
	 * @return {@code true} if {@link #type} equals {@code DocumentType.QUIZ}.
	 */
	public boolean isQuiz() {
		return type == DocumentType.QUIZ;
	}

	/**
	 * Returns whether the document is a message.
	 *
	 * @return {@code true} if {@link #type} equals {@code DocumentType.MESSAGE}.
	 */
	public boolean isMessage() {
		return type == DocumentType.MESSAGE;
	}

	/**
	 * Returns whether the document consists of screen frames.
	 *
	 * @return {@code true} if {@link #type} equals {@code DocumentType.SCREEN}.
	 */
	public boolean isScreen() {
		return type == DocumentType.SCREEN;
	}

	/**
	 * Returns the index of the given page or {@code -1} if the page isn't part
	 * of this document.
	 *
	 * @param page The page from which the index should be determined.
	 *
	 * @return The index of the given page or {@code -1} if the page isn't part
	 * of this document.
	 */
	public int getPageIndex(Page page) {
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i) == page) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the current page.
	 *
	 * @return The current page.
	 */
	public Page getCurrentPage() {
		return getPage(currentPageNumber);
	}

	/**
	 * Get the PDF document.
	 *
	 * @return The PDF document.
	 */
	public PdfDocument getPdfDocument() {
		return pdfDocument;
	}

	/**
	 * Converts the PDF document to the specified output stream.
	 *
	 * @param stream The output stream.
	 */
	public void toOutputStream(OutputStream stream) throws IOException {
		if (pdfDocument != null) {
			pdfDocument.toOutputStream(stream);
			stream.flush();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((type == null) ? 0 : type.name().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Document other = (Document) o;

		boolean eqTitle = Objects.equals(getName(), other.getName());
		boolean eqType = type == other.type;
		boolean eqPdf = Objects.equals(pdfDocument, other.pdfDocument);

		return eqTitle && eqType && eqPdf;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Adds a new page to this document. The page is placed at the tail of
	 * {@link #pages}.
	 *
	 * @param page The new page.
	 */
	private void addPage(Page page) {
		pages.add(page);

		fireAddChange(page);
	}

	private synchronized Page importPage(Page page, Rectangle2D pageRect)
			throws IOException {
		PdfDocument pagePdfDocument = page.getDocument().getPdfDocument();
		int pageIndex = pdfDocument.importPage(pagePdfDocument,
				page.getPageNumber(), pageRect);

		if (pageIndex == -1) {
			return null;
		}

		Page newPage = new Page(this, pageIndex);
		addPage(newPage);

		return newPage;
	}

	/**
	 * Replaces the specified page with the new one.
	 *
	 * @param page    The page to be replaced.
	 * @param newPage The new page.
	 */
	public void replacePage(Page page, Page newPage) {
		PdfDocument newPdfDocument = newPage.getDocument().getPdfDocument();
		int docIndex = newPage.getPageNumber();

		pdfDocument.replacePage(page.getPageNumber(), newPdfDocument, docIndex);
	}

	/**
	 * Replaces all pages from the current document with the ones from the new document.
	 *
	 * @param newDocument The document containing all new Pages.
	 */
	public void replaceAllPages(Document newDocument) {
		PdfDocument newPdfDocument = newDocument.getPdfDocument();

		for (int pageIndex = 0; pageIndex < pdfDocument.getPageCount(); pageIndex++) {
			pdfDocument.replacePage(pageIndex, newPdfDocument, pageIndex);
		}
	}

	private void fireAddChange(Page page) {
		for (DocumentChangeListener listener : changeListeners) {
			listener.pageAdded(page);
		}
	}

	private void fireRemoveChange(Page page) {
		for (DocumentChangeListener listener : changeListeners) {
			listener.pageRemoved(page);
		}
	}

	/**
	 * Initializes the document.
	 *
	 * @param pdfDocument The PDF document of the document.
	 */
	protected void init(PdfDocument pdfDocument) {
		this.pdfDocument = pdfDocument;
		this.title = pdfDocument.getTitle();

		if (title == null && file != null) {
			// Use filename as title.
			title = file.getName();
		}

		setDocumentType(DocumentType.PDF);

		loadPages();
	}

	private void loadPages() {
		pages.clear();

		int pageCount = pdfDocument.getPageCount();

		for (int number = 0; number < pageCount; number++) {
			Page page = new Page(this, number);

			// Add embedded shapes.
			List<Shape> shapes = pdfDocument.getEditableShapes(number);
			if (shapes != null) {
				for (Shape shape : shapes) {
					page.addShape(shape);
				}
			}

			pages.add(page);
		}

		if (currentPageNumber > pageCount - 1) {
			currentPageNumber = 0;
		}
	}

}
