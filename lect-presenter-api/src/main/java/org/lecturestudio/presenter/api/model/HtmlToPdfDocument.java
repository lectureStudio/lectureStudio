/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.model;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.openhtmltopdf.extend.FSDOMMutator;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.pdf.PdfDocument;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * PDF-based document that is generated from HTML input.
 *
 * @author Alex Andres
 */
public abstract class HtmlToPdfDocument extends Document {

	protected static final int PAGE_WIDTH = 640;

	protected static final int PAGE_HEIGHT = 480;


	public HtmlToPdfDocument() throws IOException {
		XRLog.setLoggingEnabled(false);
	}

	protected static PdfDocument createPdfDocument(PDDocument doc)
			throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		doc.save(stream);

		stream.flush();
		stream.close();

		return new PdfDocument(stream.toByteArray());
	}

	protected static Rectangle2D getPageBounds(PDDocument tplDoc) {
		if (isNull(tplDoc)) {
			return new Rectangle2D(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
		}

		PDPage tplPage = tplDoc.getPage(0);
		PDRectangle mediaBox = tplPage.getMediaBox();

		return new Rectangle2D(0, 0, mediaBox.getWidth(), mediaBox.getHeight());
	}

	protected static void renderHtmlPage(org.jsoup.nodes.Document jdoc,
			PDDocument tplDoc, PDDocument pdDoc, Rectangle2D contentBounds,
			Map<String, String> resourceMap) throws IOException {
		OutputSettings outputSettings = new OutputSettings();
		outputSettings.prettyPrint(false);
		outputSettings.escapeMode(EscapeMode.xhtml);

		jdoc.outputSettings(outputSettings);

		final Rectangle2D pageBounds = getPageBounds(tplDoc);
		final PDPage tplPage = nonNull(tplDoc) ? tplDoc.getPage(0) : null;
		float pageWidth = (float) pageBounds.getWidth();
		float pageHeight = (float) pageBounds.getHeight();

		if (nonNull(contentBounds)) {
			int marginLeft = (int) (contentBounds.getX() * pageWidth);
			int marginTop = (int) (contentBounds.getY() * pageHeight);
			int marginRight = (int) (pageWidth - (contentBounds.getX() + contentBounds.getWidth()) * pageWidth);
			int marginBottom = (int) (pageHeight - (contentBounds.getY() + contentBounds.getHeight()) * pageHeight);

			marginLeft = (int) (marginLeft * 1.5);
			marginTop = (int) (marginTop * 1.5);
			marginRight = (int) (marginRight * 1.5);
			marginBottom = (int) (marginBottom * 1.5);

			var head = jdoc.head();
			var style = head.appendElement("style");
			style.text(String.format("@page { margin: %dpx %dpx %dpx %dpx; }",
					marginTop, marginRight, marginBottom, marginLeft));
		}

		var builder = new PdfRendererBuilder()
				.withW3cDocument(new W3CDom().fromJsoup(jdoc), "classpath:/")
				.withProducer("lecturePresenter")
				.addDOMMutator(new EmptyDivMutator())
				.addDOMMutator(new FontColorMutator())
				.usePDDocument(pdDoc)
				.useDefaultPageSize(pageWidth / 72f, pageHeight / 72f,
						PageSizeUnits.INCHES)
				.useProtocolsStreamImplementation(
						new ClassPathStreamFactory(resourceMap), "classpath");

		if (nonNull(tplPage)) {
			builder.usePageSupplier((doc, pWidth, pHeight, pNumber, shadowPageNumber) -> {
				PDPage imported;

				try {
					imported = doc.importPage(tplPage);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}

				imported.setResources(tplPage.getResources());

				return imported;
			});
		}

		var buildPdfRenderer = builder.buildPdfRenderer();
		buildPdfRenderer.layout();
		buildPdfRenderer.createPDFWithoutClosing();
		buildPdfRenderer.close();
	}



	private static class EmptyDivMutator implements FSDOMMutator {

		@Override
		public void mutateDocument(org.w3c.dom.Document document) {
			NodeList divs = document.getElementsByTagName("div");

			for (int i = 0; i < divs.getLength(); i++) {
				org.w3c.dom.Element div = (org.w3c.dom.Element) divs.item(i);

				if (div.getChildNodes().getLength() == 1) {
					org.w3c.dom.Node node = div.getChildNodes().item(0);

					if (node.getNodeType() == Node.TEXT_NODE
							&& node.getTextContent().trim().isEmpty()) {
						org.w3c.dom.Document d = div.getOwnerDocument();
						div.appendChild(d.createElement("br"));
					}
				}
			}
		}
	}



	private static class FontColorMutator implements FSDOMMutator {

		@Override
		public void mutateDocument(org.w3c.dom.Document document) {
			NodeList fontTags = document.getElementsByTagName("font");

			for (int i = 0; i < fontTags.getLength(); i++) {
				var fontTag = (Element) fontTags.item(i);

				if (fontTag.hasAttribute("color")) {
					String color = fontTag.getAttribute("color");

					if (!fontTag.hasAttribute("style")) {
						fontTag.setAttribute("style", "color: " + color + ';');
					}
					else {
						String oldStyle = fontTag.getAttribute("style");
						String newStyle = oldStyle + "; color: " + color + ';';

						fontTag.setAttribute("style", newStyle);
					}
				}
			}
		}
	}



	private static class ClassPathStreamFactory implements FSStreamFactory {

		final Map<String, String> resourceMap;


		ClassPathStreamFactory(Map<String, String> resourceMap) {
			this.resourceMap = resourceMap;
		}

		@Override
		public FSStream getUrl(String uri) {
			try {
				final URI fullUri = new URI(uri);
				final String path = fullUri.getPath();
				final String replacement = resourceMap.get(path.substring(1));

				if (nonNull(replacement)) {
					URI repUri = new URI(replacement);

					if (repUri.getScheme().equals("file")) {
						return new PDFFileStream(repUri);
					}
				}

				return new PDFClassPathStream(path);
			}
			catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}

	}



	public static class PDFClassPathStream implements FSStream {

		private final String uri;


		public PDFClassPathStream(String uri) {
			this.uri = "resources" + uri;
		}

		@Override
		public InputStream getStream() {
			try {
				final ClassLoader classLoader = getClass().getClassLoader();
				return classLoader.getResourceAsStream(uri);
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Reader getReader() {
			try {
				return new InputStreamReader(getStream(), StandardCharsets.UTF_8);
			}
			catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
	}



	public static class PDFFileStream implements FSStream {

		private final URI uri;


		public PDFFileStream(URI uri) {
			this.uri = uri;
		}

		@Override
		public InputStream getStream() {
			try {
				return new FileInputStream(uri.getPath());
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Reader getReader() {
			try {
				return new FileReader(uri.getPath());
			}
			catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
