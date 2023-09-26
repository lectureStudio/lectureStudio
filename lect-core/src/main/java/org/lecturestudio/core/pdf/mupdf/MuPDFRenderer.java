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

import com.artifex.mupdf.fitz.ColorSpace;
import com.artifex.mupdf.fitz.DisplayList;
import com.artifex.mupdf.fitz.DrawDevice;
import com.artifex.mupdf.fitz.Matrix;
import com.artifex.mupdf.fitz.Pixmap;
import com.artifex.mupdf.fitz.Rect;
import com.artifex.mupdf.fitz.RectI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.NotesPosition;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.pdf.DocumentRenderer;
import org.lecturestudio.core.view.PresentationParameter;

public class MuPDFRenderer implements DocumentRenderer {

	private final MuPDFDocument document;

	private final Object lock = new Object();

	private final Map<Integer, Point2D> sizeMap = new HashMap<>();


	/**
	 * Create a new {@link MuPDFRenderer} with the specified
	 * {@link MuPDFDocument}.
	 *
	 * @param document The {@link MuPDFDocument}.
	 */
	public MuPDFRenderer(MuPDFDocument document) {
		this.document = document;
	}

	@Override
	public void render(Page page, PresentationParameter parameter,
			BufferedImage image) throws IOException {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		synchronized (lock) {
			Rectangle2D pageRect = parameter.getViewRect();
			int pageNumber = page.getPageNumber();
			//Needed for notes on right side
			float stmX = 0;
			float ctmX = 0;

			double sx = imageWidth / pageRect.getWidth();
			double sy = imageHeight / pageRect.getHeight();

			int x = (int) (pageRect.getX() * sx);
			int y = (int) (pageRect.getY() * sy);

			DisplayList displayList = document.getDisplayList(pageNumber);



			com.artifex.mupdf.fitz.Page p = document.getPage(pageNumber);
			Rect bounds = p.getBounds();
			System.out.println("Bounds: " + bounds);

			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.UNKNOWN){
				if(bounds.x1/bounds.y1 >= 2){
					page.getDocument().setSplitSlideNotesPositon(NotesPosition.RIGHT);
				}else {
					page.getDocument().setSplitSlideNotesPositon(NotesPosition.NONE);
				}
				page.getDocument().calculateCropbox();
			}
			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.RIGHT){
				bounds.x1 = bounds.x1/2;
			}

			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.LEFT){
				bounds.x0 = bounds.x1/2;
				x = (int) (x - (imageWidth -sx));
			}

			float scale = (float) (1.D / pageRect.getWidth());
			float pageSx = imageWidth / (bounds.x1 - bounds.x0);
			float pageSy = imageHeight / (bounds.y1 - bounds.y0);

			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.LEFT){
				stmX = bounds.x0 * pageSx;
				ctmX = bounds.x0 * pageSx;
			}

			Matrix ctm = new Matrix();
			//ctm.translate(-x, -y);
			ctm.translate(-x - ctmX, -y);
			ctm.scale(pageSx * scale, pageSy * scale);

			int px = (int) (pageRect.getX() * pageSx);
			int py = (int) (pageRect.getY() * pageSy);

			Matrix stm = new Matrix();
			//stm.translate(-px, -py);
			stm.translate(-px - stmX, -py);
			stm.scale(pageSx, pageSy);

			if (parameter.isTranslation()) {
				renderPan(parameter, image, displayList, bounds, ctm, stm);
			}
			else {
				RectI scissor = new RectI(bounds).transform(stm);
				Rect pixmapBounds = new Rect(0, 0, imageWidth, imageHeight);

				renderImage(image, displayList, pixmapBounds, ctm, scissor);

				sizeMap.put(imageWidth, new Point2D(x, y));
			}
		}
	}

	@Override
	public void renderNotes(Page page, PresentationParameter parameter,
					   BufferedImage image) throws IOException {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		synchronized (lock) {
			Rectangle2D pageRect = parameter.getViewRect();
			int pageNumber = page.getPageNumber();
			//Needed for notes on right side
			float stmX = 0;
			float ctmX = 0;

			double sx = imageWidth / pageRect.getWidth();
			double sy = imageHeight / pageRect.getHeight();

			int x = (int) (pageRect.getX() * sx);
			int y = (int) (pageRect.getY() * sy);

			DisplayList displayList = document.getDisplayList(pageNumber);

			com.artifex.mupdf.fitz.Page p = document.getPage(pageNumber);
			Rect bounds = p.getBounds();

			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.LEFT){
				bounds.x1 = bounds.x1/2;
			}
			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.RIGHT){
				bounds.x0 = bounds.x1/2;
			}

			float scale = (float) (1.D / pageRect.getWidth());
			float pageSx = imageWidth / (bounds.x1 - bounds.x0);
			float pageSy = imageHeight / (bounds.y1 - bounds.y0);

			if(page.getDocument().getSplitSlideNotesPositon() == NotesPosition.RIGHT){
				stmX = bounds.x0 * pageSx;
				ctmX = bounds.x0 * pageSx;
			}

			Matrix ctm = new Matrix();
			//ctm.translate(-x, -y);
			ctm.translate(-x - ctmX, -y);
			ctm.scale(pageSx * scale, pageSy * scale);

			int px = (int) (pageRect.getX() * pageSx);
			int py = (int) (pageRect.getY() * pageSy);

			Matrix stm = new Matrix();
			//stm.translate(-px, -py);
			stm.translate(-px - stmX, -py);
			stm.scale(pageSx, pageSy);

			if (parameter.isTranslation()) {
				renderPan(parameter, image, displayList, bounds, ctm, stm);
			}
			else {
				RectI scissor = new RectI(bounds).transform(stm);
				Rect pixmapBounds = new Rect(0, 0, imageWidth, imageHeight);

				renderImage(image, displayList, pixmapBounds, ctm, scissor);

				sizeMap.put(imageWidth, new Point2D(x, y));
			}
		}
	}

	private void renderPan(PresentationParameter parameter, BufferedImage image,
			DisplayList displayList, Rect bounds, Matrix ctm, Matrix stm) {
		Rectangle2D pageRect = parameter.getViewRect();

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double sx = imageWidth / pageRect.getWidth();
		double sy = imageHeight / pageRect.getHeight();

		int x = (int) (pageRect.getX() * sx);
		int y = (int) (pageRect.getY() * sy);

		Point2D prevTx = sizeMap.get(imageWidth);

		int dx = (int) -(x - prevTx.getX());
		int dy = (int) -(y - prevTx.getY());

		Graphics2D g = image.createGraphics();
		g.copyArea(0, 0, imageWidth - dx, imageHeight - dy, dx, dy);

		if (dx != 0) {
			// Horizontal panning.
			int pixW = Math.abs(dx);
			int pixH = imageHeight;
			int pixX = (dx < 0) ? imageWidth - pixW : 0;
			int pixY = 0;

			Rect pixmapBounds = new Rect(pixX, pixY, pixX + pixW, pixY + pixH);

			RectI scissor = new RectI(bounds).transform(stm);

			if (dx < 0) {
				scissor.x0 = scissor.x1 - pixW;
			}
			else {
				scissor.x1 = pixW;
			}

			BufferedImage tempImage = new BufferedImage(pixW, pixH, BufferedImage.TYPE_INT_RGB);

			renderImage(tempImage, displayList, pixmapBounds, ctm, scissor);

			g.drawImage(tempImage, pixX, pixY, null);
		}
		if (dy != 0) {
			// Vertical panning.
			int pixW = imageWidth;
			int pixH = Math.abs(dy);
			int pixX = 0;
			int pixY = (dy < 0) ? imageHeight - pixH : 0;

			Rect pixmapBounds = new Rect(pixX, pixY, pixX + pixW, pixY + pixH);

			RectI scissor = new RectI(bounds).transform(stm);

			if (dy < 0) {
				scissor.y0 = scissor.y1 - pixH;
			}
			else {
				scissor.y1 = pixH;
			}

			BufferedImage tempImage = new BufferedImage(pixW, pixH, BufferedImage.TYPE_INT_RGB);

			renderImage(tempImage, displayList, pixmapBounds, ctm, scissor);

			g.drawImage(tempImage, pixX, pixY, null);
		}

		g.dispose();

		sizeMap.put(imageWidth, new Point2D(x, y));
	}

	private void renderImage(BufferedImage image, DisplayList displayList,
			Rect pixmapBounds, Matrix ctm, RectI scissor) {
		Pixmap pixmap = new Pixmap(ColorSpace.DeviceBGR, pixmapBounds, true);
		pixmap.clear(255);

		DrawDevice dev = new DrawDevice(pixmap);
		displayList.run(dev, ctm, new Rect(scissor), null);
		dev.close();
		dev.destroy();

		WritableRaster raster = image.getRaster();
		int[] imageData = ((DataBufferInt) raster.getDataBuffer()).getData();
		int[] pixels = pixmap.getPixels();

		System.arraycopy(pixels, 0, imageData, 0, pixels.length);

		pixmap.destroy();
	}
}
