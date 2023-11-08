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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.listener.ShapeListener;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.SlideViewOverlay;
import org.lecturestudio.core.view.ViewType;

public class SlideView extends Control implements ParameterChangeListener, org.lecturestudio.core.view.SlideView, ShapeListener {

	private static final String DEFAULT_STYLE_CLASS = "slide-view";

	/** Specifies how the view should be treated by a renderer. */
	private ObjectProperty<ViewType> viewType;

	private final ObjectProperty<Page> page = new SimpleObjectProperty<>();

	private final ObjectProperty<RenderController> pageRenderer = new SimpleObjectProperty<>();

	private final ReadOnlyObjectWrapper<Bounds> canvasBounds = new ReadOnlyObjectWrapper<>();

	private final ReadOnlyObjectWrapper<Affine> pageTransform = new ReadOnlyObjectWrapper<>(new Affine());

	private final ReadOnlyObjectWrapper<PresentationParameter> pParameter = new ReadOnlyObjectWrapper<>();

	private final ObservableList<SlideViewOverlay> overlayNodes = FXCollections.observableArrayList();

	private final ObservableList<PageObjectView<?>> objectViews = FXCollections.observableArrayList();

	private ObjectProperty<Pos> alignment;

	private final BooleanProperty seekProperty = new BooleanProperty(true);


	public SlideView() {
		initialize();
	}

	public ObservableList<SlideViewOverlay> getOverlays() {
		return overlayNodes;
	}

	public void addOverlay(SlideViewOverlay node) {
		if (isNull(node)) {
			return;
		}
		if (Node.class.isAssignableFrom(node.getClass())) {
			overlayNodes.add(node);
		}
	}

	public void removeOverlay(SlideViewOverlay node) {
		if (isNull(node)) {
			return;
		}
		if (Node.class.isAssignableFrom(node.getClass())) {
			overlayNodes.remove(node);
		}
	}

