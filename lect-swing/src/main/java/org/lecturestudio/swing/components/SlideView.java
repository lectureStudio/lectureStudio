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

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.listener.ShapeListener;
import org.lecturestudio.core.model.shape.ScreenCaptureShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.swing.SwingGraphicsContext;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.core.view.ViewType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * SlideView class is responsible for viewing the slide images that are rendered
 * by a PDF adapter.
 * 
 * @author Alex
 */
public class SlideView extends JComponent implements org.lecturestudio.core.view.SlideView, ShapeListener, ParameterChangeListener {

	private static final long serialVersionUID = -7415177296923113959L;

	private SlideRenderer renderer;

	/** Specifies how the view should be treated by a renderer. */
	private ViewType viewType;

	private Page page;

	private final Rectangle canvasBounds = new Rectangle();

	private AffineTransform pageTransform = new AffineTransform();

	private PresentationParameter pParameter;

	private final List<SlideViewOverlay> overlays = new ArrayList<>();

	private final List<PageObjectView<?>> objectViews = new ArrayList<>();

	private Position alignment;

	private JComponent surfaceView;


	public SlideView() {
		super();

		initialize();
	}

	@Override
	public Page forPage() {
		return getPage();
	}

	@Override
	public void parameterChanged(Page page, PresentationParameter parameter) {
		if (isNull(parameter)) {
			return;
		}

		pParameter = parameter;
		renderer.setPresentationParameter(parameter);

		updateViewTransform();

		renderPage();
	}

	public final void setAlignment(Position position) {
		if (alignment != position) {
			alignment = position;
		}
	}

	public final Position getAlignment() {
		return alignment == null ? Position.TOP_LEFT : alignment;
	}

	public Rectangle getCanvasBounds() {
		return canvasBounds;
	}

	/**
	 * Returns the current page.
	 *
	 * @return the page
	 */
	public Page getPage() {
		return page;
	}

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

			renderer.setPresentationParameter(pParameter);
			renderer.setPage(page);

