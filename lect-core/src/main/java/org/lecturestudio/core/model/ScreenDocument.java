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

package org.lecturestudio.core.model;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.pdf.pdfbox.PDFGraphics2D;

public class ScreenDocument extends Document {

	private static final int PADDING = 10;


	public ScreenDocument() throws IOException {
		super();

		setDocumentType(DocumentType.SCREEN);
	}

	public ScreenDocument(byte[] byteArray) throws IOException {
		super(byteArray);

		setDocumentType(DocumentType.SCREEN);
	}

	public Page createPage(BufferedImage image) {
		Page page = createPage();
		int pageIndex = page.getPageNumber();

		Rectangle2D rect = page.getPageRect();

		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double pageWidth = rect.getWidth() - PADDING * 2;
		double pageHeight = rect.getHeight() - PADDING * 2;

		PageMetrics metrics = new PageMetrics(pageWidth, pageHeight);
		Dimension2D size = metrics.convert(imageWidth, imageHeight);

		double s = pageWidth / imageWidth;

		if (imageHeight > size.getHeight()) {
			s = pageHeight / imageHeight;
		}

		double sInv = 1 / s;

		int x = (int) (((pageWidth - imageWidth * s) / 2 + PADDING) * sInv);
		int y = (int) (((pageHeight - imageHeight * s) / 2 + PADDING) * sInv);
		int w = (int) imageWidth;
		int h = (int) imageHeight;

		try {
			getPdfDocument().setPageContentTransform(pageIndex, AffineTransform.getScaleInstance(s, s));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		int padding = (int) (PADDING * s);

		PDFGraphics2D g2d = (PDFGraphics2D) getPdfDocument().createAppendablePageGraphics2D(pageIndex, getSplittedSlideNotes());
		// Draw screen frame with a border around it.
		g2d.setColor(Color.BLACK);
		g2d.drawRect(x - padding, y - padding, w + padding * 2, h + padding * 2);
		g2d.drawImage(image, x, y, null);
		g2d.close();
		g2d.dispose();

		return page;
	}
}
