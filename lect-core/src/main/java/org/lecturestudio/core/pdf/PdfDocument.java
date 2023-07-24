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

package org.lecturestudio.core.pdf;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.pdf.mupdf.MuPDFDocument;
import org.lecturestudio.core.pdf.pdfbox.PDFBoxDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PdfDocument {

	private static final Logger LOG = LogManager.getLogger(PdfDocument.class);

	public static final String EMBEDDED_SHAPES_KEY = "PresenterShapes";

	private final PDFBoxDocument pdfBoxDocument;

	private final MuPDFDocument muPDFDocument;

	private Map<Integer, List<Shape>> shapes;


	/**
	 * Create a new {@link PdfDocument}
	 */
	public PdfDocument() {
		pdfBoxDocument = new PDFBoxDocument();
		muPDFDocument = new MuPDFDocument();

		init();
	}

	/**
	 * Create a new {@link PdfDocument} with the specified file.
	 *
	 * @param file The file.
	 */
	public PdfDocument(File file) throws IOException {
		pdfBoxDocument = new PDFBoxDocument(file);
		muPDFDocument = new MuPDFDocument(file);

		init();
	}

	/**
	 * Create a new {@link PdfDocument} with the specified byte array.
	 *
	 * @param byteArray The byte array.
	 */
	public PdfDocument(byte[] byteArray) throws IOException {
		pdfBoxDocument = new PDFBoxDocument(byteArray);
        muPDFDocument = new MuPDFDocument(byteArray);

		init();
	}

	/**
	 * Closes the document.
	 */
	public void close() throws IOException {
		pdfBoxDocument.close();
		muPDFDocument.close();
	}

	/**
	 * Get the document outline.
	 *
	 * @return The document outline.
	 */
	public DocumentOutline getDocumentOutline() {
		return muPDFDocument.getDocumentOutline();
	}

	/**
	 * Get the document renderer.
	 *
	 * @return The document renderer.
	 */
	public DocumentRenderer getDocumentRenderer() {
		return muPDFDocument.getDocumentRenderer();
	}

	/**
	 * Create a new page at the end of the document.
	 * The page size will be the size of the first page in the document.
	 * If the document is empty the default page size is width = 640 and height = 460.
	 *
	 * @return The page index of the new page.
	 */
	public int createPage() {
		// Default page size is the size of the first page in the document.
		Rectangle2D pageRect;

		if (getPageCount() > 0) {
			pageRect = getPageMediaBox(0);
		}
		else {
			pageRect = new Rectangle2D(0, 0, 640, 460);
		}

		pdfBoxDocument.addPage((int) pageRect.getWidth(), (int) pageRect.getHeight());
		muPDFDocument.addPage((int) pageRect.getWidth(), (int) pageRect.getHeight());

		return getPageCount() - 1;
	}

	/**
	 * Create a new page with the specified size at the end of the document.
	 *
	 * @param size The size of the new page.
	 *
	 * @return The page index of the new page.
	 */
	public int createPage(Dimension2D size) {
		pdfBoxDocument.addPage((int) size.getWidth(), (int) size.getHeight());
		muPDFDocument.addPage((int) size.getWidth(), (int) size.getHeight());

		return getPageCount() - 1;
	}

	/**
	 * Delete the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 */
	public void removePage(int pageNumber) {
		pdfBoxDocument.deletePage(pageNumber);
		muPDFDocument.deletePage(pageNumber);
	}

	/**
	 * Get the bounds of the page that has the specified page index.
	 *
	 * @param pageIndex The page index.
	 *
	 * @return The bounds of the page that has the specified page index.
	 */
	public Rectangle2D getPageMediaBox(int pageIndex) {
		return pdfBoxDocument.getPageBounds(pageIndex);
	}

	public void setPageContentTransform(int pageIndex, AffineTransform transform) throws IOException {
		pdfBoxDocument.setPageContentTransform(pdfBoxDocument, pageIndex, transform);
	}

	public int importPage(PdfDocument pdfDocument, int pageIndex, Rectangle2D pageRect) throws IOException {
		muPDFDocument.importPage(pdfDocument.muPDFDocument, pageIndex);

		return pdfBoxDocument.importPage(pdfDocument.pdfBoxDocument, pageIndex, pageRect);
	}

	/**
	 * Replaces the page that has the {@code pageIndex} with the page
	 * that has {@code docIndex} in {@code newPdfDocument}.
	 *
	 * @param pageIndex      The page index of the page to replace.
	 * @param newPdfDocument The {@link PdfDocument} that contains the new page.
	 * @param docIndex       The index of the new page in {@code newPdfDocument}.
	 */
	public void replacePage(int pageIndex, PdfDocument newPdfDocument, int docIndex) {
		pdfBoxDocument.replacePage(pageIndex, newPdfDocument.pdfBoxDocument, docIndex);
	}

	public void createEditableAnnotationStream(int pageIndex, List<Shape> shapes) throws IOException {
		pdfBoxDocument.setEditableAnnotations(pageIndex, shapes);
	}

	/**
	 * Create a {@link Graphics2D} object with the specified page index.
	 * The PDF graphics stream name will be set to {@code null}.
	 * The content will overwrite the existing one.
	 *
	 * @param pageIndex The index of the page to which to draw.
	 *
	 * @return The newly created {@link Graphics2D} object.
	 */
	public Graphics2D createPageGraphics2D(int pageIndex) {
		return pdfBoxDocument.createGraphics(pageIndex, null, false);
	}

	/**
	 * Create a {@link Graphics2D} object with the specified page index.
	 * The PDF graphics stream name will be set to {@code null}.
	 * The content will be appended to the existing one.
	 *
	 * @param pageIndex The index of the page to which to draw.
	 *
	 * @return The newly created {@link Graphics2D} object.
	 */
	public Graphics2D createAppendablePageGraphics2D(int pageIndex) {
		return pdfBoxDocument.createGraphics(pageIndex, null, true);
	}

	/**
	 * Create a {@link Graphics2D} object with the specified page index and PDF graphics stream name.
	 * The content will be appended to the existing one.
	 *
	 * @param pageIndex The index of the page to which to draw.
	 * @param name The PDF graphics stream name.
	 *
	 * @return The newly created {@link Graphics2D} object.
	 */
	public Graphics2D createAppendablePageGraphics2D(int pageIndex, String name) {
		return pdfBoxDocument.createGraphics(pageIndex, name, true);
	}

	/**
	 * Set the title of the document.
	 *
	 * @param title The title.
	 */
	public void setTitle(String title) {
		pdfBoxDocument.setTitle(title);
	}

	/**
	 * Get the title of the document.
	 *
	 * @return The title.
	 */
	public String getTitle() {
		return pdfBoxDocument.getTitle();
	}

	/**
	 * Set the author of the document.
	 *
	 * @param author The author.
	 */
	public void setAuthor(String author) {
		pdfBoxDocument.setAuthor(author);
	}

	/**
	 * Get the author of the document.
	 *
	 * @return The author.
	 */
	public String getAuthor() {
		return pdfBoxDocument.getAuthor();
	}

	/**
	 * Get the number of pages in the document.
	 *
	 * @return The number of pages in the document.
	 */
	public int getPageCount() {
		return muPDFDocument.getPageCount();
	}

	/**
	 * Get the text of the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The text of the page that has the specified page number.
	 */
	public String getPageText(int pageNumber) {
		return muPDFDocument.getPageText(pageNumber);
	}

	/**
	 * Get the word bounds of the page that has the specified page number.
	 *
	 * @param pageNumber The page number.
	 * @return The word bounds of the page that has the specified page number.
	 */
	public List<Rectangle2D> getNormalizedWordPositions(int pageNumber) {
		return muPDFDocument.getPageWordsNormalized(pageNumber);
	}

	/**
	 * Get the text bounds of the page that has the specified page index.
	 *
	 * @param pageIndex The page index.
	 *
	 * @return The text bounds of the page that has the specified page index.
	 */
	public Rectangle2D getTextBounds(int pageIndex) throws IOException {
		return pdfBoxDocument.getPageTextBounds(pageIndex);
	}
	
	/**
	 * Get the editable shapes of the page that has the specified page index.
	 *
	 * @param pageIndex The page index.
	 *
	 * @return The editable shapes of the page that has the specified page index.
	 */
	public List<Shape> getEditableShapes(int pageIndex) {
		return shapes.get(pageIndex);
	}

	/**
	 * Save the document to the specified {@link OutputStream}.
	 *
	 * @param stream The {@link OutputStream} to write to.
	 */
	public void toOutputStream(OutputStream stream) throws IOException {
		pdfBoxDocument.toOutputStream(stream);
	}

	private void init() {
		try {
			shapes = pdfBoxDocument.removeEditableAnnotations();
		}
		catch (IOException e) {
			LOG.error("Load editable annotations failed", e);

			shapes = new HashMap<>();
		}
	}
	/**
	 * Get the PDFBox Document.
	 *
	 * @return The PDFBox Document
	 */

	public PDFBoxDocument getPdfBoxDocument() {
		return pdfBoxDocument;
	}
}
