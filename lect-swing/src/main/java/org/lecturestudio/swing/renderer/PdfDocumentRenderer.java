/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.swing.renderer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.NotesPosition;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.pdf.PdfDocument;
import org.lecturestudio.core.pdf.pdfbox.PDFGraphics2D;
import org.lecturestudio.core.render.RenderService;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;

/**
 * Renders recorded documents and pages with annotations to a single PDF
 * document.
 *
 * @author Alex Andres
 */
public class PdfDocumentRenderer extends ExecutableBase {

	private RenderService renderService;

	private PresentationParameterProvider ppProvider;

	private List<Document> documents;

	private List<Page> pages;

	private File outputFile;

	private ProgressCallback progressCallback;

	private boolean editable;

	private boolean pageScale;


	/**
	 * Sets the provider for {@code PresentationParameter}s which are used to
	 * render the view state of each document page.
	 *
	 * @param ppProvider The provider for recorded {@code PresentationParameter}s.
	 */
	public void setParameterProvider(PresentationParameterProvider ppProvider) {
		this.ppProvider = ppProvider;
	}

	/**
	 * Sets a list of documents that should be merged into the rendered
	 * document.
	 *
	 * @param docs The documents to render.
	 */
	public void setDocuments(List<Document> docs) {
		this.documents = docs;
	}

	/**
	 * Sets a list of selected pages that should be included into the rendered
	 * document. A page will be rendered only if it exists in one of the
	 * provided documents by {@link #setDocuments(List)}.
	 *
	 * @param pages The pages to render.
	 */
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	/**
	 * Enables the annotations on the pages to be editable when the rendered
	 * documents is opened. The annotations will be available as if they were
	 * created by the user during the presentation.
	 *
	 * @param editable True to make the annotations editable.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * Enables page scaling. If set to {@code true}, the page content will be
	 * rendered with the page transformation provided by the {@code
	 * PresentationParameter}.
	 *
	 * @param pageScale True to enable page scaling.
	 */
	public void setPageScale(boolean pageScale) {
		this.pageScale = pageScale;
	}

	/**
	 * Sets the file path where to write the rendered document. The file path is
	 * mandatory in order to start the document rendering.
	 *
	 * @param file The target document path.
	 */
	public void setOutputFile(File file) {
		outputFile = file;
	}

	/**
	 * Sets the progress callback function to monitor the document render
	 * progress.
	 *
	 * @param callback The progress callback.
	 */
	public void setProgressCallback(ProgressCallback callback) {
		progressCallback = callback;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		renderService = new RenderService();
		renderService.registerRenderer(new ArrowRenderer());
		renderService.registerRenderer(new EllipseRenderer());
		renderService.registerRenderer(new LineRenderer());
		renderService.registerRenderer(new PDFTeXRenderer());
		renderService.registerRenderer(new PDFTextRenderer());
		renderService.registerRenderer(new StrokeRenderer());
		renderService.registerRenderer(new RectangleRenderer());
		renderService.registerRenderer(new TextSelectionRenderer());
	}

	@Override
	protected void startInternal() throws ExecutableException {
		if (isNull(documents)) {
			throw new ExecutableException("No documents to render provided");
		}
		if (isNull(pages)) {
			throw new ExecutableException("No pages to render provided");
		}
		if (isNull(outputFile)) {
			throw new ExecutableException("No output file provided");
		}

		try {
			Document newDocument = new Document();
			newDocument.setTitle(FileUtils.stripExtension(outputFile.getName()));

			int pageCount = documents.stream().mapToInt(Document::getPageCount).sum();
			int pagesWritten = 0;
			float pageStep = 1.f / pageCount;

			for (Page page : pages) {
				if (documents.contains(page.getDocument())) {
					PresentationParameter param = ppProvider.getParameter(page);

					// Import page with all annotations.
					createPage(param, newDocument, page);

					// Progress notification.
					pagesWritten++;

					if (nonNull(progressCallback)) {
						progressCallback.onProgress(pagesWritten * pageStep);
					}
				}
			}

			if (nonNull(outputFile.getParentFile())) {
				Files.createDirectories(outputFile.getParentFile().toPath());
			}

			FileOutputStream fileStream = new FileOutputStream(outputFile);

			newDocument.toOutputStream(fileStream);
			newDocument.close();

			fileStream.flush();
			fileStream.close();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		// Nothing to do.
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		// Nothing to do.
	}

	private void createPage(PresentationParameter param,
			Document newDocument, Page page) throws Exception {
		Rectangle2D pageRect = param.getPageRect();
		NotesPosition notesPosition = page.getDocument().getSplitSlideNotesPositon();

		Page newPage = newDocument.createPage(page, pageScale ? pageRect : null);
		int pageIndex = newPage.getPageNumber();

		if (!page.hasShapes()) {
			// Nothing to render.
			return;
		}

		// Create page annotation graphics renderer.
		PdfDocument pdfDocument = newDocument.getPdfDocument();
		PDFGraphics2D graphics;

		if (editable) {
			// Tag graphics stream to be able to find it later.
			graphics = (PDFGraphics2D) pdfDocument.createAppendablePageGraphics2D(
					pageIndex, PdfDocument.EMBEDDED_SHAPES_KEY, NotesPosition.UNKNOWN);
		}
		else {
			graphics = (PDFGraphics2D) pdfDocument.createAppendablePageGraphics2D(
					pageIndex, NotesPosition.UNKNOWN);
		}

		SwingGraphicsContext gc = new SwingGraphicsContext(graphics);

		// Calculate page annotation scaling.
		AffineTransform transform = new AffineTransform();
		transform.translate(-pageRect.getX(), -pageRect.getY());
		transform.scale(pageRect.getWidth(), pageRect.getWidth());

		if (pageScale) {
			transform.scale(1 / pageRect.getWidth(), 1 / pageRect.getHeight());
		}

		AffineTransform annotTransform = transform.createInverse();
		Rectangle2D mediaBox = pdfDocument.getPageMediaBox(pageIndex, notesPosition);

		double pageWidth = mediaBox.getWidth();
		double sx = pageWidth * annotTransform.getScaleX();
		double tx = pageWidth * annotTransform.getTranslateX();
		double ty = pageWidth * annotTransform.getTranslateY();

		if(page.getDocument().getActualSplitSlideNotesPositon() == NotesPosition.LEFT) {
			mediaBox.setRect(mediaBox.getWidth(), mediaBox.getY(), mediaBox.getWidth(), mediaBox.getHeight());
			tx -= pageWidth;
		}
		gc.translate(-tx , ty + mediaBox.getHeight());
		gc.scale(sx, -sx);

		// Draw shapes.
		renderService.renderShapes(page.getShapes(), gc);

		if (editable) {
			// Create additional binary encoded shape stream.
			List<Shape> shapes = page.getShapes().stream()
					.filter(shape -> renderService.hasRenderer(shape.getClass()))
					.collect(Collectors.toList());

			pdfDocument.createEditableAnnotationStream(pageIndex, shapes);
		}

		graphics.close();
	}
}
