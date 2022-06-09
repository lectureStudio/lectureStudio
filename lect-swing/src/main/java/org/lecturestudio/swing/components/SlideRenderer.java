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

package org.lecturestudio.swing.components;

import static java.util.Objects.isNull;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import org.lecturestudio.core.app.configuration.WhiteboardConfiguration;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.GridShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.ViewType;

public class SlideRenderer {

	private static final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private static final GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

	private final Dimension2D imageRect = new Dimension2D(0, 0);

	private final ViewType viewType;

	private Page page;

	private PresentationParameter pParameter;

	private BufferedImage currentImage;
	private BufferedImage backImage;
	private BufferedImage frontImage;

	private BufferedImage bufferImage;
	private Graphics2D bufferg2d;

	private AffineTransform deviceTransform;

	private RenderController renderController;


	public SlideRenderer(ViewType viewType) {
		this.viewType = viewType;

		setDeviceTransform(AffineTransform.getScaleInstance(
				gc.getDefaultTransform().getScaleX(),
				gc.getDefaultTransform().getScaleY())
		);
	}

	public void setRenderController(RenderController renderController) {
		this.renderController = renderController;
	}

	public void setDeviceTransform(AffineTransform transform) {
		deviceTransform = transform;
	}

	public BufferedImage getImage() {
		return bufferImage;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public void setPresentationParameter(PresentationParameter parameter) {
		this.pParameter = parameter;
	}

	public synchronized void renderPage() {
		updatePageImage();
		renderShapes();
	}

	public synchronized void renderShapes() {
		if (page == null || pParameter == null || currentImage == null) {
			return;
		}

		Graphics2D g = currentImage.createGraphics();
		refreshFrontImage(g, page.getShapes(), pParameter);
		g.dispose();

		bufferg2d.drawImage(currentImage, 0, 0, null);
	}

	/**
	 * Sets the new background image and repaints the component.
	 *
	 * @param image new BufferedImage
	 */
	private void setBackImage(BufferedImage image) {
		if (isNull(image)) {
			return;
		}

		Dimension size = new Dimension((int) imageRect.getWidth(), (int) imageRect.getHeight());

		frontImage = createImage(frontImage, size);
		currentImage = createImage(currentImage, size);
		bufferImage = createImage(bufferImage, size);

		bufferg2d = createGraphics(bufferg2d, bufferImage);

		Graphics2D g2d = frontImage.createGraphics();
		g2d.drawImage(backImage, 0, 0, null);
		g2d.dispose();

		g2d = currentImage.createGraphics();
		g2d.drawImage(frontImage, 0, 0, null);
		g2d.dispose();
	}

	/**
	 * Updates the background image. This method is called when the background
	 * image need to be re-rendered.
	 */
	private synchronized void updatePageImage() {
		if (page == null) {
			return;
		}
		if (imageRect.getWidth() == 0 || imageRect.getHeight() == 0) {
			return;
		}

		Dimension size = new Dimension((int)imageRect.getWidth(), (int)imageRect.getHeight());

		backImage = createBackImage(backImage, size);

		renderController.renderPage(backImage, page, viewType);

		setBackImage(backImage);
	}
	
	private void drawShapes(final Graphics2D g, List<Shape> shapes) {
		SwingGraphicsContext gc = new SwingGraphicsContext(g);

		renderController.renderShapes(gc, viewType, imageRect, page, shapes);
	}

	private synchronized void refreshFrontImage(Graphics g, List<Shape> shapes,
			PresentationParameter parameter) {
		if (frontImage == null) {
			return;
		}

		if (parameter.showGrid()) {
			WhiteboardConfiguration wbConfig = parameter.getWhiteboardConfig();

			GridShape gridShape = new GridShape();
			gridShape.setViewRatio(new Dimension2D(4.0, 3.0));
			gridShape.setColor(wbConfig.getGridColor());
			gridShape.setHorizontalLinesInterval(wbConfig.getHorizontalLinesInterval());
			gridShape.setHorizontalLinesVisible(wbConfig.getHorizontalLinesVisible());
			gridShape.setVerticalLinesInterval(wbConfig.getVerticalLinesInterval());
			gridShape.setVerticalLinesVisible(wbConfig.getVerticalLinesVisible());

			shapes.add(0, gridShape);
		}

		Graphics2D g2d = frontImage.createGraphics();
		g2d.drawImage(backImage, 0, 0, null);
		drawShapes(g2d, shapes);
		g2d.dispose();
		
		g.drawImage(frontImage, 0, 0, null);
	}

	void resizeBuffer(Dimension2D size) {
		if (size.getWidth() < 1 || size.getHeight() < 1) {
			return;
		}

		final int imageWidth = (int) (size.getWidth() * deviceTransform.getScaleX());
		final int imageHeight = (int) (size.getHeight() * deviceTransform.getScaleY());

		Dimension newSize = new Dimension(imageWidth, imageHeight);

		if (imageWidth != imageRect.getWidth() || imageHeight != imageRect.getHeight()) {
			imageRect.setSize(imageWidth, imageHeight);

			frontImage = createImage(frontImage, newSize);
			currentImage = createImage(currentImage, newSize);
			bufferImage = createImage(bufferImage, newSize);

			bufferg2d = createGraphics(bufferg2d, bufferImage);
		}
	}

	public void dispose() {
		if (backImage != null) {
			backImage.flush();
			backImage = null;
		}
		if (frontImage != null) {
			frontImage.flush();
			frontImage = null;
		}
		if (currentImage != null) {
			currentImage.flush();
			currentImage = null;
		}
		if (bufferImage != null) {
			bufferImage.flush();
			bufferImage = null;
		}
		if (bufferg2d != null) {
			bufferg2d.dispose();
		}
	}
	
	private BufferedImage createImage(BufferedImage reference, Dimension size) {
		if (reference != null) {
			if (size.width == reference.getWidth() && size.height == reference.getHeight()) {
				return reference;
			}

			reference.flush();
			reference = null;
		}

		reference = gc.createCompatibleImage(size.width, size.height);
		reference.setAccelerationPriority(1);

		return reference;
	}

	private BufferedImage createBackImage(BufferedImage reference, Dimension size) {
		if (reference != null) {
			if (size.width == reference.getWidth() && size.height == reference.getHeight()) {
				return reference;
			}

			reference.flush();
			reference = null;
		}

		reference = gc.createCompatibleImage(size.width, size.height);
		reference.setAccelerationPriority(1);

		return reference;
	}

	private Graphics2D createGraphics(Graphics2D reference, BufferedImage refImage) {
		if (reference != null) {
			reference.dispose();
			reference = null;
		}
		if (refImage != null) {
			reference = refImage.createGraphics();
		}

		return reference;
	}
}
