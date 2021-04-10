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

package org.lecturestudio.core.pdf.pdfbox;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionLaunch;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.io.BitConverter;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.pdf.DocumentAdapter;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.pdf.PdfDocument;

public class PDFBoxDocument implements DocumentAdapter {

	private final PDFBoxRenderer renderer;

	public final PDDocument doc;

	private DocumentOutline outline;


	public PDFBoxDocument() {
		this(new PDDocument());
	}

	public PDFBoxDocument(File file) throws IOException {
		this(PDDocument.load(file));
	}

	public PDFBoxDocument(byte[] byteArray) throws IOException {
		this(PDDocument.load(byteArray));
	}

	private PDFBoxDocument(PDDocument pdDoc) {
		doc = pdDoc;
		renderer = new PDFBoxRenderer(doc);
	}

	@Override
	public void close() throws IOException {
		doc.close();
	}

	@Override
	public DocumentOutline getDocumentOutline() {
		if (isNull(outline)) {
			outline = new DocumentOutline();

			PDDocumentCatalog pdCatalog = doc.getDocumentCatalog();
			PDDocumentOutline pdOutline = pdCatalog.getDocumentOutline();

			PDOutlineItem lastItem = null;

			for (PDOutlineItem item : pdOutline.children()) {
				if (nonNull(lastItem) && lastItem.getTitle().equals(item.getTitle())) {
					// Skip duplicate item, even if the page number is different.
					continue;
				}

				loadOutline(pdCatalog, item, outline);

				lastItem = item;
			}
		}

		return outline;
	}

	@Override
	public DocumentRenderer getDocumentRenderer() {
		return renderer;
	}

	@Override
	public PDFGraphics2D createGraphics(int pageIndex, String name, boolean appendContent) {
		PDPage pdPage = doc.getPage(pageIndex);

		return new PDFGraphics2D(doc, pdPage, name, appendContent);
	}

	@Override
	public void setTitle(String title) {
		doc.getDocumentInformation().setTitle(title);
	}

	@Override
	public String getTitle() {
		return doc.getDocumentInformation().getTitle();
	}

	@Override
	public void setAuthor(String author) {
		doc.getDocumentInformation().setAuthor(author);
	}

	@Override
	public String getAuthor() {
		return doc.getDocumentInformation().getAuthor();
	}

	@Override
	public Rectangle2D getPageBounds(int pageNumber) {
		PDPage page = doc.getPage(pageNumber);
		PDRectangle rect = page.getMediaBox();

		return new Rectangle2D(0, 0, rect.getWidth(), rect.getHeight());
	}

	@Override
	public int getPageCount() {
		return doc.getNumberOfPages();
	}

	@Override
	public String getPageText(int pageNumber) throws IOException {
		TextExtractor textExtractor = new TextExtractor(doc);

		return textExtractor.getText(pageNumber + 1);
	}

	@Override
	public List<Rectangle2D> getPageWordsNormalized(int pageNumber) throws IOException {
		WordBoundsExtractor wordExtractor = new WordBoundsExtractor(doc);

		return wordExtractor.getWordBounds(pageNumber + 1);
	}

	@Override
	public Rectangle2D getPageTextBounds(int pageNumber) throws IOException {
		TextBoundsExtractor textBoundsExtractor = new TextBoundsExtractor(doc);

		return textBoundsExtractor.getTextBounds(pageNumber + 1);
	}

	@Override
	public Set<URI> getLinks(int pageNumber) {
		Set<URI> uris = new HashSet<>();

		PDPage page = doc.getPage(pageNumber);
		List<PDAnnotation> annotations;

		try {
			annotations = page.getAnnotations(annotation -> PDAnnotationLink.SUB_TYPE.equals(annotation.getSubtype()));
		}
		catch (IOException e) {
			e.printStackTrace();
			return uris;
		}

		for (PDAnnotation annotation : annotations) {
			PDAnnotationLink link = (PDAnnotationLink) annotation;
			PDAction action = link.getAction();

			if (action == null) {
				continue;
			}

			if (PDActionURI.SUB_TYPE.equals(action.getSubType())) {
				PDActionURI pdUri = (PDActionURI) action;
				String uriStr = pdUri.getURI().toLowerCase();
				uriStr = uriStr.replaceAll("/$", "");

				uris.add(URI.create(uriStr));
			}
		}

		return uris;
	}

