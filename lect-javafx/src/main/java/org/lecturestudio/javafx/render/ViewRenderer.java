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

package org.lecturestudio.javafx.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import org.lecturestudio.core.app.configuration.WhiteboardConfiguration;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
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
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.swing.converter.RectangleConverter;

import static java.util.Objects.*;

public class ViewRenderer {

	private static final GraphicsEnvironment GE = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private static final GraphicsConfiguration GC = GE.getDefaultScreenDevice().getDefaultConfiguration();

	private final Dimension2D imageRect = new Dimension2D(0, 0);

	private final ViewType viewType;

	private final Java2DFrameConverter frameConverter = new Java2DFrameConverter();

	private FFmpegFrameFilter frameFilter;

	private Frame videoFrame;

	private Page page;

	private PresentationParameter parameter;

	private BufferedImage currentImage;
	private BufferedImage backImage;
	private BufferedImage frontImage;

	private BufferedImage bufferImage;
	private Graphics2D bufferg2d;

	private Shape lastShape;

	private RenderController renderController;


	public ViewRenderer(ViewType viewType) {
		this.viewType = viewType;
	}

	public void setRenderController(RenderController renderController) {
		this.renderController = renderController;
	}

	public BufferedImage getImage() {
		return bufferImage;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public void setParameter(PresentationParameter parameter) {
		this.parameter = parameter;
	}

	public synchronized void renderPage(Page page, Dimension size) {
		if (isNull(page) || page.getDocument().isClosed()) {
			return;
		}

		// A new page has been focused, clear video frame.
		disposeFrame();

		updateBackImage(page, size);

		renderForeground();
	}

	private synchronized void disposeFrame() {
		if (nonNull(videoFrame)) {
			videoFrame.close();
			videoFrame = null;
		}
	}

	public synchronized void renderFrame(Frame frame) throws Exception {
		if (isNull(frame) || isNull(frame.type)) {
			disposeFrame();
			renderForeground();
			return;
		}

		final int frameWidth = frame.imageWidth;
		final int frameHeight = frame.imageHeight;
		final int targetWidth = currentImage.getWidth();
		final int targetHeight = currentImage.getHeight();

		if (isNull(frameFilter)
				|| frameFilter.getImageWidth() != targetWidth
				|| frameFilter.getImageHeight() != targetHeight) {
			destroyFrameFilter();
			createFrameFilter(targetWidth, targetHeight, frameWidth, frameHeight);
		}

		// TODO: handle this in connection with painted annotations
//		disposeFrame();

		videoFrame = frame.clone();

		Graphics2D g2d = frontImage.createGraphics();
		refreshBackground(g2d, page, parameter);
		g2d.dispose();

		renderForeground();
	}

	public synchronized void renderForeground() {
		if (page == null || parameter == null || currentImage == null) {
			return;
		}

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

		bufferg2d.setClip(0, 0, bufferImage.getWidth(), bufferImage.getHeight());
		bufferg2d.drawImage(currentImage, 0, 0, null);
	}

	public synchronized void render(Page page, Shape shape, Rectangle clip) {
		if (page == null || frontImage == null)
			return;

		Graphics2D g = currentImage.createGraphics();

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

		if (clip != null) {
			bufferg2d.setClip(clip.x, clip.y, clip.width, clip.height);
		}
		else {
			bufferg2d.setClip(0, 0, bufferImage.getWidth(), bufferImage.getHeight());
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

		return calculateScale(imageRect, RectangleConverter.INSTANCE.to(pageRect));
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

	/**
	 * Updates the background image. This method is called when the background
	 * image needs to be re-rendered.
	 */
	private synchronized void updateBackImage(Page page, Dimension size) {
		if (page == null) {
			adjustImageRect(size);
			return;
		}

		if (imageRect.getWidth() <= 0 || imageRect.getHeight() <= 0) {
			if (!adjustImageRect(size)) {
				return;
			}
		}

		size = new Dimension((int)imageRect.getWidth(), (int)imageRect.getHeight());

		backImage = createBackImage(backImage, size);

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
		refreshBackground(g2d, page, parameter);
		drawShapes(g2d, shapes);
		g2d.dispose();

		g.drawImage(frontImage, 0, 0, null);
	}

	private void refreshBackground(final Graphics2D g, Page page, PresentationParameter parameter) {
		if (nonNull(videoFrame)) {
			drawVideoFrame(g, videoFrame);
		}
		else {
			g.drawImage(backImage, 0, 0, null);
		}

		if (isWhiteboardSlide(page)) {
			drawBackground(g);
		}
		if (parameter.isExtended()) {
			drawContentBorder(g, parameter);
		}
	}

	private void drawVideoFrame(final Graphics2D g, Frame frame) {
		try {
			frameFilter.push(frame);
			frame = frameFilter.pull();
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		BufferedImage converted = frameConverter.convert(frame);

		frame.close();

		final int x = (currentImage.getWidth() - converted.getWidth()) / 2;
		final int y = (currentImage.getHeight() - converted.getHeight()) / 2;

//		if (converted.getWidth() != targetWidth || converted.getHeight() != targetHeight) {
//			bufferg2d.setPaint(Color.BLACK);
//			bufferg2d.fillRect(0, 0, targetWidth, targetHeight);
//		}

		g.drawImage(converted, x, y, null);
	}

	private void drawContentBorder(Graphics2D g, PresentationParameter parameter) {
		Point2D scale = getScale(parameter);

		int width = (int) (imageRect.getWidth() * (scale.getX() / (imageRect.getWidth() / 4)));
		int height = (int) (imageRect.getHeight() * (scale.getY() / (imageRect.getHeight() / 3)));

		g.setColor(Color.GRAY);
		float[] dash = { 3.0f };
		g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
		g.drawRect(-2, -2, width + 4, height + 4);
	}

	private void drawBackground(Graphics2D g) {
		if (currentImage == null)
			return;

		int width = currentImage.getWidth();
		int height = currentImage.getHeight();

		g.setBackground(Color.white);
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

	private void createFrameFilter(int width, int height, int frameWidth, int frameHeight) throws Exception {
		String scale = String.format("scale=%dx%d", width, height);

		frameFilter = new FFmpegFrameFilter(scale, frameWidth, frameHeight);
		frameFilter.start();
	}

	private void destroyFrameFilter() throws Exception {
		if (nonNull(frameFilter)) {
			frameFilter.stop();
			frameFilter.release();
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

		reference = GC.createCompatibleImage(size.width, size.height);
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

		reference = GC.createCompatibleImage(size.width, size.height);
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