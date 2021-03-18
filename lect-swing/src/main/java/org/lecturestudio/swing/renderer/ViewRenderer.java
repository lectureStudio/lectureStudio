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

package org.lecturestudio.swing.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.GridConfiguration;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.ArrowShape;
import org.lecturestudio.core.model.shape.EllipseShape;
import org.lecturestudio.core.model.shape.GridShape;
import org.lecturestudio.core.model.shape.LineShape;
import org.lecturestudio.core.model.shape.StrokeShape;
import org.lecturestudio.core.model.shape.RectangleShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextSelectionShape;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.SlideView;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.Rectangle2DConverter;

public class ViewRenderer {

	private static final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private static final GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	
	private final Dimension2D imageRect = new Dimension2D(0, 0);

	private final ViewType viewType;
	
	private final Configuration config;
	
	private Page page;
	
	private PresentationParameterProvider ppProvider;
	
	private BufferedImage currentImage;
	private BufferedImage backImage;
	private BufferedImage frontImage;
	private BufferedImage watermark;
	
	private BufferedImage bufferImage;
	private Graphics2D bufferg2d;

	private AffineTransform deviceTransform;

	private Shape lastShape;

	private RenderController renderController;
	

	public ViewRenderer(ViewType viewType, Configuration config) {
		this.viewType = viewType;
		this.config = config;
	}

	public void setRenderController(RenderController renderController) {
		this.renderController = renderController;
	}

	public void setDeviceTransform(AffineTransform transform) {
		this.deviceTransform = transform;
	}

	public BufferedImage getImage() {
		return bufferImage;
	}
	
	public Dimension2D getImageRect() {
		return imageRect;
	}
	
	public void setPage(Page page) {
		this.page = page;
	}
	
	public void setParameter(PresentationParameterProvider ppProvider) {
		this.ppProvider = ppProvider;
	}
	
	public synchronized void renderPage(Page page, Dimension size, Point2D pan) {
		panPage(page, size, pan);
		renderForeground();
	}

	public synchronized void renderPage(Page page, Dimension size) {
		updateBackImage(page, size);
		renderForeground();
	}
	
	public synchronized void renderForeground() {
		if (page == null || ppProvider == null || currentImage == null)
			return;
		
		PresentationParameter parameter = ppProvider.getParameter(page);
		List<Shape> shapes = page.getShapes();
		Shape shape = null;
		
		if (!shapes.isEmpty()) {
			shape = shapes.remove(shapes.size() - 1);
		}
		
		
		Graphics2D g = currentImage.createGraphics();
		refreshFrontImage(g, page, shapes, parameter);
		
		lastShape = null;
		
		
		
		if (shape != null) {
			lastShape = shape;
			
			drawShape(g, shape);
		}
		
		
		
		g.dispose();
		
		bufferg2d.drawImage(currentImage, 0, 0, null);
	}
	
	public synchronized void render(Page page, Shape shape, org.lecturestudio.core.geometry.Rectangle2D clip) {
		if (page == null || frontImage == null)
			return;

		Graphics2D g = currentImage.createGraphics();
		
		PresentationParameter parameter = ppProvider.getParameter(page);
		
		if (!page.hasShapes()) {
			refreshBackground(g, page, parameter);
			g.dispose();
			return;
		}
		
		// Draw last shape permanently.
		if (!shape.equals(lastShape)) {
			Graphics2D g2d = frontImage.createGraphics();
			drawShape(g2d, lastShape);
			g2d.dispose();
			
			lastShape = null;
		}

		// Cache shapes.
		if (shape.getClass().isAssignableFrom(TextSelectionShape.class) ||
			shape.getClass().isAssignableFrom(StrokeShape.class) ||
			shape.getClass().isAssignableFrom(LineShape.class) ||
			shape.getClass().isAssignableFrom(ArrowShape.class) ||
			shape.getClass().isAssignableFrom(RectangleShape.class) ||
			shape.getClass().isAssignableFrom(EllipseShape.class)) {
			lastShape = shape;
		}

		g.drawImage(frontImage, 0, 0, null);

		if (page.hasShapes()) {
			drawShape(g, shape);
		}

		g.dispose();
		
		bufferg2d.drawImage(currentImage, 0, 0, null);
	}
	
	public Point2D getScale(PresentationParameter parameter) {
		org.lecturestudio.core.geometry.Rectangle2D pageRect = parameter.getViewRect();
		Point2D scale = calculateScale(imageRect, Rectangle2DConverter.INSTANCE.to(pageRect));
		
		return scale;
	}
	
