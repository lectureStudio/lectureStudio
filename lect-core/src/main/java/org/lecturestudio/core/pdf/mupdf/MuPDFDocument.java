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

package org.lecturestudio.core.pdf.mupdf;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.artifex.mupdf.fitz.Buffer;
import com.artifex.mupdf.fitz.DisplayList;
import com.artifex.mupdf.fitz.Document;
import com.artifex.mupdf.fitz.Link;
import com.artifex.mupdf.fitz.Outline;
import com.artifex.mupdf.fitz.PDFDocument;
import com.artifex.mupdf.fitz.PDFGraftMap;
import com.artifex.mupdf.fitz.PDFObject;
import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.Rect;
import com.artifex.mupdf.fitz.SeekableInputOutputStream;
import com.artifex.mupdf.fitz.StructuredText;

import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.io.BitConverter;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.pdf.DocumentAdapter;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.pdf.PdfDocument;

public class MuPDFDocument implements DocumentAdapter {

	private static final Executor EXECUTOR = Executors.newFixedThreadPool(1);

	private final Map<Integer, PageEntry> displayListMap = new ConcurrentHashMap<>();

	private final PDFDocument doc;

	private final DocumentRenderer renderer;

	private DocumentOutline outline;

	private AbstractMap.SimpleEntry<DocumentAdapter, PDFGraftMap> graftMapping;

	private final Object mutex = new Object();


	/**
	 * Create a new {@link MuPDFDocument}.
	 * (Calls {@link #MuPDFDocument(PDFDocument)} with a new {@link PDFDocument})
	 */
	public MuPDFDocument() {
		this(new PDFDocument());
	}

	/**
	 * Create a new {@link MuPDFDocument} with the specified file.
	 * (Calls {@link #MuPDFDocument(PDFDocument)})
	 *
	 * @param file The file.
	 */
	public MuPDFDocument(File file) {
		this((PDFDocument) Document.openDocument(file.getAbsolutePath()));
	}

	/**
	 * Create a new {@link MuPDFDocument} with the specified byte array.
	 * (Calls {@link #MuPDFDocument(PDFDocument)})
	 *
	 * @param byteArray The byte array.
	 */
	public MuPDFDocument(byte[] byteArray) {
		this((PDFDocument) Document.openDocument(byteArray, ""));
	}

	/**
	 * Create a new {@link MuPDFDocument} with the specified {@link PDFDocument}.
	 *
	 * @param document The {@link PDFDocument}.
	 */
	private MuPDFDocument(PDFDocument document) {
		doc = document;
		renderer = new MuPDFRenderer(this);
	}

	@Override
	public void close() {
		synchronized (mutex) {
			doc.destroy();
		}
	}

	@Override
	public DocumentOutline getDocumentOutline() {
		synchronized (mutex) {
			if (isNull(outline)) {
				outline = new DocumentOutline();

				loadOutline(doc.loadOutline(), outline);
			}
		}

		return outline;
	}

	@Override
	public DocumentRenderer getDocumentRenderer() {
		return renderer;
	}

	@Override
	public Graphics2D createGraphics(int pageIndex, String name, boolean appendContent) {
		return null;
	}

	@Override
	public void setTitle(String title) {
		//doc.setTitle(title);
	}

	@Override
	public String getTitle() {
		synchronized (mutex) {
			return doc.getMetaData(Document.META_INFO_TITLE);
		}
	}

	@Override
	public void setAuthor(String author) {
		//doc.setAuthor(author);
	}

	@Override
	public String getAuthor() {
		synchronized (mutex) {
			return doc.getMetaData(Document.META_INFO_AUTHOR);
		}
	}

	@Override
	public Rectangle2D getPageBounds(int pageNumber) {
		synchronized (mutex) {
			Page page = getPage(pageNumber);
			Rect bounds = page.getBounds();

			return new Rectangle2D(0, 0, bounds.x1 - bounds.x0, bounds.y1 - bounds.y0);
		}
	}

	@Override
	public int getPageCount() {
		synchronized (mutex) {
			return doc.countPages();
		}
	}

	@Override
	public String getPageText(int pageNumber) {
		synchronized (mutex) {
			CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
				DisplayList displayList = getDisplayList(pageNumber);
				Page page = getPage(pageNumber);

				SimpleTextWalker textWalker = new SimpleTextWalker(page.getBounds());

				StructuredText structuredText = displayList.toStructuredText();
				structuredText.walk(textWalker);

				return textWalker.getText();
			}, EXECUTOR);

