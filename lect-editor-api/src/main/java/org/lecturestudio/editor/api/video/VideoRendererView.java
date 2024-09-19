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

package org.lecturestudio.editor.api.video;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.listener.ShapeListener;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.SlideView;
import org.lecturestudio.core.view.ViewType;

/**
 *
 * 
 * @author Alex Andres
 */
public class VideoRendererView implements SlideView, ShapeListener, ParameterChangeListener {

	private static final ViewType viewType = ViewType.User;

	private final int width;

	private final int height;

	private final BufferedImage backImage;

	private final BufferedImage image;

	private final AffineTransform pageTransform;

	private final AtomicBoolean pageChanged;

	private Dimension2D size;

	private RenderController renderController;

	private Page page;


	public VideoRendererView(ApplicationContext context, Dimension2D pictureSize) {
		width = (int) pictureSize.getWidth();
		height = (int) pictureSize.getHeight();
		pageTransform = new AffineTransform();
		pageChanged = new AtomicBoolean();

		GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gConf = gEnv.getDefaultScreenDevice().getDefaultConfiguration();

		backImage = gConf.createCompatibleImage(width, height);
		backImage.setAccelerationPriority(1.0f);

		image = gConf.createCompatibleImage(width, height);
		image.setAccelerationPriority(1.0f);

		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(viewType);
		ppProvider.addParameterChangeListener(this);
	}

	public Dimension2D getImageSize() {
		return new Dimension2D(width, height);
	}

	public void setRenderController(RenderController renderController) {
		this.renderController = renderController;
	}

	public void renderFrameImage(BufferedImage image) {
		Graphics2D g = backImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		setPageChanged();
	}

	public void renderPageImage() {
		updateBackImage();
	}

	public BufferedImage renderCurrentFrame() {
		repaintView();

		return image;
	}

	public void dispose() {
		backImage.flush();
		image.flush();
	}

	/**
	 * Sets the new page model.
	 * 
	 * @param page the new Page
	 */
	@Override
	public void setPage(Page page) {
		if (this.page == page) {
			return;
		}
		if (nonNull(this.page)) {
			this.page.removeShapeListener(this);
		}

		this.page = page;

		if (nonNull(page)) {
			page.addShapeListener(this);

			updateBackImage();
		}
	}

	@Override
	public Page forPage() {
		return page;
	}

	@Override
	public void parameterChanged(Page p, PresentationParameter parameter) {
		updateBackImage();
	}

	@Override
	public void shapePainted(ShapePaintEvent event) {
		setPageChanged();
	}

	@Override
	public void shapeModified(ShapeModifyEvent event) {
		setPageChanged();
	}

	private void setPageChanged() {
		pageChanged.set(true);
	}

	private void repaintView() {
		if (pageChanged.compareAndSet(true, false)) {
			Graphics2D g = image.createGraphics();
			drawAllShapes(g);
			g.dispose();
		}
	}

	private void drawAllShapes(Graphics2D g) {
		g.drawImage(backImage, 0, 0, null);
		g.scale(pageTransform.getScaleX(), pageTransform.getScaleY());

		SwingGraphicsContext gc = new SwingGraphicsContext(g);

		renderController.renderShapes(gc, viewType, size, page, page.getShapes());
	}

	private void updateBackImage() {
		if (isNull(page)) {
			return;
		}

		updatePageTransform();

		renderController.renderPage(backImage, page, viewType);

		setPageChanged();
		repaintView();
	}

	private void updatePageTransform() {
		PageMetrics metrics = page.getPageMetrics();
		size = metrics.convert(width, height);

		pageTransform.setToScale(width / size.getWidth(), height / size.getHeight());
	}
}