	/**
	 * Calculates the scale factors.
	 * 
	 * @param destRect
	 * @param pageRect
	 * @return
	 */
	protected Point2D calculateScale(Dimension2D destRect, Rectangle2D pageRect) {
		return new Point2D.Double(destRect.getWidth() / pageRect.getWidth(), destRect.getHeight() / pageRect.getHeight());
	}
	
	/**
	 * Removes the slide image from the view.
	 */
	public void removeSlideImage() {
		backImage = null;
	}
	
	/**
	 * Sets the new background image and repaints the component.
	 * 
	 * @param image new BufferedImage
	 */
	private void setBackImage(BufferedImage image) {
		if (image != null) {
			PresentationParameter parameter = ppProvider.getParameter(page);
			Dimension size = new Dimension((int)imageRect.getWidth(), (int)imageRect.getHeight());

			frontImage = createImage(frontImage, size);
			currentImage = createImage(currentImage, size);
			bufferImage = createImage(bufferImage, size);
			
			bufferg2d = createGraphics(bufferg2d, bufferImage);
			
			Graphics2D g2d = frontImage.createGraphics();
			refreshBackground(g2d, page, parameter);
			g2d.dispose();
			
			g2d = currentImage.createGraphics();
			g2d.drawImage(frontImage, 0, 0, null);
			g2d.dispose();
		}
	}
	
	public void setWatermark(BufferedImage image) {
		if (watermark != null)
			watermark.flush();
		
		this.watermark = image;
		
		renderForeground();
	}

	public synchronized void panPage(Page page, Dimension size, Point2D pan) {
		if (isWhiteboardSlide(page)) {
			return;
		}

		renderController.renderPage(backImage, page, viewType);

		setBackImage(backImage);
	}

	/**
	 * Updates the background image. This method is called when the background
	 * image need to be re-rendered.
	 */
	public synchronized void updateBackImage(Page page, Dimension size) {
		if (page == null) {
			adjustImageRect(size);
			return;
		}
		
		if (imageRect.getWidth() == 0 || imageRect.getHeight() == 0) {
			if (!adjustImageRect(size)) {
				return;
			}
		}

		size = new Dimension((int)imageRect.getWidth(), (int)imageRect.getHeight());

		backImage = createBackImage(backImage, size);

		Graphics2D g2d = backImage.createGraphics();
		
		// Clear background.
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, size.width, size.height);
		
		if (isWhiteboardSlide(page)) {
			g2d.dispose();
			setBackImage(backImage);
			return;
		}

		g2d.dispose();

		renderController.renderPage(backImage, page, viewType);