			try {
				return future.get();
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public List<Rectangle2D> getPageWordsNormalized(int pageNumber) {
		synchronized (mutex) {
			DisplayList displayList = getDisplayList(pageNumber);
			Page page = getPage(pageNumber);

			WordWalker wordWalker = new WordWalker(page.getBounds());

			StructuredText structuredText = displayList.toStructuredText();
			structuredText.walk(wordWalker);

			return wordWalker.getWordBounds();
		}
	}

	@Override
	public Rectangle2D getPageTextBounds(int pageNumber) throws IOException {
		return null;
	}

	@Override
	public Set<URI> getLinks(int pageNumber) {
		synchronized (mutex) {
			Page page = getPage(pageNumber);

			Set<URI> uris = new HashSet<>();
			Link[] links = page.getLinks();

			if (nonNull(links)) {
				for (Link link : links) {
					if (nonNull(link.uri)) {
						uris.add(URI.create(link.uri));
					}
				}
			}

			return uris;
		}
	}

	@Override
	public Set<File> getLaunchActions(int pageNumber) {
		synchronized (mutex) {
			Page page = getPage(pageNumber);

			Set<File> launchActions = new HashSet<>();
			Link[] links = page.getLinks();

			if (nonNull(links)) {
				for (Link link : links) {
					if (nonNull(link.uri)) {
						launchActions.add(new File(link.uri));
					}
				}
			}

			return launchActions;
		}
	}

	@Override
	public void addPage(int width, int height) {
		synchronized (mutex) {
			PDFObject resources = doc.newDictionary();
			Buffer buffer = new Buffer();

			PDFObject page = doc.addPage(new Rect(0, 0, width, height), 0, resources, buffer);

			// Insert page object at the end of the document.
			doc.insertPage(-1, page);

			buffer.destroy();
		}
	}

	@Override
	public void deletePage(int pageNumber) {
		synchronized (mutex) {
			doc.deletePage(pageNumber);
		}
	}

	@Override
	public int importPage(DocumentAdapter srcDocument, int pageNumber) {
		synchronized (mutex) {
			PDFGraftMap graftMap;

			if (isNull(graftMapping) || !graftMapping.getKey().equals(srcDocument)) {
				if (nonNull(graftMapping) && nonNull(graftMapping.getValue())) {
					graftMapping.getValue().destroy();
				}

				graftMapping = new AbstractMap.SimpleEntry<>(srcDocument, doc.newPDFGraftMap());
			}

			graftMap = graftMapping.getValue();

			MuPDFDocument muPDFSrcDoc = (MuPDFDocument) srcDocument;

			PDFObject srcPage = muPDFSrcDoc.doc.findPage(pageNumber);
			PDFObject dstPage = doc.newDictionary();

			PDFObject mediaBox = srcPage.get("MediaBox");
			PDFObject rotate = srcPage.get("Rotate");
			PDFObject resources = srcPage.get("Resources");
			PDFObject contents = srcPage.get("Contents");

			if (nonNull(mediaBox)) {
				try {
					dstPage.put("MediaBox", graftMap.graftObject(mediaBox));
				}
				catch (Throwable e) {
					// Ignore
				}
			}
			if (nonNull(rotate)) {
				try {
					dstPage.put("Rotate", graftMap.graftObject(rotate));
				}
				catch (Throwable e) {
					// Ignore
				}
			}
			if (nonNull(resources)) {
				try {
					dstPage.put("Resources", graftMap.graftObject(resources));
				}
				catch (Throwable e) {
					dstPage.put("Resources", doc.newDictionary());
				}
			}
			if (nonNull(contents)) {
				try {
					dstPage.put("Contents", graftMap.graftObject(contents));
				}
				catch (Throwable e) {
					dstPage.put("Contents", doc.newDictionary());
				}
			}

			doc.insertPage(-1, doc.addObject(dstPage));

			return getPageCount() - 1;
		}
	}

	@Override
	public void toOutputStream(OutputStream stream) throws IOException {
		synchronized (mutex) {
			doc.save(new SeekableInputOutputStream() {

				@Override
				public int read(byte[] b) throws IOException {
					return 0;
				}

				@Override
				public long seek(long offset, int whence) throws IOException {
					return 0;
				}

				@Override
				public long position() throws IOException {
					return 0;
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					stream.write(b, off, len);
				}
			}, "compress");
		}
	}

	@Override
	public void setEditableAnnotations(int pageNumber, List<Shape> shapes) throws IOException {
		synchronized (mutex) {
			PDFObject page = doc.findPage(pageNumber);
			PDFObject shapesContent = page.get(PdfDocument.EMBEDDED_SHAPES_KEY);

			if (!shapesContent.isArray()) {
				shapesContent = doc.newArray();
			}

			Buffer buffer = new Buffer();

			for (Shape shape : shapes) {
				// Write shape class.
				String cls = shape.getClass().getCanonicalName();
				byte[] data = cls.getBytes();
				buffer.writeBytes(BitConverter.getBigEndianBytes(data.length));
				buffer.writeBytes(data);

				// Write shape as binary data.
				data = shape.toByteArray();
				buffer.writeBytes(BitConverter.getBigEndianBytes(data.length));
				buffer.writeBytes(data);
			}

			shapesContent.push(doc.addStream(buffer));

			buffer.destroy();

			page.put(PdfDocument.EMBEDDED_SHAPES_KEY, shapesContent);
		}
	}

	@Override
	public List<Shape> getEditableAnnotations(int pageNumber) throws IOException {
		synchronized (mutex) {
			PDFObject pageDict = doc.findPage(pageNumber);
			PDFObject shapesContent = pageDict.get(PdfDocument.EMBEDDED_SHAPES_KEY);

			List<Shape> shapes = new ArrayList<>();

			if (!shapesContent.isArray()) {
				return shapes;
			}

			for (int i = 0; i < shapesContent.size(); i++) {
				PDFObject stream = shapesContent.get(i);

				ByteArrayInputStream byteStream = new ByteArrayInputStream(stream.readStream());

				byte[] lenBytes = new byte[4];

				while (byteStream.available() > 0) {
					// Read shape class.
					byteStream.read(lenBytes);
					byte[] data = new byte[BitConverter.getBigEndianInt(lenBytes)];
					byteStream.read(data);

					try {
						Class<?> cls = Class.forName(new String(data));
						Constructor<?> ctor = cls.getConstructor(byte[].class);

						// Read shape data.
						byteStream.read(lenBytes);
						data = new byte[BitConverter.getBigEndianInt(lenBytes)];
						byteStream.read(data);

						shapes.add((Shape) ctor.newInstance(data));
					}
					catch (Exception e) {
						throw new IOException("Create embedded annotation failed.", e);
					}
				}
			}

			return shapes;
		}
	}

	@Override
	public Map<Integer, List<Shape>> removeEditableAnnotations() throws IOException {
		synchronized (mutex) {
			Map<Integer, List<Shape>> shapes = new HashMap<>();

			for (int number = 0; number < doc.countPages(); number++) {
				PDFObject pageDict = doc.findPage(number);

				// Store parsed annotations before removing them.
				shapes.put(number, getEditableAnnotations(number));

				// Remove binary annotation streams.
				PDFObject shapesContent = pageDict.get(PdfDocument.EMBEDDED_SHAPES_KEY);

				if (shapesContent.isArray()) {
					for (int i = 0; i < shapesContent.size(); i++) {
						PDFObject stream = shapesContent.get(i);

						doc.deleteObject(stream);
					}
				}

				pageDict.delete(PdfDocument.EMBEDDED_SHAPES_KEY);
			}

			return shapes;
		}
	}

	/**
	 * Get the display list of the {@link PageEntry} that is mapped to the specified page number.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The display list of the {@link PageEntry} that is mapped to the specified page number.
	 */
	public DisplayList getDisplayList(int pageNumber) {
		synchronized (mutex) {
			return getPageEntry(pageNumber).displayList;
		}
	}

	/**
	 * Get the page of the {@link PageEntry} that is mapped to the specified page number.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The page of the {@link PageEntry} that is mapped to the specified page number.
	 */
	public Page getPage(int pageNumber) {
		synchronized (mutex) {
			return getPageEntry(pageNumber).page;
		}
	}

	/**
	 * Returns the {@link PageEntry} that is mapped to the specified page number.
	 * If the {@link #displayListMap} doesn't contain a {@link PageEntry} for that page number,
	 * this method loads the page from {@link #doc},
	 * creates a new {@link PageEntry} and adds it to {@link #displayListMap}.
	 *
	 * @param pageNumber The page number.
	 *
	 * @return The {@link PageEntry} that is mapped to the specified page number.
	 */
	private PageEntry getPageEntry(int pageNumber) {
		synchronized (mutex) {
			PageEntry pageEntry = displayListMap.get(pageNumber);

			if (isNull(pageEntry)) {
				Page page = doc.loadPage(pageNumber);
				DisplayList displayList = page.toDisplayList();

				pageEntry = new PageEntry(page, displayList);

				displayListMap.put(pageNumber, pageEntry);
			}

			return pageEntry;
		}
	}

	private static void loadOutline(Outline[] outlines, DocumentOutlineItem outline) {
		if (isNull(outlines)) {
			return;
		}

		Outline lastItem = null;

		for (Outline item : outlines) {
			if (nonNull(lastItem) && lastItem.title.equals(item.title)) {
				// Skip duplicate item, even if the page number is different.
				continue;
			}

			String uri = item.uri;
			Integer pageNumber = null;

			try {
				int sepIndex = uri.indexOf(",");
				String uriPage = uri.substring(uri.indexOf("#") + 1,
						sepIndex > 0 ? uri.indexOf(",") : uri.length());

				pageNumber = Integer.parseInt(uriPage) - 1;
			}
			catch (Exception e) {
				// Ignore.
			}

			// Remove line tabulation character from the title string.
			String title = item.title;
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

			if (nonNull(item.down)) {
				loadOutline(item.down, outlineItem);
			}

			lastItem = item;
		}
	}



	private static class PageEntry {

		final DisplayList displayList;

		final Page page;


		PageEntry(Page page, DisplayList displayList) {
			this.page = page;
			this.displayList = displayList;
		}

	}
}
