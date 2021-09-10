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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.util.FileUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 * This class represents a document. Documents consist of a list Pages and can
 * be Whiteboard-Documents, Screen-Capture-Documents or normal Documents. Documents have a handle so that
 * the PDF-API can associate its specific information with the document.
 * 
 * @author Alex Andres
 * @author Maximilian Felix Ratzke
 */
public class Document {

	private static final Logger LOG = LogManager.getLogger(Document.class);
	
	private final List<DocumentChangeListener> changeListeners = new ArrayList<>();

	private final List<Page> pages = new ArrayList<>();

	/** The opened document file. May be null, if this document is not loaded from a file. */
	private File file;
	
	private DocumentType type;
	private Dimension2D pageSize;

	/** The PDF document. */
	private PdfDocument pdfDocument;

	/** The title of the PDF document. */
	private String title;
	
	private int currentPageNumber = 0;
	

	public Document() throws IOException {
		this(new PdfDocument());
	}
	
	public Document(File file) throws IOException {
		this(new PdfDocument(file));
		this.file = file;
	}

	public Document(PdfDocument pdfDocument) {
		initPDFDocument(pdfDocument);
		setPageSize(new Dimension2D(640, 480));
	}

	public Document(byte[] bytes) throws IOException {
		this(new PdfDocument(bytes));
	}

	public DocumentOutline getDocumentOutline() {
		return pdfDocument.getDocumentOutline();
	}

	public DocumentRenderer getDocumentRenderer() {
		return pdfDocument.getDocumentRenderer();
	}

	public void addChangeListener(DocumentChangeListener listener) {
		if (!changeListeners.contains(listener)) {
			changeListeners.add(listener);
		}
	}

	public void removeChangeListener(DocumentChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	public void close() {
		closeDocument();

		pages.clear();
	}
	
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
	
	public void setPdfDocument(PdfDocument pdfDocument) {
		close();
		initPDFDocument(pdfDocument);
		fireDocumentChange();
	}

	public void setPageSize(Dimension2D size) {
		pageSize = size;
	}

	public Rectangle2D getPageRect(int pageIndex) {
		return pdfDocument.getPageMediaBox(pageIndex);
	}

	public String getPageText(int pageIndex) {
		return pdfDocument.getPageText(pageIndex);
	}
	
	public List<Rectangle2D> getTextPositions(int pageIndex) {
		try {
			return pdfDocument.getNormalizedWordPositions(pageIndex);
		}
		catch (IOException e) {
			LOG.error("Get text words failed.", e);
			return null;
		}
	}
	
	public List<URI> getUriActions(int pageIndex) {
		List<URI> actions = new ArrayList<>();

		if (isPDF()) {
			try {
				actions = pdfDocument.getUriActions(pageIndex);
			}
			catch (IOException e) {
				LOG.error("Get URI actions failed.", e);
			}
		}
		
		return actions;
	}
	
	public List<File> getLaunchActions(int pageIndex) {
		List<File> files = new ArrayList<>();

		if (isPDF()) {
			try {
				files = pdfDocument.getLaunchActions(pageIndex);
			}
			catch (IOException e) {
				LOG.error("Get launch actions failed.", e);
			}
		}
		
		return files;
	}

	public File getFile() {
		return file;
	}
	
    public String getFilePath() {
    	String path = null;
    	
    	if (file != null) {
    		path = file.getPath();
    	}
    	
        return path;
    }

	public void setTitle(String title) {
		if (pdfDocument != null) {
			pdfDocument.setTitle(title);
		}
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setAuthor(String author) {
		if (pdfDocument != null) {
			pdfDocument.setAuthor(author);
		}
	}
	
	public String getAuthor() {
		return pdfDocument != null ? pdfDocument.getAuthor() : "";
	}

	/**
	 * Either the filename is returned, if this document is backed by a file, or
	 * the document title is returned.
	 *
	 * @return the name of this document.
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
	 * Returns the page at the given index
	 * 
	 * @param index the page index within the document.
	 *
	 * @return the page at the given index, or null if there is no page
	 * at the given index.
	 */
	public Page getPage(int index) {
		if (index < 0 || index >= getPageCount())
			return null;

		return pages.get(index);
	}

	/**
	 * Returns a list of all pages in this document
	 */
	public List<Page> getPages() {
		return new ArrayList<>(pages);
	}

	/**
	 * Removes the given page if it is part of this document
	 */
	public boolean removePage(Page page) {
		int pageNumber = getPageIndex(page);
		boolean isSelected = pageNumber == currentPageNumber;

		if (pages.remove(page)) {
			pdfDocument.removePage(pageNumber);

			if (isSelected) {
				currentPageNumber = -1;
			}

			fireRemoveChange(page);

			return true;
		}

		return false;
	}

	/**
	 * Adds a set of Pages
	 */
	public void addPages(int index, Collection<? extends Page> collection) {
		pages.addAll(index, collection);
	}

	/**
	 * Returns the number of pages in this document
	 */
	public int getPageCount() {
		return pages.size();
	}

	/**
	 * Returns the page number of the current page in this document
	 */
	public int getCurrentPageNumber() {
		return currentPageNumber;
	}

	/**
	 * Sets the current page number
	 */
	public boolean selectPage(int pageNumber) {
		if (pageNumber < 0 || pageNumber > getPageCount() - 1) {
			return false;
		}
		if (this.currentPageNumber == pageNumber) {
			return false;
		}
		
		this.currentPageNumber = pageNumber;

		return true;
	}
	
	public boolean selectPage(Page page) {
		return selectPage(getPageIndex(page));
	}

	public Page createPage() {
		int pageIndex = pdfDocument.createPage();

		Page newPage = new Page(this, pageIndex);
		addPage(newPage);
		
		return newPage;
	}

	public Page createPage(BufferedImage image) {
		int pageIndex = pdfDocument.createPage(image);

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

	public void setDocumentType(DocumentType type) {
		this.type = type;
	}
	
	public DocumentType getType() {
		return type;
	}
	
	public boolean isPDF() {
		return type == DocumentType.PDF;
	}
	
	public boolean isWhiteboard() {
		return type == DocumentType.WHITEBOARD;
	}
	
	public boolean isQuiz() {
		return type == DocumentType.QUIZ;
	}

	public boolean isScreenCapture() {
		return type == DocumentType.SCREEN_CAPTURE;
	}

	/**
	 * Returns the index of the given page or -1 if the page isn't part of this
	 * document
	 */
	public int getPageIndex(Page page) {
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i) == page)
				return i;
		}
		return -1;
	}