		setBackImage(backImage);
	}
	
	/**
	 * Updates the front image. The front image only gets the shapes of the page
	 * rendered.
	 * 
	 * @param g The graphics context.
	 * @param shape The new or modified shape.
	 */
	private void drawShape(final Graphics2D g, Shape shape) {
		List<Shape> shapes = new ArrayList<>();
		shapes.add(shape);
		
		SwingGraphicsContext gc = new SwingGraphicsContext(g);

		renderController.renderShapes(gc, viewType, imageRect, page, shapes);
	}
	
	private void drawShapes(final Graphics2D g, List<Shape> shapes) {
		SwingGraphicsContext gc = new SwingGraphicsContext(g);

		renderController.renderShapes(gc, viewType, imageRect, page, shapes);
	}
	
	private synchronized void refreshFrontImage(Graphics g, Page page, List<Shape> shapes, PresentationParameter parameter) {
		if (frontImage == null)
			return;

		if (parameter.showGrid()) {
			GridConfiguration gridConfig = parameter.getGridConfiguration();

			GridShape gridShape = new GridShape();
			gridShape.setViewRatio(new Dimension2D(4.0, 3.0));
			gridShape.setColor(gridConfig.getColor());
			gridShape.setHorizontalLinesInterval(gridConfig.getHorizontalLinesInterval());
			gridShape.setHorizontalLinesVisible(gridConfig.getHorizontalLinesVisible());
			gridShape.setVerticalLinesInterval(gridConfig.getVerticalLinesInterval());
			gridShape.setVerticalLinesVisible(gridConfig.getVerticalLinesVisible());

			shapes.add(0, gridShape);
		}

		Graphics2D g2d = frontImage.createGraphics();
		refreshBackground(g2d, page, parameter);
		drawShapes(g2d, shapes);
		g2d.dispose();
		
		g.drawImage(frontImage, 0, 0, null);
	}
	
	private void refreshBackground(final Graphics2D g, Page page, PresentationParameter parameter) {
		g.drawImage(backImage, 0, 0, null);
		
		if (isWhiteboardSlide(page))
			drawBackground(g);
		
		if (parameter.isExtended())
			drawContentBorder(g, parameter);
		
		drawWatermark(g);
	}
	
	private void drawWatermark(Graphics2D g) {
		if (watermark != null && viewType == ViewType.Presentation) {
			AffineTransform t = g.getTransform();
			g.setTransform(new AffineTransform());

			int width = (int) imageRect.getWidth() / 5;
			float ratio = width / (float) watermark.getWidth();
			int height = (int) (watermark.getHeight() * ratio);

			Point wPos = getWatermarkPosition(watermark);
			int x = wPos.x;
			int y = wPos.y;

			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setColor(Color.WHITE);
			g.setBackground(Color.WHITE);
			g.fillRect(x, y, width, height);
			g.clearRect(x, y, width, height);
			g.drawImage(watermark, x, y, width, height, null);

			g.setTransform(t);
		}
	}

	private void drawContentBorder(Graphics2D g, PresentationParameter parameter) {
		Point2D scale = getScale(parameter);

		int width = (int) (imageRect.getWidth() * (scale.getX() / (imageRect.getWidth() / 4)));
		int height = (int) (imageRect.getHeight() * (scale.getY() / (imageRect.getHeight() / 3)));

		g.setColor(Color.GRAY);
		float dash[] = { 3.0f };
		g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
		g.drawRect(-2, -2, width + 4, height + 4);
	}

	private void drawBackground(Graphics2D g) {
		if (currentImage == null)
			return;
		
		int width = currentImage.getWidth();
		int height = currentImage.getHeight();
		
		org.lecturestudio.core.graphics.Color color = config.getWhiteboardConfig().getBackgroundColor();
		Color bgColor = ColorConverter.INSTANCE.to(color);
		
		g.setBackground(bgColor);
		g.clearRect(0, 0, width, height);
	}

	private boolean isWhiteboardSlide(Page page) {
		if (page == null)
			return false;
		
		Document doc = page.getDocument();
		return doc != null && doc.isWhiteboard();
	}
	
	public boolean adjustImageRect(Dimension size) {
		if (page == null) {
			return false;
		}

		final int width = (int) (size.width * deviceTransform.getScaleX());
		final int height = (int) (size.height * deviceTransform.getScaleY());

		PageMetrics metrics = page.getPageMetrics();

		size.width = (int) metrics.getWidth(height);
		size.height = (int) metrics.getHeight(width);

		if (size.width != imageRect.getWidth() || size.height != imageRect.getHeight()) {
			imageRect.setSize(size.width, size.height);

			frontImage = createImage(frontImage, size);
			currentImage = createImage(currentImage, size);
			bufferImage = createImage(bufferImage, size);

			bufferg2d = createGraphics(bufferg2d, bufferImage);

			return true;
		}
		
		return false;
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
	
	private Point getWatermarkPosition(BufferedImage watermark) {
		int width = (int) imageRect.getWidth() / 5;
		float ratio = width / (float) watermark.getWidth();
		int height = (int) (watermark.getHeight() * ratio);
		Position pos = config.getDisplayConfig().getIpPosition();
		int x = 0;
		int y = 0;

		switch (pos) {
			case TOP_LEFT:
				x = 0;
				y = 0;
				break;

			case TOP_CENTER:
				x = (int) ((imageRect.getWidth() - width) / 2);
				y = 0;
				break;

			case TOP_RIGHT:
				x = (int) (imageRect.getWidth() - width);
				y = 0;
				break;

			case CENTER_LEFT:
				x = 0;
				y = (int) ((imageRect.getHeight() - height) / 2);
				break;

			case CENTER_RIGHT:
				x = (int) (imageRect.getWidth() - width);
				y = (int) ((imageRect.getHeight() - height) / 2);
				break;

			case BOTTOM_LEFT:
				x = 0;
				y = (int) (imageRect.getHeight() - height);
				break;

			case BOTTOM_CENTER:
				x = (int) ((imageRect.getWidth() - width) / 2);
				y = (int) (imageRect.getHeight() - height);
				break;

			case BOTTOM_RIGHT:
				x = (int) (imageRect.getWidth() - width);
				y = (int) (imageRect.getHeight() - height);
				break;
		}

		return new Point(x, y);
	}
	
}