	public void addPageObjectView(PageObjectView<?> objectView) {
		if (isNull(objectView)) {
			return;
		}
		if (PageObject.class.isAssignableFrom(objectView.getClass())) {
			PageObject<?> pageObject = (PageObject<?>) objectView;
			pageObject.setPageTransform(getPageTransform());
		}
		if (Node.class.isAssignableFrom(objectView.getClass())) {
			getPageObjectViews().add(objectView);
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
		if (Node.class.isAssignableFrom(objectView.getClass())) {
			getPageObjectViews().remove(objectView);
		}
	}

	public void removeAllPageObjectViews() {
		getPageObjectViews().clear();
	}

	public ObservableList<PageObjectView<?>> getPageObjectViews() {
		return objectViews;
	}

	public final void setAlignment(Pos value) {
		alignmentProperty().set(value);
	}

	public final Pos getAlignment() {
		return alignment == null ? Pos.TOP_LEFT : alignment.get();
	}

	public final ObjectProperty<Pos> alignmentProperty() {
		if (alignment == null) {
			alignment = new StyleableObjectProperty<>(Pos.TOP_LEFT) {

				@Override
				public CssMetaData<SlideView, Pos> getCssMetaData() {
					return StyleableProperties.ALIGNMENT;
				}

				@Override
				public Object getBean() {
					return SlideView.this;
				}

				@Override
				public String getName() {
					return "alignment";
				}
			};
		}
		return alignment;
	}

	/**
	 * Describes the real SlideView canvas bounds.
	 */
	public ReadOnlyObjectProperty<Bounds> canvasBoundsProperty() {
		return canvasBounds.getReadOnlyProperty();
	}

	public Bounds getCanvasBounds() {
		return canvasBounds.get();
	}

	public ReadOnlyObjectProperty<Affine> pageTransformProperty() {
		return pageTransform.getReadOnlyProperty();
	}

	public Affine getPageTransform() {
		return pageTransform.get();
	}

	/**
	 * Describes how a page should be displayed on this SlideView.
	 */
	public ReadOnlyObjectProperty<PresentationParameter> presentationParameterProperty() {
		return pParameter.getReadOnlyProperty();
	}

	public PresentationParameter getPresentationParameter() {
		return pParameter.get();
	}

	public final ObjectProperty<ViewType> viewTypeProperty() {
		if (viewType == null) {
			viewType = new StyleableObjectProperty<>(ViewType.User) {

				@Override
				public CssMetaData<SlideView, ViewType> getCssMetaData() {
					return StyleableProperties.VIEW_TYPE;
				}

				@Override
				public Object getBean() {
					return SlideView.this;
				}

				@Override
				public String getName() {
					return "viewType";
				}
			};
		}
		return viewType;
	}

	public final void setViewType(ViewType value) {
		viewTypeProperty().setValue(value);
	}

	public final ViewType getViewType() {
		return viewType == null ? ViewType.User : viewType.getValue();
	}
	
	public void addSelectionHandler(EventHandler<MouseEvent> handler) {
		addEventHandler(MouseEvent.MOUSE_RELEASED, handler);
	}

	public final ObjectProperty<Page> pageProperty() {
		return page;
	}

	public Page getPage() {
		return pageProperty().get();
	}

	@Override
	public void setPage(Page page) {
		Page oldPage = getPage();

		pageProperty().set(page);

		if (nonNull(oldPage)) {
			oldPage.removeShapeListener(this);
		}
		if (nonNull(page)) {
			if (Boolean.FALSE.equals(seekProperty.get())) {
				page.addShapeListener(this);
			}
		}
	}

	public final ObjectProperty<RenderController> pageRendererProperty() {
		return pageRenderer;
	}

	public RenderController getPageRenderer() {
		return pageRendererProperty().get();
	}

	public void setPageRenderer(RenderController renderer) {
		pageRendererProperty().set(renderer);
	}

	@Override
	public Page forPage() {
		return getPage();
	}

	@Override
	public void parameterChanged(Page page, PresentationParameter parameter) {
		pParameter.set(null);
		pParameter.set(parameter);

		updateViewTransform();
	}

	public synchronized void repaint() {
		if (seekProperty.get()) {
			return;
		}

		SlideViewSkin skin = (SlideViewSkin) getSkin();
		skin.repaint();
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * 		CssMetaData of its super classes.
	 */
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}
	
	@Override
	protected Skin<?> createDefaultSkin() {
		return new SlideViewSkin(this, canvasBounds);
	}

	private void updateViewTransform() {
		if (isNull(pParameter.get()) || isNull(getCanvasBounds())) {
			return;
		}

		Rectangle2D pageRect = pParameter.get().getPageRect();
		Bounds canvasBounds = getCanvasBounds();

		double tx = canvasBounds.getMinX() / canvasBounds.getWidth() * pageRect.getWidth() + pageRect.getX();
		double ty = canvasBounds.getMinY() / canvasBounds.getHeight() * pageRect.getHeight() + pageRect.getY();
		double s = canvasBounds.getWidth() / pageRect.getWidth();

		Affine transform = getPageTransform();
		transform.setToTransform(s, 0, tx, 0, s, ty);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
		setSnapToPixel(true);
		setPickOnBounds(true);

		canvasBoundsProperty().addListener((observable, oldBounds, newBounds) -> updateViewTransform());
		seekProperty().addListener(((observable, oldValue, newValue) -> {
			Page page = getPage();

			if (nonNull(page)) {
				if (Boolean.FALSE.equals(newValue)) {
					page.addShapeListener(this);
				}
				else {
					page.removeShapeListener(this);
				}
			}
		}));
	}

	public BooleanProperty seekProperty() {
		return seekProperty;
	}

	public void setSeek(boolean seekProperty) {
		this.seekProperty.set(seekProperty);
	}

	@Override
	public void shapePainted(ShapePaintEvent event) {
		repaint();
	}

	@Override
	public void shapeModified(ShapeModifyEvent event) {
		repaint();
	}


	private static class StyleableProperties {

		private static final StyleConverter<?, ViewType> VIEW_TYPE_CONVERTER = StyleConverter.getEnumConverter(ViewType.class);

		private static final CssMetaData<SlideView, Pos> ALIGNMENT = new CssMetaData<>(
				"-fx-alignment", new EnumConverter<>(Pos.class), Pos.TOP_LEFT) {

			@Override
			public boolean isSettable(SlideView n) {
				return n.alignment == null || !n.alignment.isBound();
			}

			@Override
			public StyleableProperty<Pos> getStyleableProperty(SlideView n) {
				return (StyleableProperty<Pos>) (WritableValue<Pos>) n.alignmentProperty();
			}

			@Override
			public Pos getInitialValue(SlideView n) {
				return Pos.TOP_LEFT;
			}
		};

		private static final CssMetaData<SlideView, ViewType> VIEW_TYPE = new CssMetaData<>(
				"-fx-view-type", VIEW_TYPE_CONVERTER, ViewType.User) {

			@Override
			public boolean isSettable(SlideView n) {
				return n.viewType == null || !n.viewType.isBound();
			}

			@Override
			public StyleableProperty<ViewType> getStyleableProperty(SlideView n) {
				return (StyleableProperty<ViewType>) (WritableValue<ViewType>) n.viewTypeProperty();
			}

			@Override
			public ViewType getInitialValue(SlideView n) {
				return ViewType.User;
			}
		};

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

		static {
			List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
			styleables.add(ALIGNMENT);
			styleables.add(VIEW_TYPE);

			STYLEABLES = Collections.unmodifiableList(styleables);
		}

	}
	
}
