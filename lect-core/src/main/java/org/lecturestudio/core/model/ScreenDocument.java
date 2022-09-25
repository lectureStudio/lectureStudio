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
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.pdf.pdfbox.PDFGraphics2D;

public class ScreenDocument extends Document {

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

		int x = (int) ((rect.getWidth() - image.getWidth(null)) / 2);
		int y = (int) ((rect.getHeight() - image.getHeight(null)) / 2);
		int w = (int) rect.getWidth();
		int h = (int) rect.getHeight();

		PDFGraphics2D g2d = (PDFGraphics2D) getPdfDocument().createPageGraphics2D(pageIndex);
		// Draw screen frame onto a black page background.
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, w, h);
		g2d.drawImage(image, x, y, null);
		g2d.close();
		g2d.dispose();

		return page;
	}
}
