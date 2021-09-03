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

package org.lecturestudio.javafx.control;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.image.*;
import javafx.stage.Screen;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditEvent.Type;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.render.RenderThread;
import org.lecturestudio.core.render.RenderThreadTask;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.javafx.beans.converter.HPositionConverter;
import org.lecturestudio.javafx.beans.converter.VPositionConverter;
import org.lecturestudio.javafx.render.ViewRenderer;
import org.lecturestudio.javafx.util.FxUtils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class SlideViewSkin extends SkinBase<SlideView> {

	private static final Map<ViewType, RenderThread> executors = new HashMap<>();

	static {
		executors.put(ViewType.Preview, new RenderThread());
		executors.put(ViewType.User, new RenderThread());
	}

	private RenderThread renderThread;

	private final Dimension2D viewSize = new Dimension2D();

	private final ChangeListener<Bounds> boundsListener = (observable, oldValue, newValue) -> {
		if (newValue.getWidth() < 1 || newValue.getHeight() < 1) {
			return;
		}

		onBoundsChanged(newValue);
	};

	private final ChangeListener<Page> pageListener = (observable, oldPage, newPage) -> {
		onPageChanged(oldPage, newPage);
	};

	private final ChangeListener<PresentationParameter> presentationListener = (observable, oldParam, newParam) -> {
		onPresentationChanged(newParam);
	};

	private final PageEditedListener pageEditedListener = this::onPageEdited;

	private PixelBuffer<IntBuffer> pixelBuffer;

	private ImageView imageView;

	/** FX frame buffer image. */
	private WritableImage fxBufferImage;

	private BufferedImage slideImage;
	private BufferedImage screenCaptureImage;

	private ViewRenderer renderer;

	private final RenderThreadTask renderPageTask = new PageRenderTask();


	protected SlideViewSkin(SlideView control, ReadOnlyObjectWrapper<Bounds> canvasBounds) {
		super(control);

		initLayout(control, canvasBounds);
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
		SlideView control = getSkinnable();

		control.layoutBoundsProperty().removeListener(boundsListener);
		control.pageProperty().removeListener(pageListener);
		control.presentationParameterProperty().removeListener(presentationListener);

		// unregisterChangeListeners(control.seekingProperty());

		Page page = control.getPage();

		if (nonNull(page)) {
			page.removePageEditedListener(pageEditedListener);
		}

		super.dispose();
	}

	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return 10;
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return 10;
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return leftInset + viewSize.getWidth() + rightInset;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		return topInset + viewSize.getHeight() + bottomInset;
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
		final SlideView slideView = getSkinnable();
		final HPos hPos = getHPos(slideView.getAlignment());
		final VPos vPos = getVPos(slideView.getAlignment());

		layoutInArea(imageView, contentX, contentY, contentWidth, contentHeight, -1, hPos, vPos);

		final double overlayX = imageView.getLayoutX();
		final double overlayY = imageView.getLayoutY();
		final double overlayWidth = viewSize.getWidth();
		final double overlayHeight = viewSize.getHeight();

		for (SlideViewOverlay overlay : slideView.getOverlays()) {
			Node overlayNode = (Node) overlay;
			HPos ohPos = HPositionConverter.INSTANCE.to(overlay.getPosition());
			VPos ovPos = VPositionConverter.INSTANCE.to(overlay.getPosition());

			layoutInArea(overlayNode, overlayX, overlayY, overlayWidth, overlayHeight, -1, ohPos, ovPos);
		}
	}

	public void renderScreenCaptureFrame(BufferedImage frame) {
		screenCaptureImage = frame;
		renderThread.onTask(new FrameRenderTask(frame));
	}

	private void initLayout(SlideView control, ReadOnlyObjectWrapper<Bounds> canvasBounds) {
		renderThread = executors.get(control.getViewType());

		Screen primary = Screen.getPrimary();

		renderer = new ViewRenderer(control.getViewType());
		renderer.setDeviceTransform(AffineTransform.getScaleInstance(
				primary.getOutputScaleX(),
				primary.getOutputScaleY()
		));

		if (isNull(renderThread)) {
			renderThread = new RenderThread();
		}
		if (!renderThread.started()) {
			try {
				renderThread.start();
			}
			catch (ExecutableException e) {
				e.printStackTrace();
				//LOG.error("Start render thread failed.", e);
			}
		}

		RenderController slideRenderer = control.getPageRenderer();

		renderer.setRenderController(slideRenderer);
		renderer.setParameter(control.getPresentationParameter());
		renderer.setPage(control.getPage());

		imageView = new ImageView();
		imageView.setSmooth(false);
		imageView.setPreserveRatio(true);
		imageView.setMouseTransparent(true);

		control.layoutBoundsProperty().addListener(boundsListener);
		control.pageProperty().addListener(pageListener);
		control.presentationParameterProperty().addListener(presentationListener);

		Page page = control.getPage();

		if (nonNull(page)) {
			page.addPageEditedListener(pageEditedListener);
		}

		canvasBounds.bind(imageView.boundsInParentProperty());

		control.getPageObjectViews().addListener((ListChangeListener<PageObjectView<?>>) change -> {
			Platform.runLater(() -> {
				while (change.next()) {
					if (change.wasAdded()) {
						for (PageObjectView<?> objectViewNode : change.getAddedSubList()) {
							getChildren().add((Node) objectViewNode);
						}
					}
					else if (change.wasRemoved()) {
						for (PageObjectView<?> objectViewNode : change.getRemoved()) {
							getChildren().remove(objectViewNode);
						}
					}
				}
			});
		});

		control.getOverlays().addListener((ListChangeListener<SlideViewOverlay>) change -> {
			Platform.runLater(() -> {
				while (change.next()) {
					if (change.wasAdded()) {
						for (SlideViewOverlay overlay : change.getAddedSubList()) {
							Node overlayNode = (Node) overlay;
							getChildren().add(overlayNode);
						}
					}
					else if (change.wasRemoved()) {
						for (SlideViewOverlay overlay : change.getRemoved()) {
							Node overlayNode = (Node) overlay;
							getChildren().remove(overlayNode);
						}
					}
				}
			});
		});

		control.focusedProperty().addListener(observable -> {
			// Focus page object views.
			for (PageObjectView<?> objectView : control.getPageObjectViews()) {
				if (objectView.getFocus()) {
					Node nodeView = (Node) objectView;
					nodeView.requestFocus();
					return;
				}
			}
		});

		getChildren().addAll(imageView);

		for (PageObjectView<?> objectView : control.getPageObjectViews()) {
			getChildren().add((Node) objectView);
		}

		for (SlideViewOverlay overlay : control.getOverlays()) {
			Node overlayNode = (Node) overlay;
			getChildren().add(overlayNode);
		}
	}

	private Dimension2D getViewSize(Bounds bounds) {
		PageMetrics metrics = getSkinnable().getPage().getPageMetrics();

		final double width = snapSizeX(bounds.getWidth()) - (snappedLeftInset() + snappedRightInset());
		final double height = snapSizeY(bounds.getHeight()) - (snappedTopInset() + snappedBottomInset());

		return metrics.convert(width, height);
	}

	private void resizeBuffer(Dimension2D size) {
		if (size.getWidth() < 1 || size.getHeight() < 1) {
			return;
		}

		Screen primary = Screen.getPrimary();

		final int imageWidth = (int) (size.getWidth() * primary.getOutputScaleX());
		final int imageHeight = (int) (size.getHeight() * primary.getOutputScaleY());

		renderer.adjustImageRect(new Dimension(imageWidth, imageHeight));

		if (isNull(fxBufferImage) || imageWidth != fxBufferImage.getWidth() || imageHeight != fxBufferImage.getHeight()) {
			if (screenCaptureImage != null) {
				renderScreenCaptureFrame(screenCaptureImage);
			}
			else {
				slideImage = createImage(slideImage, imageWidth, imageHeight);
				int[] viewBuffer = ((DataBufferInt) slideImage.getRaster().getDataBuffer()).getData();

				IntBuffer byteBuffer = IntBuffer.wrap(viewBuffer);
				PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();

				pixelBuffer = new PixelBuffer<>(imageWidth, imageHeight, byteBuffer, pixelFormat);
				fxBufferImage = new WritableImage(pixelBuffer);

				imageView.setImage(fxBufferImage);
			}
		}
	}

	private BufferedImage createImage(BufferedImage imageRef, int width, int height) {
		if (imageRef != null) {
			if (width == imageRef.getWidth() && height == imageRef.getHeight()) {
				return imageRef;
			}

			imageRef.flush();
			imageRef = null;
		}

		ColorModel model = new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				32,
				0x00ff0000,		// Red
				0x0000ff00,		// Green
				0x000000ff,		// Blue
				0xff000000,		// Alpha
				true,
				DataBuffer.TYPE_INT
		);

		WritableRaster raster = model.createCompatibleWritableRaster(width, height);

		imageRef = new BufferedImage(model, raster, model.isAlphaPremultiplied(), null);
		imageRef.setAccelerationPriority(1);

		return imageRef;
	}

	private void setBounds(Bounds bounds) {
		Dimension2D size = getViewSize(bounds);

		resizeBuffer(size);

		viewSize.setSize(size.getWidth(), size.getHeight());

		imageView.setFitWidth((int) size.getWidth());
	}

	private void onBoundsChanged(Bounds bounds) {
		Page page = getSkinnable().getPage();

		if (isNull(page)) {
			return;
		}

		setBounds(bounds);

		FxUtils.invoke(() -> {
			renderer.renderPage(getSkinnable().getPage(), new Dimension(
					(int) viewSize.getWidth(), (int) viewSize.getHeight()));

			updateBuffer(null, renderer.getImage());
		});
	}

	private void onPageChanged(Page oldPage, Page newPage) {
		if (nonNull(oldPage)) {
			oldPage.removePageEditedListener(pageEditedListener);
		}
		if (nonNull(newPage)) {
			newPage.addPageEditedListener(pageEditedListener);
		}

		renderer.setPage(newPage);

		renderThread.onTask(renderPageTask);
	}

	private void onPageEdited(final PageEditEvent event) {
		renderThread.onTask(new ShapeRenderTask(event));
	}

	private void onPresentationChanged(PresentationParameter newParam) {
		if (isNull(newParam)) {
			return;
		}

		renderer.setParameter(newParam);

		renderThread.onTask(renderPageTask);
	}

	private void updateBuffer(final Rectangle clip, BufferedImage image) {
		final Graphics2D g2d = slideImage.createGraphics();
		if (nonNull(clip)) {
			g2d.setClip(clip.x, clip.y, clip.width, clip.height);
		}

		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		Runnable fxRunnable = () -> {
			pixelBuffer.updateBuffer(pixelBuffer -> {
				if (nonNull(clip) && !clip.isEmpty()) {
					return new javafx.geometry.Rectangle2D(clip.x, clip.y, clip.width, clip.height);
				}
				return null;
			});
		};

		FxUtils.invoke(fxRunnable);
	}

	private static HPos getHPos(Pos pos) {
		switch (pos) {
			case CENTER:
				return HPos.CENTER;

			case TOP_LEFT:
			case CENTER_LEFT:
			case BOTTOM_LEFT:
			case BASELINE_LEFT:
				return HPos.LEFT;

			case TOP_RIGHT:
			case CENTER_RIGHT:
			case BOTTOM_RIGHT:
			case BASELINE_RIGHT:
				return HPos.RIGHT;

			default:
				return HPos.CENTER;
		}
	}

	private static VPos getVPos(Pos pos) {
		switch (pos) {
			case CENTER:
				return VPos.CENTER;

			case TOP_LEFT:
			case TOP_CENTER:
			case TOP_RIGHT:
				return VPos.TOP;

			case BASELINE_LEFT:
			case BASELINE_CENTER:
			case BASELINE_RIGHT:
				return VPos.BASELINE;

			case BOTTOM_LEFT:
			case BOTTOM_CENTER:
			case BOTTOM_RIGHT:
				return VPos.BOTTOM;

			default:
				return VPos.CENTER;
		}
	}

	private Rectangle getClipRect(Rectangle2D clip) {
		if (isNull(clip)) {
			return null;
		}

		Rectangle2D pageRect = getSkinnable().getPresentationParameter().getViewRect();

		double sx = pixelBuffer.getWidth() / pageRect.getWidth();

		int x = (int) ((clip.getX() - pageRect.getX()) * sx) - 5;
		int y = (int) ((clip.getY() - pageRect.getY()) * sx) - 5;
		int w = (int) (clip.getWidth() * sx) + 10;
		int h = (int) (clip.getHeight() * sx) + 10;

		if (x + w > pixelBuffer.getWidth()) {
			w -= x + w - pixelBuffer.getWidth();
		}
		if (y + h > pixelBuffer.getHeight()) {
			h -= y + h - pixelBuffer.getHeight();
		}

		x = Math.max(0, x);
		y = Math.max(0, y);
		w = Math.max(0, w);
		h = Math.max(0, h);

		return new Rectangle(x, y, w, h);
	}

	private class PageRenderTask implements RenderThreadTask {

		@Override
		public void render() {
			renderer.renderPage(getSkinnable().getPage(), new Dimension((int) viewSize.getWidth(), (int) viewSize.getHeight()));

			updateBuffer(null, renderer.getImage());
		}
	}

	private class ShapeRenderTask implements RenderThreadTask {

		private final PageEditEvent event;


		public ShapeRenderTask(final PageEditEvent event) {
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

				updateBuffer(null, renderer.getImage());
			}
			else {
				final Page page = event.getPage();
				final Rectangle clip = getClipRect(event.getDirtyArea());

				renderer.render(page, shape, clip);

				updateBuffer(clip, renderer.getImage());
			}
		}
	}

	private class FrameRenderTask implements RenderThreadTask {

		private final ColorModel colorModel = new DirectColorModel(32,
				0x0000ff00,   // Red
				0x00ff0000,   // Green
				0xff000000,   // Blue
				0x000000ff    // Alpha
		);

		private final BufferedImage frame;

		public FrameRenderTask(final BufferedImage frame) {
			this.frame = frame;
		}

		@Override
		public void render() throws Exception {
//			Image image = imageView.getImage();
//
//			if (image instanceof WritableImage) {
//				WritableImage writableImage = (WritableImage) image;
//				PixelWriter pw = writableImage.getPixelWriter();
//
//
//			}

			screenCaptureImage = frame;

			if (frame == null) {
				// Use default renderer
				imageView.setImage(fxBufferImage);

				// updateBuffer(null, renderer.getImage());
			} else {

//				Dimension size = new Dimension((int) viewSize.getWidth(), (int) viewSize.getHeight());
//				renderer.renderFrame(frame, size);
//				updateBuffer(null, renderer.getImage());

//				BufferedImage converted = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
//				converted.getGraphics().drawImage(frame, 0, 0, null);
//
//				// Rectangle clip = new Rectangle(0, 0, frame.getWidth(), frame.getHeight());
//				updateBuffer(null, converted);

				// updateBuffer(null, convertImageType(frame));

				// Use frame image
				imageView.setImage(toFXImage(frame));

//				int[] pixels = new int[frame.getWidth() * frame.getHeight()];
//				frame.getRaster().getPixels(0, 0, frame.getWidth(), frame.getHeight(), pixels);
//
////				int pixel = (255 << 24) | (255 << 16) | (0 << 8) | 0 ;
////				Arrays.fill(pixels, pixel);
//
//				frame.getRaster().setPixels(0, 0, frame.getWidth(), frame.getHeight(), pixels);
//
//				updateBuffer(null, frame);

//				Image image = imageView.getImage();
//				int width = (int) image.getWidth();
//				int height = (int) image.getHeight();
//
//				WritableImage img = new WritableImage(width, height);
//				PixelWriter pw = img.getPixelWriter();
//
//				int alpha = 255 ;
//				int r = 255 ;
//				int g = 0 ;
//				int b = 0 ;
//
//				int pixel = (alpha << 24) | (r << 16) | (g << 8) | b ;
//				int[] pixels = new int[width * height];
//				Arrays.fill(pixels, pixel);
//
//				pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
//				imageView.setImage(img);
			}

//			renderer.renderForeground();
//			updateBuffer(null, frame);
		}

		private BufferedImage convertImageType(BufferedImage frame) {
			BufferedImage converted = frame;
			switch (frame.getType()) {
				case BufferedImage.TYPE_INT_ARGB:
				case BufferedImage.TYPE_INT_ARGB_PRE:
					break;
				default:
					WritableRaster raster = colorModel.createCompatibleWritableRaster(frame.getWidth(), frame.getHeight());
					converted = new BufferedImage(colorModel, raster, false, null);
					Graphics2D g2d = converted.createGraphics();
					g2d.drawImage(frame, 0, 0, null);
					g2d.dispose();
					break;
			}
			return converted;
		}

		public WritableImage toFXImage(BufferedImage frame) {
			if (frame == null) {
				return null;
			}



//			frame = convertImageType(frame);

			WritableImage image = new WritableImage(frame.getWidth(), frame.getHeight());


			long currentTime = System.currentTimeMillis();

			PixelWriter pw = image.getPixelWriter();
			DataBufferInt db = (DataBufferInt) frame.getRaster().getDataBuffer();

			int[] data = db.getData();
			int offset = frame.getRaster().getDataBuffer().getOffset();
			int scan =  0;

			SampleModel sm = frame.getRaster().getSampleModel();
			if (sm instanceof SinglePixelPackedSampleModel) {
				scan = ((SinglePixelPackedSampleModel)sm).getScanlineStride();
			}

			PixelFormat<IntBuffer> pf = (frame.isAlphaPremultiplied() ?
					PixelFormat.getIntArgbPreInstance() :
					PixelFormat.getIntArgbInstance());
			pw.setPixels(0, 0, frame.getWidth(), frame.getHeight(), pf, data, offset, scan);

			System.out.println("Transform Time: " + (System.currentTimeMillis() - currentTime) + "ms");

			return image;
		}
	}
}