	@Override
	public Set<File> getLaunchActions(int pageNumber) {
		Set<File> launchActions = new HashSet<>();

		PDPage page = doc.getPage(pageNumber);
		List<PDAnnotation> annotations;

		try {
			annotations = page.getAnnotations(annotation -> PDAnnotationLink.SUB_TYPE.equals(annotation.getSubtype()));
		}
		catch (IOException e) {
			e.printStackTrace();
			return launchActions;
		}

		for (PDAnnotation annotation : annotations) {
			PDAnnotationLink link = (PDAnnotationLink) annotation;
			PDAction action = link.getAction();

			if (action == null) {
				continue;
			}

			if (PDActionLaunch.SUB_TYPE.equals(action.getSubType())) {
				PDActionLaunch pdLaunch = (PDActionLaunch) action;
				PDFileSpecification fileSpec = null;

				try {
					fileSpec = pdLaunch.getFile();
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				if (fileSpec == null || fileSpec.getFile() == null) {
					continue;
				}

				launchActions.add(new File(fileSpec.getFile()));
			}
		}

		return launchActions;
	}

	@Override
	public void addPage(int width, int height) {
		doc.addPage(new PDPage(new PDRectangle(width, height)));
	}

	@Override
	public void deletePage(int pageNumber) {
		doc.removePage(pageNumber);
	}

	@Override
	public int importPage(DocumentAdapter srcDocument, int pageNumber) throws IOException {
		PDFBoxDocument pdfBoxSrcDoc = (PDFBoxDocument) srcDocument;

		return importPage(pdfBoxSrcDoc, pageNumber, new AffineTransform());
	}

	public int importPage(PDFBoxDocument srcDocument, int pageNumber, AffineTransform transform) throws IOException {
		PDDocument sourceDocument = srcDocument.doc;
		PDPage page = sourceDocument.getPage(pageNumber);
		PDPage imported = doc.importPage(page);

		imported.setResources(page.getResources());

		if (page.getRotation() == 90) {
			// Set rotation to zero.
			float mh = page.getMediaBox().getHeight();
			float mw = page.getMediaBox().getWidth();
			imported.setMediaBox(new PDRectangle(mh, mw));
			imported.setCropBox(new PDRectangle(mh, mw));
			imported.setRotation(0);
		}

		List<PDStream> newContents = new ArrayList<>();

		float w = imported.getMediaBox().getWidth();
		float h = imported.getMediaBox().getHeight();
		float sx = (float) transform.getScaleX();
		float sy = (float) transform.getScaleY();
		// Translate by taking the scaled size into account.
		float tx = (float) (transform.getTranslateX() * w * -sx);
		// Move y-position from bottom-left to top-left origin and translate.
		float ty = (float) (h - (h * sy) + transform.getTranslateY() * h * sy);

		// Create page transformation content stream.
		PDStream transformStream = new PDStream(sourceDocument);
		OutputStream transformOutStream = transformStream.createOutputStream(COSName.FLATE_DECODE);
		ContentStreamWriter writer = new ContentStreamWriter(transformOutStream);
		writer.writeToken(new COSFloat(sx));
		writer.writeToken(COSInteger.ZERO);
		writer.writeToken(COSInteger.ZERO);
		writer.writeToken(new COSFloat(sy));
		writer.writeToken(new COSFloat(tx));
		writer.writeToken(new COSFloat(ty));
		writer.writeToken(Operator.getOperator("cm"));
		transformOutStream.flush();
		transformOutStream.close();

		newContents.add(transformStream);

		if (page.hasContents()) {
			PDFStreamParser parser = new PDFStreamParser(page);
			parser.parse();

			PDStream contentStream = new PDStream(sourceDocument);
			OutputStream contentOutStream = contentStream.createOutputStream(COSName.FLATE_DECODE);
			ContentStreamWriter contentWriter = new ContentStreamWriter(contentOutStream);

			contentWriter.writeToken(Operator.getOperator("q"));

			if (page.getRotation() == 90) {
				// Set rotation to zero.
				AffineTransform t = new AffineTransform();
				t.rotate(-Math.PI / 2);
				t.translate(-page.getMediaBox().getWidth(), 0);
				Matrix m = new Matrix(t);

				contentWriter.writeToken(new COSFloat(m.getScaleX()));
				contentWriter.writeToken(new COSFloat(m.getShearY()));
				contentWriter.writeToken(new COSFloat(m.getShearX()));
				contentWriter.writeToken(new COSFloat(m.getScaleY()));
				contentWriter.writeToken(new COSFloat(m.getTranslateX()));
				contentWriter.writeToken(new COSFloat(m.getTranslateY()));
				contentWriter.writeToken(Operator.getOperator("cm"));
			}

			contentWriter.writeTokens(parser.getTokens());
			contentWriter.writeToken(Operator.getOperator("Q"));
			contentOutStream.flush();
			contentOutStream.close();

			newContents.add(contentStream);
		}

		imported.setContents(newContents);

		return getPageCount() - 1;
	}

	@Override
	public void toOutputStream(OutputStream stream) throws IOException {
		doc.save(stream);
	}

	@Override
	public void setEditableAnnotations(int pageIndex, List<Shape> shapes) throws IOException {
		PDPage pdPage = doc.getPage(pageIndex);
		COSDictionary pageDict = pdPage.getCOSObject();
		COSArray shapesContent = (COSArray) pageDict.getDictionaryObject(PdfDocument.EMBEDDED_SHAPES_KEY);
		PDStream pdStream = new PDStream(doc);

		if (shapesContent == null) {
			shapesContent = new COSArray();
		}

		shapesContent.add(pdStream);

		OutputStream outStream = pdStream.createOutputStream(COSName.FLATE_DECODE);

		for (Shape shape : shapes) {
			// Write shape class.
			String cls = shape.getClass().getCanonicalName();
			byte[] data = cls.getBytes();
			outStream.write(BitConverter.getBigEndianBytes(data.length));
			outStream.write(data);

			// Write shape as binary data.
			data = shape.toByteArray();
			outStream.write(BitConverter.getBigEndianBytes(data.length));
			outStream.write(data);
		}

		outStream.flush();
		outStream.close();

		pageDict.setItem(PdfDocument.EMBEDDED_SHAPES_KEY, shapesContent);
	}

	@Override
	public List<Shape> getEditableAnnotations(int pageIndex) throws IOException {
		List<Shape> shapes = new ArrayList<>();

		PDPage pdPage = doc.getPage(pageIndex);
		COSDictionary pageDict = pdPage.getCOSObject();
		COSBase base = pageDict.getDictionaryObject(PdfDocument.EMBEDDED_SHAPES_KEY);

		// Page contents should be an array.
		if (base instanceof COSArray) {
			COSArray contents = (COSArray) base;

			// Parse each content stream.
			for (COSBase content : contents) {
				COSObject obj = (COSObject) content;
				COSStream pdStream = (COSStream) obj.getObject();

				try {
					InputStream inStream = pdStream.createInputStream();
					byte[] lenBytes = new byte[4];

					while (inStream.available() > 0) {
						// Read shape class.
						inStream.read(lenBytes);
						byte[] data = new byte[BitConverter.getBigEndianInt(lenBytes)];
						inStream.read(data);

						Class<?> cls = Class.forName(new String(data));
						Constructor<?> ctor = cls.getConstructor(byte[].class);

						// Read shape data.
						inStream.read(lenBytes);
						data = new byte[BitConverter.getBigEndianInt(lenBytes)];
						inStream.read(data);

						shapes.add((Shape) ctor.newInstance(data));
					}
				}
				catch (Exception e) {
					throw new IOException("Create embedded annotation failed.", e);
				}
			}
		}

		return shapes;
	}

	/**
	 * Parse page contents and remove editable annotations for page rendering.
	 *
	 * @return The removed annotations from all pages.
	 *
	 * @throws IOException If the cleaned document cannot be created.
	 */
	@Override
	public Map<Integer, List<Shape>> removeEditableAnnotations() throws IOException {
		Map<Integer, List<Shape>> shapes = new HashMap<>();

		for (int number = 0; number < doc.getNumberOfPages(); number++) {
			PDPage pdPage = doc.getPage(number);
			COSDictionary pageDict = pdPage.getCOSObject();
			COSBase contents = pageDict.getDictionaryObject(COSName.CONTENTS);

			// Remove content streams which were tagged to be editable.
			if (contents instanceof COSStream) {
				PDStream stream = new PDStream((COSStream) contents);

				if (isEditableStream(stream)) {
					pdPage.setContents((PDStream) null);
				}
			}
			else if (contents instanceof COSArray && ((COSArray) contents).size() > 0) {
				COSArray array = (COSArray) contents;
				for (int j = 0; j < array.size(); j++) {
					PDStream stream = new PDStream((COSStream) array.getObject(j));

					if (isEditableStream(stream)) {
						array.remove(j);
					}
				}
			}

			// Store parsed annotations before removing them.
			shapes.put(number, getEditableAnnotations(number));

			// Remove binary annotation stream.
			pageDict.removeItem(COSName.getPDFName(PdfDocument.EMBEDDED_SHAPES_KEY));
		}

		return shapes;
	}

	private void loadOutline(PDDocumentCatalog catalog, PDOutlineItem item,
			DocumentOutlineItem outline) {
		if (isNull(item)) {
			return;
		}

		Integer pageNumber = null;

		try {
			PDPage page = item.findDestinationPage(doc);
			pageNumber = catalog.getPages().indexOf(page);
		}
		catch (IOException e) {
			// Ignore.
		}

		// Remove line tabulation character from the title string.
		String title = item.getTitle();
		int ltIndex;

		while ((ltIndex = title.indexOf("\u000B")) > -1) {
			// Do not replace the line tabulation with a space separator if
			// a space separator precedes the line tabulation.
			int prevIndex = ltIndex - 1 > -1 ? ltIndex - 1 : 0;
			String rep = title.charAt(prevIndex) == ' ' ? "" : " ";

			title = title.replace("\u000B", rep);
		}

		DocumentOutlineItem outlineItem = new DocumentOutlineItem(title, pageNumber);
		outline.getChildren().add(outlineItem);

		if (item.hasChildren()) {
			PDOutlineItem lastItem = null;

			for (PDOutlineItem child : item.children()) {
				if (nonNull(lastItem) && lastItem.getTitle().equals(item.getTitle())) {
					// Skip duplicate item.
					continue;
				}

				loadOutline(catalog, child, outlineItem);

				lastItem = child;
			}
		}
	}

	private boolean isEditableStream(PDStream stream) {
		String name = stream.getCOSObject().getNameAsString(COSName.NAME);
		return name != null && name.equals(PdfDocument.EMBEDDED_SHAPES_KEY);
	}
}