			setBounds(getBounds());
			renderPage();
		}
	}

	public AffineTransform getPageTransform() {
		return pageTransform;
	}

	public void setPageTransform(AffineTransform transform) {
		if (pageTransform == transform) {
			return;
		}

		AffineTransform oldTransform = pageTransform;
		pageTransform = transform;

		for (Component c : surfaceView.getComponents()) {
			if (c instanceof PageObject) {
				PageObject<?> pageObject = (PageObject<?>) c;
				pageObject.setPageTransform(pageTransform);
			}
		}

		renderer.setPageTransform(transform);

		firePropertyChange("transform", oldTransform, pageTransform);
	}

	private RenderController renderController;

	private final Dimension2D imageRect = new Dimension2D(0, 0);


	public void setPageRenderer(RenderController pageRenderer) {
		renderController = pageRenderer;

		renderer.setRenderController(pageRenderer);
	}

	public void addPageObjectView(PageObjectView<?> objectView) {
		if (isNull(objectView)) {
			return;
		}
		if (PageObject.class.isAssignableFrom(objectView.getClass())) {
			PageObject<?> pageObject = (PageObject<?>) objectView;
			pageObject.setPageTransform(getPageTransform());
		}
		if (Component.class.isAssignableFrom(objectView.getClass())) {
			getPageObjectViews().add(objectView);

			surfaceView.add((Component) objectView);
			surfaceView.revalidate();
			surfaceView.repaint();
		}
	}

	public void addPageObjectViews(List<PageObjectView<?>> objectViewList) {
		if (isNull(objectViewList) || objectViewList.isEmpty()) {
			return;
		}

		for (PageObjectView<?> objectView : objectViewList) {
			addPageObjectView(objectView);
		}
	}

	public void removePageObjectView(PageObjectView<?> objectView) {
		if (isNull(objectView)) {
			return;
		}
		if (Component.class.isAssignableFrom(objectView.getClass())) {
			getPageObjectViews().remove(objectView);

			surfaceView.remove((Component) objectView);
			surfaceView.repaint();
		}
	}

	public void removeAllPageObjectViews() {
		getPageObjectViews().clear();

		surfaceView.removeAll();
		surfaceView.repaint();
	}

	public List<PageObjectView<?>> getPageObjectViews() {
		return objectViews;
	}

	public List<SlideViewOverlay> getOverlays() {
		return overlays;
	}

	public void addOverlay(SlideViewOverlay overlay) {
		if (isNull(overlay)) {
			return;
		}
		if (Component.class.isAssignableFrom(overlay.getClass())) {
			getOverlays().add(overlay);
			surfaceView.add((Component) overlay);

			layoutOverlay(overlay);
			surfaceView.repaint();
		}
	}

	public void removeOverlay(SlideViewOverlay overlay) {
		if (isNull(overlay)) {
			return;
		}
		if (Component.class.isAssignableFrom(overlay.getClass())) {
			getOverlays().remove(overlay);

			surfaceView.remove((Component) overlay);
			surfaceView.repaint();
		}
	}

	public void setSlideLocation(int x, int y) {
		surfaceView.setLocation(x, y);
	}

	/**
	 * Describes how a page should be displayed on this SlideView.
	 */
	public PresentationParameter getPresentationParameter() {
		return pParameter;
	}

	/**
	 * Sets the new ViewType of this view. The parameter should be one of
	 * these:
	 * <li>Preview</li>
	 * <li>User</li>
	 * <li>Presentation</li>
	 *
	 * @param type the new ViewType.
	 */
	public void setViewType(ViewType type) {
		viewType = type;
	}

	/**
	 * Returns the {@code ViewType} of this {@code SlideView}.
	 *
	 * @return the view type.
	 */
	public ViewType getViewType() {
		return isNull(viewType) ? ViewType.User : viewType;
	}

	private void updateViewTransform() {
		if (isNull(pParameter)) {
			return;
		}

		Rectangle2D pageRect = pParameter.getPageRect();
		Rectangle canvasBounds = getCanvasBounds();

		double tx = canvasBounds.getMinX() / canvasBounds.getWidth() * pageRect.getWidth() + pageRect.getX();
		double ty = canvasBounds.getMinY() / canvasBounds.getHeight() * pageRect.getHeight() + pageRect.getY();
		double s = canvasBounds.getWidth() / pageRect.getWidth();

		setPageTransform(new AffineTransform(s, 0, 0, s, tx, ty));
	}

	private void onBoundsChanged(Rectangle bounds) {
		if (isNull(page)) {
			return;
		}

		setAspectBounds(bounds.x, bounds.y, bounds.width, bounds.height);

		for (SlideViewOverlay overlay : overlays) {
			layoutOverlay(overlay);
		}

		renderPage();
	}

	private Dimension2D getViewSize(Rectangle bounds) {
		PageMetrics metrics = page.getPageMetrics();
		Insets insets = getInsets();

		final double width = bounds.getWidth() - (insets.left + insets.right);
		final double height = bounds.getHeight() - (insets.top + insets.bottom);

		return metrics.convert(width, height);
	}

	private void setAspectBounds(int x, int y, int width, int height) {
		if (isNull(page) || page.getDocument().isClosed()) {
			return;
		}

		Dimension2D size = getViewSize(new Rectangle(x, y, width, height));

		if (size.getWidth() == canvasBounds.getWidth() || size.getHeight() == canvasBounds.getHeight()) {
			return;
		}

		canvasBounds.setRect(getX(), getY(), size.getWidth(), size.getHeight());

		renderer.resizeBuffer(size);

		surfaceView.setSize((int) size.getWidth(), (int) size.getHeight());
		imageRect.setSize(size.getWidth(), size.getHeight());

		updateViewTransform();
	}

	private void initialize() {
		setLayout(null);
		setIgnoreRepaint(true);
		setFocusable(false);

		surfaceView = new JComponent() {

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (isNull(page) || isNull(pParameter)) {
					return;
				}

				Graphics2D g2d = (Graphics2D) g;

				if (nonNull(backImage)) {
					AffineTransform transform = g2d.getTransform();
					AffineTransform imageTransform = new AffineTransform();
					imageTransform.translate(transform.getTranslateX(), transform.getTranslateY());

					BufferedImage renderImage = backImage;

					if (!OsInfo.isWindows() && page.hasShapes()) {
						Graphics2D fg2d = frontImage.createGraphics();
						fg2d.drawImage(backImage, 0, 0, null);

						SwingGraphicsContext gc = new SwingGraphicsContext(fg2d);
						gc.scale(transform.getScaleX(), transform.getScaleY());

						renderController.renderShapes(gc, getViewType(), imageRect, page, page.getShapes());

						fg2d.dispose();

						renderImage = frontImage;
					}

					g2d.setTransform(imageTransform);
					g2d.drawImage(renderImage, 0, 0, this);
					g2d.setTransform(transform);
				}

				if (OsInfo.isWindows() && page.hasShapes()) {
					SwingGraphicsContext gc = new SwingGraphicsContext(g2d);

					List<Shape> shapes = page.getShapes().stream().filter(shape -> !(shape instanceof ScreenCaptureShape)).collect(Collectors.toList());

					renderController.renderShapes(gc, getViewType(), imageRect, page, shapes);
				}
			}
		};
		surfaceView.setLayout(null);
		surfaceView.setIgnoreRepaint(true);

		add(surfaceView);

		renderer = new SlideRenderer(getViewType());

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				Rectangle bounds = getBounds();

				if (bounds.width < 1 || bounds.height < 1) {
					return;
				}

				GraphicsConfiguration gc = getGraphicsConfiguration();

				if (nonNull(gc)) {
					renderer.setDeviceTransform(gc.getDefaultTransform());
				}

				updateViewTransform();
				onBoundsChanged(bounds);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				updateViewTransform();
			}
		});
	}

	@Override
	public void shapePainted(ShapePaintEvent event) {
		Rectangle2D clip = event.getClipRect();

		if (nonNull(clip)) {
			repaintView(clip);
		}
		else {
			repaintView();
		}
	}

	@Override
	public void shapeModified(ShapeModifyEvent event) {
		Iterator<Shape> shapes = event.getShapes().iterator();
		Rectangle2D clip = null;

		if (shapes.hasNext()) {
			clip = shapes.next().getBounds().clone();
		}

		while (shapes.hasNext()) {
			clip.union(shapes.next().getBounds());
		}

		if (nonNull(clip)) {
			repaintView();
		}
		else {
			repaintView();
		}
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
		if (isNull(page) || getBounds().isEmpty()) {
			return;
		}

		executor.execute(() -> {
			updateBackImage();

			repaintView();
		});
	}

	private void repaintView() {
		repaintView(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
	}

	private void repaintView(int x, int y, int w, int h) {
		surfaceView.repaint(x, y, w, h);
//		if (SwingUtilities.isEventDispatchThread()) {
//			surfaceView.paintImmediately(x, y, w, h);
//		}
//		else {
//			try {
//				SwingUtilities.invokeAndWait(() -> {
//					surfaceView.paintImmediately(x, y, w, h);
//				});
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

	private void repaintView(Rectangle2D rect) {
		Rectangle2D pageRect = pParameter.getViewRect();
		Rectangle canvasBounds = getCanvasBounds();

		double sx = canvasBounds.getWidth() / pageRect.getWidth();
		double tx = pageRect.getX() * sx;
		double ty = pageRect.getY() * sx;

		int x = (int) (rect.getX() * pageTransform.getScaleX() - tx) - 5;
		int y = (int) (rect.getY() * pageTransform.getScaleY() - ty) - 5;
		int w = (int) (rect.getWidth() * pageTransform.getScaleX()) + 10;
		int h = (int) (rect.getHeight() * pageTransform.getScaleY()) + 10;

		repaintView(x, y, w, h);
	}



	private final BlockingQueue<Runnable> deque = new LinkedBlockingDeque<>(5);
	private final ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L,
			TimeUnit.MILLISECONDS, deque,
			new ThreadPoolExecutor.DiscardPolicy());

	private BufferedImage backImage;

	private BufferedImage frontImage;

	private BufferedImage createBackImage(BufferedImage reference, int width, int height) {
		if (reference != null) {
			if (width == reference.getWidth() && height == reference.getHeight()) {
				return reference;
			}

			reference.flush();
		}

		reference = (BufferedImage) surfaceView.createImage(width, height);
		reference.setAccelerationPriority(1);

		return reference;
	}

	private synchronized void updateBackImage() {
		if (isNull(getGraphicsConfiguration())) {
			return;
		}

		AffineTransform transform = getGraphicsConfiguration().getDefaultTransform();
		int width = (int) (canvasBounds.width * transform.getScaleX());
		int height = (int) (canvasBounds.height * transform.getScaleY());

		backImage = createBackImage(backImage, width, height);

		if (!OsInfo.isWindows()) {
			frontImage = createBackImage(frontImage, width, height);
		}

		if (isScreenCaptureSlide(page)) {

			Graphics2D g2d = backImage.createGraphics();

			// Clear background.
			g2d.setBackground(Color.white);
			g2d.clearRect(0, 0, width, height);

			List<Shape> shapes = page.getShapes().stream().filter(shape -> shape instanceof ScreenCaptureShape).collect(Collectors.toList());
			renderController.renderShapes(new SwingGraphicsContext(g2d), getViewType(), new Dimension2D(width, height), page, shapes);

			g2d.dispose();

//			ScreenCaptureShape screenCaptureShape = (ScreenCaptureShape) page.getShapes().stream().filter(shape -> shape instanceof ScreenCaptureShape).findFirst().orElse(null);
//			if (screenCaptureShape != null) {
//				setBackImage(screenCaptureShape.getFrame());
//			}
		}
		else {
			renderController.renderPage(backImage, page, getViewType());
		}
	}

	private boolean isScreenCaptureSlide(Page page) {
		return page.getShapes().stream().anyMatch(shape -> shape instanceof ScreenCaptureShape);
	}

	private void layoutOverlay(SlideViewOverlay overlay) {
		Component overlayComponent = (Component) overlay;
		Dimension overlaySize = overlayComponent.getPreferredSize();

		int overlayWidth = overlaySize.width;
		int overlayHeight = overlaySize.height;

		int width = getCanvasBounds().width;
		int height = getCanvasBounds().height;

		int x = 0;
		int y = 0;

		switch (overlay.getPosition()) {
			case TOP_LEFT:
				x = 0;
				y = 0;
				break;

			case TOP_CENTER:
				x = (width - overlayWidth) / 2;
				y = 0;
				break;

			case TOP_RIGHT:
				x = width - overlayWidth;
				y = 0;
				break;

			case CENTER_LEFT:
				x = 0;
				y = (height - overlayHeight) / 2;
				break;

			case CENTER_RIGHT:
				x = width - overlayWidth;
				y = (height - overlayHeight) / 2;
				break;

			case BOTTOM_LEFT:
				x = 0;
				y = height - overlayHeight;
				break;

			case BOTTOM_CENTER:
				x = (width - overlayWidth) / 2;
				y = height - overlayHeight;
				break;

			case BOTTOM_RIGHT:
				x = width - overlayWidth;
				y = height - overlayHeight;
				break;
		}

		overlayComponent.setSize(overlaySize);
		overlayComponent.setLocation(x, y);
	}
}