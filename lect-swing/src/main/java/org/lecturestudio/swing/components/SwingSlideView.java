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

import com.google.common.eventbus.Subscribe;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.ToolSelectionEvent;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditEvent.Type;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.render.RenderThread;
import org.lecturestudio.core.render.RenderThreadTask;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.SlideView;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.swing.renderer.ViewRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.isNull;

/**
 * SlideView class is responsible for viewing the slide images that are rendered
 * by a PDF adapter.
 * 
 * @author Alex
 */
public class SwingSlideView extends JComponent implements SlideView, PageEditedListener, ParameterChangeListener {

	private static final long serialVersionUID = -7415177296923113959L;
	
	private static final Logger LOG = LogManager.getLogger(SwingSlideView.class);

	private static Map<ViewType, RenderThread> executors = new HashMap<>();

	static {
		executors.put(ViewType.Presentation, new RenderThread());
		executors.put(ViewType.User, new RenderThread());
	}

	private ApplicationContext context;

	private RenderThread renderThread;

	private ViewRenderer renderer;

	private Page page;

	private ViewType viewType;
	
	private ToolType toolType;

	private final Point2D lastPan = new Point2D.Double();

	private RenderThreadTask renderPageTask = new PageTask();

	private RenderThreadTask renderForegroundTask = new ForegroundTask();

	private Font textBoxFont = new Font("Arial", Font.PLAIN, 24);
	private TeXFont teXBoxFont = new TeXFont(TeXFont.Type.SERIF, 24);

	private Color textBoxFontColor = Color.BLACK;

	private boolean showTextBoxes = false;
	
	private boolean seeking = false;
	
	
	public SwingSlideView(ViewType viewType, ApplicationContext context) {
		this.context = context;
		this.renderer = new ViewRenderer(viewType, context.getConfiguration());
		this.renderer.setDeviceTransform(new AffineTransform());
		this.renderThread = executors.get(viewType);

		if (!renderThread.started()) {
			try {
				renderThread.start();
			}
			catch (ExecutableException e) {
				LOG.error("Start render thread failed.", e);
			}
		}

		setLayout(null);
		setIgnoreRepaint(true);
		setFocusable(false);
		setViewType(viewType);

		initListeners();
		
		ApplicationBus.register(this);
	}

	public void setRenderController(RenderController renderController) {
		renderer.setRenderController(renderController);
	}

	@Override
	public void pageEdited(final PageEditEvent event) {
		if (!seeking) {
			renderThread.onTask(new EditTask(event));
		}
	}
	
	public void onSeeking(boolean seek) {
		seeking = seek;
		
		if (!seeking) {
			renderThread.onTask(renderForegroundTask);
		}
	}
	
	@Subscribe
	public void onEvent(ToolSelectionEvent event) {
		this.toolType = event.getToolType();
	}
	
	/**
	 * Adds a mouse selection listener which is notified when the user has
	 * clicked on the canvas.
	 * 
	 * @param l a MouseListener
	 */
	public void addSelectionListener(MouseListener l) {
		addMouseListener(l);
	}

	public void updateImageRect() {
        if (renderer != null) {
		    renderer.adjustImageRect(getBounds().getSize());
        }
	}
	
