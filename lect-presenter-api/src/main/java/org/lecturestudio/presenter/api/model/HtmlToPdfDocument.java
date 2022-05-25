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

import com.openhtmltopdf.extend.FSDOMMutator;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;

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
		doc.close();

		stream.flush();
		stream.close();

		return new PdfDocument(stream.toByteArray());
	}

	protected static void renderHtmlPage(org.jsoup.nodes.Document jdoc,
			PDDocument pdDoc) throws IOException {
		OutputSettings outputSettings = new OutputSettings();
		outputSettings.prettyPrint(false);
		outputSettings.escapeMode(EscapeMode.xhtml);

		jdoc.outputSettings(outputSettings);

		var builder = new PdfRendererBuilder()
				.withW3cDocument(new W3CDom().fromJsoup(jdoc), "classpath:/html/")
				.withProducer("lecturePresenter")
				.addDOMMutator(new EmptyDivMutator())
				.addDOMMutator(new FontColorMutator())
				.usePDDocument(pdDoc)
				.useDefaultPageSize(PAGE_WIDTH / 72f, PAGE_HEIGHT / 72f,
						PageSizeUnits.INCHES)
				.useProtocolsStreamImplementation(new ClassPathStreamFactory(),
						"classpath");

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

		@Override
		public FSStream getUrl(String uri) {
			try {
				final URI fullUri = new URI(uri);
				return new PDFClassPathStream(fullUri.getPath());
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
}
