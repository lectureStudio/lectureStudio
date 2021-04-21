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
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final String REGEX_HTTP = "((([A-Za-z]{3,9}:(?:\\/\\/)?)"
			+ "(?:[-;:&=\\+\\$,\\w]+@)?"
			+ "[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)"
			+ "((?:\\/[\\+~%\\/.\\w-_]*)?" + "\\??(?:[-\\+=&;%@.\\w_]*)#?"
			+ "(?:[.\\!\\/\\\\w]*))?)";

	public static final String EMBEDDED_SHAPES_KEY = "PresenterShapes";

	private final Pattern httpPattern = Pattern.compile(REGEX_HTTP);

	private final PDFBoxDocument pdfBoxDocument;

	private final MuPDFDocument muPDFDocument;

	private Map<Integer, List<Shape>> shapes;


	public PdfDocument() {
		pdfBoxDocument = new PDFBoxDocument();
		muPDFDocument = new MuPDFDocument();

		init();
	}

	public PdfDocument(File file) throws IOException {
		pdfBoxDocument = new PDFBoxDocument(file);
		muPDFDocument = new MuPDFDocument(file);

		init();
	}

	public PdfDocument(byte[] byteArray) throws IOException {
		pdfBoxDocument = new PDFBoxDocument(byteArray);
        muPDFDocument = new MuPDFDocument(byteArray);

		init();
	}

	public void close() throws IOException {
		pdfBoxDocument.close();
		muPDFDocument.close();
	}

	public DocumentOutline getDocumentOutline() {
		return muPDFDocument.getDocumentOutline();
	}

	public DocumentRenderer getDocumentRenderer() {
		return muPDFDocument.getDocumentRenderer();
	}

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

	public int createPage(Dimension2D size) {
		pdfBoxDocument.addPage((int) size.getWidth(), (int) size.getHeight());
		muPDFDocument.addPage((int) size.getWidth(), (int) size.getHeight());

		return getPageCount() - 1;
	}

	public void removePage(int pageNumber) {
		pdfBoxDocument.deletePage(pageNumber);
		muPDFDocument.deletePage(pageNumber);
	}
	
	public Rectangle2D getPageMediaBox(int pageIndex) {
		return pdfBoxDocument.getPageBounds(pageIndex);
	}
	
	public int importPage(PdfDocument pdfDocument, int pageIndex, Rectangle2D pageRect) throws IOException {
		return pdfBoxDocument.importPage(pdfDocument.pdfBoxDocument, pageIndex, pageRect);
	}

	public void replacePage(int pageIndex, PdfDocument newPdfDocument, int docIndex) throws IOException {
		pdfBoxDocument.replacePage(pageIndex, newPdfDocument.pdfBoxDocument, docIndex);
	}

	public void createEditableAnnotationStream(int pageIndex, List<Shape> shapes) throws IOException {
		pdfBoxDocument.setEditableAnnotations(pageIndex, shapes);
	}
	
	public Graphics2D createPageGraphics2D(int pageIndex) {
		return pdfBoxDocument.createGraphics(pageIndex, null, false);
	}

	public Graphics2D createAppendablePageGraphics2D(int pageIndex) {
		return pdfBoxDocument.createGraphics(pageIndex, null, true);
	}
	
	public Graphics2D createAppendablePageGraphics2D(int pageIndex, String name) {
		return pdfBoxDocument.createGraphics(pageIndex, name, true);
	}
	
	public void setTitle(String title) {
		pdfBoxDocument.setTitle(title);
	}
	
	public String getTitle() {
		return pdfBoxDocument.getTitle();
	}
	
	public void setAuthor(String author) {
		pdfBoxDocument.setAuthor(author);
	}
	
	public String getAuthor() {
		return pdfBoxDocument.getAuthor();
	}
	
	public int getPageCount() {
		return muPDFDocument.getPageCount();
	}
	
	public String getPageText(int pageNumber) {
		return muPDFDocument.getPageText(pageNumber);
	}

	public List<Rectangle2D> getNormalizedWordPositions(int pageNumber) throws IOException {
		return muPDFDocument.getPageWordsNormalized(pageNumber);
	}
	
	public Rectangle2D getTextBounds(int pageIndex) throws IOException {
		return pdfBoxDocument.getPageTextBounds(pageIndex);
	}
	
	public List<URI> getUriActions(int pageIndex) throws IOException {
		List<URI> list = new ArrayList<>(muPDFDocument.getLinks(pageIndex));

		// Extract URIs from raw page text.
		String text = getPageText(pageIndex);
		Matcher matcher = httpPattern.matcher(text);
		String uriStr;

		while (matcher.find()) {
			uriStr = matcher.group().toLowerCase();
			uriStr = uriStr.replaceAll("/$", "");

			// Some special treatment.
			if (uriStr.startsWith("www"))
				uriStr = "http://" + uriStr;
			if (uriStr.startsWith("file://"))
				uriStr = "file:///" + uriStr.substring(7);

			URI uri = URI.create(uriStr);

			if (!list.contains(uri)) {
				list.add(uri);
			}
		}
		
		return list;
	}
	
	public List<File> getLaunchActions(int pageIndex) throws IOException {
		return new ArrayList<>(muPDFDocument.getLaunchActions(pageIndex));
	}

	public List<Shape> getEditableShapes(int pageIndex) {
		return shapes.get(pageIndex);
	}

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
}