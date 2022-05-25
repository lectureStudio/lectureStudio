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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

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
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Screen;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.Page;
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

	private PixelBuffer<IntBuffer> pixelBuffer;

	private ImageView imageView;

	/** FX frame buffer image. */
	private WritableImage fxBufferImage;

	private BufferedImage slideImage;

	private ViewRenderer renderer;

	private final RenderThreadTask renderPageTask = new PageRenderTask();


	protected SlideViewSkin(SlideView control, ReadOnlyObjectWrapper<Bounds> canvasBounds) {
		super(control);

		initLayout(control, canvasBounds);
	}

	public void repaint() {
		Platform.runLater(() -> {
			renderer.renderForeground();

			updateBuffer(null);
		});
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
		SlideView control = getSkinnable();

		control.layoutBoundsProperty().removeListener(boundsListener);
		control.pageProperty().removeListener(pageListener);
		control.presentationParameterProperty().removeListener(presentationListener);

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

	private void initLayout(SlideView control, ReadOnlyObjectWrapper<Bounds> canvasBounds) {
		renderThread = executors.get(control.getViewType());

		renderer = new ViewRenderer(control.getViewType());

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
							getChildren().remove((Node) objectViewNode);
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
			slideImage = createImage(slideImage, imageWidth, imageHeight);

			int[] viewBuffer = ((DataBufferInt) slideImage.getRaster().getDataBuffer()).getData();

			IntBuffer byteBuffer = IntBuffer.wrap(viewBuffer);
			PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();

			pixelBuffer = new PixelBuffer<>(imageWidth, imageHeight, byteBuffer, pixelFormat);
			fxBufferImage = new WritableImage(pixelBuffer);

            imageView.setImage(fxBufferImage);
		}
	}

	private BufferedImage createImage(BufferedImage imageRef, int width, int height) {
		if (imageRef != null) {
			if (width == imageRef.getWidth() && height == imageRef.getHeight()) {
				return imageRef;
			}

			imageRef.flush();
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

			updateBuffer(null);
		});
	}

	private void onPageChanged(Page oldPage, Page newPage) {
		renderer.setPage(newPage);

		renderThread.onTask(renderPageTask);
	}

	private void onPresentationChanged(PresentationParameter newParam) {
		if (isNull(newParam)) {
			return;
		}

		renderer.setParameter(newParam);

		renderThread.onTask(renderPageTask);
	}

	private void updateBuffer(final Rectangle clip) {
		final Graphics2D g2d = slideImage.createGraphics();
		if (nonNull(clip)) {
			g2d.setClip(clip.x, clip.y, clip.width, clip.height);
		}
		g2d.drawImage(renderer.getImage(), 0, 0, null);
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

	private class PageRenderTask implements RenderThreadTask {

		@Override
		public void render() {
			setBounds(getSkinnable().getLayoutBounds());

			renderer.renderPage(getSkinnable().getPage(), new Dimension((int) viewSize.getWidth(), (int) viewSize.getHeight()));

			updateBuffer(null);
		}
	}
}
