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

package org.lecturestudio.core.pdf.pdfbox;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.view.PresentationParameter;

public class PDFBoxRenderer implements DocumentRenderer {

	private final PDFRenderer renderer;


	/**
	 * Creates a new {@link PDFBoxRenderer} and uses the specified {@link PDDocument}
	 * to initialize {@link #renderer} with a new {@link PDFRenderer}.
	 *
	 * @param doc The {@link PDDocument}.
	 */
	public PDFBoxRenderer(PDDocument doc) {
		renderer = new PDFRenderer(doc);
		renderer.setSubsamplingAllowed(true);
	}

	@Override
	public void render(Page page, PresentationParameter parameter, BufferedImage image) throws IOException {
		Graphics2D g = image.createGraphics();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		if (page.getDocument().isWhiteboard()) {
			g.setColor(new Color(parameter.getBackgroundColor().getRGBA()));
			g.fillRect(0, 0, imageWidth, imageHeight);
		}
		else {
			Rectangle2D pageRect = parameter.getViewRect();

			double sx = imageWidth / pageRect.getWidth();
			double sy = imageHeight / pageRect.getHeight();
			double tx = pageRect.getX() * sx;
			double ty = pageRect.getY() * sy;

			double s = 1.D / pageRect.getWidth();

			sx = imageWidth / page.getPageRect().getWidth() * s;
			sy = imageHeight / page.getPageRect().getHeight() * s;

			g.translate(-tx, -ty);
			g.scale(sx, sy);

			renderer.renderPageToGraphics(page.getPageNumber(), g);
		}

		g.dispose();
	}
}