	/**
	 * Initializes listeners.
	 */
	protected void initListeners() {
		addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				Dimension size = getBounds().getSize();

				// Avoid rendering images multiple times if size has not changed.
				int w = (int) renderer.getImageRect().getWidth();
				int h = (int) renderer.getImageRect().getHeight();

				if (size.width < 1 || size.height < 1) {
					return;
				}

				if (w - size.width == 0 || h - size.height == 0) {
					return;
				}

				final GraphicsConfiguration graphicsConfig = getGraphicsConfiguration();

				if (isNull(graphicsConfig)) {
					return;
				}

				final AffineTransform transform = graphicsConfig.getDefaultTransform();

				renderer.setDeviceTransform(transform);
				renderer.adjustImageRect(size);
				renderer.renderPage(getPage(), size);
			}
		});
	}

	/**
	 * Removes the slide image from the view.
	 */
	private void removeSlideImage() {
		Dimension hostRect = getParent().getSize();
		setSize(hostRect.width, hostRect.height);

		renderer.removeSlideImage();
	}

	/**
	 * Sets the new page model.
	 * 
	 * @param page the new Page
	 */
	public void setPage(Page page) {
		if (this.page == page)
			return;

		if (this.page != null) {
			this.page.removePageEditedListener(this);
		}

		if (page == null) {
			this.page = null;
			removeSlideImage();
		}
		else {
			this.page = page;

			renderer.setPage(page);
            renderer.setParameter(context.getPagePropertyPropvider(getViewType()));

			page.addPageEditedListener(this);

			setBounds(getBounds());
			renderPage();
		}
	}

	/**
	 * Returns the current page.
	 * 
	 * @return the page
	 */
	public Page getPage() {
		return page;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		setAspectBounds(x, y, width, height);
	}

	@Override
	public void setBounds(Rectangle rect) {
		setAspectBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Sets the bounds of this widget based on the 4:3 aspect ratio.
	 * 
	 * @param x the new x coordinate
	 * @param y the new y coordinate
	 * @param width the new width
	 * @param height the new height
	 */
	private void setAspectBounds(int x, int y, int width, int height) {
		if (page == null || page.getDocument().isClosed()) {
			super.setBounds(x, y, width, height);
			return;
		}

		PageMetrics metrics = page.getPageMetrics();
		Dimension2D size = metrics.convert(width, height);

		super.setBounds(x, y, (int) size.getWidth(), (int) size.getHeight());
	}

	@Override
	public Page forPage() {
		return getPage();
	}

	@Override
	public void parameterChanged(final Page p, final PresentationParameter parameter) {
		if (parameter.isTranslation()) {
			if (!seeking) {
				double x = parameter.getTranslation().getX();
				double y = parameter.getTranslation().getY();

				double panX = lastPan.getX() - x;
				double panY = lastPan.getY() - y;

				lastPan.setLocation(x, y);

				renderThread.onTask(new TranslateTask(new Point2D.Double(panX, panY)));
			}
			return;
		}

		lastPan.setLocation(0, 0);

		renderPage();
	}
	
	/**
	 * Returns the current PresentationParameter of this view.
	 * 
	 * @return a PresentationParameter
	 */
	public PresentationParameter getCurrentParameter() {
		PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(getViewType());
		PresentationParameter param = ppProvider.getParameter(getPage());
		
		if (param == null) {
			param = new PresentationParameter(null, null);
		}
		
		return param;
	}

	/**
	 * Sets the new ViewType of this view. The parameter should be one of these:
	 * <li>Preview</li> <li>User</li> <li>Presentation</li>
	 * 
	 * @param viewType the new ViewType
	 */
	protected void setViewType(ViewType viewType) {
		if (this.viewType != viewType) {
			if (this.viewType != null) {
				PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(this.viewType);
				ppProvider.removeParameterChangeListener(this);
			}

			this.viewType = viewType;

			PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(getViewType());
			ppProvider.addParameterChangeListener(this);
		}
	}

	/**
	 * Returns the {@code ViewType} of this {@code SlideView}.
	 * 
	 * @return the view type.
	 */
	public ViewType getViewType() {
		return viewType;
	}

	public void refresh() {
		renderThread.onTask(renderForegroundTask);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		BufferedImage image = renderer.getImage();
		
		if (page == null || image == null) {
			return;
		}

		/*
		 * HiDPI scaling:
		 * Set to Identity transform, since the renderer scales the image
		 * to the appropriate size.
		 */

		Graphics2D g2d = (Graphics2D) g;
		final AffineTransform transform = g2d.getTransform();
		final AffineTransform imageTransform = new AffineTransform();
		imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

		g2d.setTransform(imageTransform);
		g2d.drawImage(image, 0, 0, null);
		g2d.setTransform(transform);
	}

	public void setWatermark(BufferedImage image) {
		renderer.setWatermark(image);
        
		repaint();
	}

	public Point2D getScale() {
		final AffineTransform transform = getGraphicsConfiguration().getDefaultTransform();

		Point2D scale = renderer.getScale(getCurrentParameter());
		scale.setLocation(scale.getX() / transform.getScaleX(), scale.getX() / transform.getScaleX());

		return scale;
	}
	
	public org.lecturestudio.core.geometry.Dimension2D getImageRect() {
		return renderer.getImageRect();
	}

	public void dispose() {
		renderer.dispose();

        page = null;
	}

	/**
	 * Updates the background image. This method is called when the background
	 * image need to be re-rendered.
	 */
	public void renderPage() {
		renderThread.onTask(renderPageTask);
	}


	private class PageTask implements RenderThreadTask {

		@Override
		public void render() throws Exception {
			if (page == null || getBounds().isEmpty()) {
				return;
			}

			renderer.renderPage(getPage(), getBounds().getSize());
		}

	}


	private class ForegroundTask implements RenderThreadTask {

		@Override
		public void render() throws Exception {
			renderer.renderForeground();
		}

	}


	private class TranslateTask implements RenderThreadTask {

		private final Point2D point;


		public TranslateTask(Point2D point) {
			this.point = point;
		}

		@Override
		public void render() throws Exception {
			if (point.getX() != 0 || point.getY() != 0) {
				renderer.renderPage(getPage(), getBounds().getSize(), point);
			}
			else {
				// Paint last shape permanently.
				renderer.renderForeground();
			}
		}
		
	}


	private class EditTask implements RenderThreadTask {

		private final PageEditEvent event;


		public EditTask(final PageEditEvent event) {
			this.event = event;
		}

		@Override
		public void render() throws Exception {
			final Shape shape = event.getShape();
			final PageEditEvent.Type type = event.getType();

			if (shape == null ||
				type == Type.CLEAR ||
				type == Type.SHAPES_ADDED ||
				type == Type.SHAPE_ADDED ||
				type == Type.SHAPE_REMOVED ||
				shape instanceof TextShape ||
				shape instanceof TeXShape ||
				shape.isSelected()) {

				renderer.renderForeground();
			}
			else {
				final Page page = event.getPage();
				final org.lecturestudio.core.geometry.Rectangle2D clipRect = event.getDirtyArea();

				renderer.render(page, shape, clipRect);
			}
		}

	}

}