	/**
	 * Returns the current page
	 */
	public Page getCurrentPage() {
		return getPage(currentPageNumber);
	}
	
	public PdfDocument getPdfDocument() {
		return pdfDocument;
	}

	public String getChecksum(MessageDigest digest) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		pdfDocument.toOutputStream(stream);
		stream.flush();
		
		digest.update(stream.toByteArray());
		
		byte[] bytes = digest.digest();
		char[] hex = new char[bytes.length << 1];

		final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		
		// Convert it to hexadecimal format.
		for (int i = 0, j = 0; i < bytes.length; i++) {
			hex[j++] = HEX_DIGITS[(0xF0 & bytes[i]) >>> 4];
			hex[j++] = HEX_DIGITS[0x0F & bytes[i]];
		}
		
		return new String(hex);
	}
	
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
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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

		boolean eqTitle = Objects.equals(title, other.title);
		boolean eqType = type == other.type;
		boolean eqPdf = Objects.equals(pdfDocument, other.pdfDocument);

		return eqTitle && eqType && eqPdf;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * Adds a new page to this document. The page is placed at the tail of the
	 * page list.
	 * 
	 * @param page The new page.
	 */
	protected void addPage(Page page) {
		pages.add(page);
		
		fireAddChange(page);
	}

	protected synchronized Page importPage(Page page, Rectangle2D pageRect) throws IOException {
		PdfDocument pagePdfDocument = page.getDocument().getPdfDocument();
		int pageIndex = pdfDocument.importPage(pagePdfDocument, page.getPageNumber(), pageRect);

		Page newPage = new Page(this, pageIndex);
		newPage.addShapes(page.getShapes());

		addPage(newPage);

		return newPage;
	}

	public void replacePage(Page page, Page newPage) throws IOException {
		PdfDocument newPdfDocument = newPage.getDocument().getPdfDocument();
		int docIndex = newPage.getPageNumber();

		pdfDocument.replacePage(page.getPageNumber(), newPdfDocument, docIndex);
	}

	protected void fireDocumentChange() {
		for (DocumentChangeListener listener : changeListeners) {
			listener.documentChanged(this);
		}
	}

	protected void fireAddChange(Page page) {
		for (DocumentChangeListener listener : changeListeners) {
			listener.pageAdded(page);
		}
	}

	protected void fireRemoveChange(Page page) {
		for (DocumentChangeListener listener : changeListeners) {
			listener.pageRemoved(page);
		}
	}

	private void initPDFDocument(PdfDocument pdfDocument) {
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
