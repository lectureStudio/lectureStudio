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

import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableStringProperty;
import javafx.geometry.Bounds;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;

import org.lecturestudio.javafx.util.FxStyleablePropertyFactory;

public class SvgIcon extends Region {

	private static final FxStyleablePropertyFactory<SvgIcon> FACTORY = new FxStyleablePropertyFactory<>(Region.getClassCssMetaData());

	private final StyleableStringProperty content;

	private final StyleableObjectProperty<Paint> fill;

	private final StyleableObjectProperty<Paint> stroke;

	private final StyleableObjectProperty<Number> strokeWidth;

	private final StyleableObjectProperty<Number> size;

	private final StyleableObjectProperty<Number> contentWidth;

	private final StyleableObjectProperty<Number> contentHeight;

	private final StyleableBooleanProperty smooth;

	private final SVGPath svgPath;


	public SvgIcon() {
		super();

		content = FACTORY.createStringProperty(this, "icon-content", "-fx-icon-content", s -> s.content, "");
		fill = FACTORY.createPaintProperty(this, "icon-fill", "-fx-icon-fill", s -> s.fill, Color.BLACK);
		stroke = FACTORY.createPaintProperty(this, "icon-stroke", "-fx-icon-stroke", s -> s.stroke, Color.BLACK);
		strokeWidth = FACTORY.createNumberProperty(this, "icon-stroke-width", "-fx-icon-stroke-width", s -> s.strokeWidth, 0);
		size = FACTORY.createNumberProperty(this, "icon-size", "-fx-icon-size", s -> s.size, Font.getDefault().getSize());
		smooth = FACTORY.createBooleanProperty(this, "icon-smooth", "-fx-icon-smooth", s -> s.smooth, true);
		contentWidth = FACTORY.createNumberProperty(this, "icon-content-width", "-fx-icon-content-width", s -> s.contentWidth, null);
		contentHeight = FACTORY.createNumberProperty(this, "icon-content-height", "-fx-icon-content-height", s -> s.contentHeight, null);
		svgPath = new SVGPath();
		svgPath.setManaged(false);

		initialize();
	}

	public final String getContent() {
		return content.get();
	}

	public final void setContent(String value) {
		content.set(value);
	}

	public final ObservableValue<String> contentProperty() {
		return content;
	}

	public final double getContentWidth() {
		Number value = contentWidth.get();
		return nonNull(value) ? value.doubleValue() : svgPath.prefWidth(-1);
	}

	public final void setContentWidth(double value) {
		contentWidth.set(value);
	}

	public final ObservableValue<Number> contentWidthProperty() {
		return contentWidth;
	}

	public final double getContentHeight() {
		Number value = contentHeight.get();
		return nonNull(value) ? value.doubleValue() : svgPath.prefHeight(-1);
	}

	public final void setContentHeight(double value) {
		contentHeight.set(value);
	}

	public final ObservableValue<Number> contentHeightProperty() {
		return contentHeight;
	}

	public final void setFillColor(Paint value) {
		fill.set(value);
	}

	public final Paint getFillColor() {
		return fill.get();
	}

	public final ObservableValue<Paint> fillColorProperty() {
		return fill;
	}

	public final void setStrokeColor(Paint value) {
		stroke.set(value);
	}

	public final Paint getStrokeColor() {
		return stroke.get();
	}

	public final ObservableValue<Paint> strokeColorProperty() {
		return stroke;
	}

	public final void setStrokeWidth(double value) {
		strokeWidth.set(value);
	}

	public final double getStrokeWidth() {
		return strokeWidth.get().doubleValue();
	}

	public final ObservableValue<Number> strokeWidthProperty() {
		return strokeWidth;
	}

	public final double getSize() {
		return size.get().doubleValue();
	}

	public final void setSize(double value) {
		size.set(value);
	}

	public final ObservableValue<Number> sizeProperty() {
		return size;
	}

	public final void setSmooth(boolean value) {
		smooth.set(value);
	}

	public final boolean isSmooth() {
		return smooth.get();
	}

	public final ObservableValue<Boolean> smoothProperty() {
		return smooth;
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return FACTORY.getCssMetaData();
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		return getClassCssMetaData();
	}

	@Override
	protected double computeMinWidth(double height) {
		return computePrefWidth(height);
	}

	@Override
	protected double computeMinHeight(double width) {
		return computePrefHeight(width);
	}

	@Override
	protected double computeMaxWidth(double height) {
		return getSize();
	}

	@Override
	protected double computeMaxHeight(double width) {
		return getSize();
	}

	@Override
	protected double computePrefWidth(double height) {
		return getSize();
	}

	@Override
	protected double computePrefHeight(double width) {
		return getSize();
	}

	@Override
	protected void layoutChildren() {
		transformShape();
	}

	private void transformShape() {
		String content = getContent();

		if (nonNull(content) && !content.isEmpty()) {
			Bounds bounds = svgPath.getBoundsInLocal();

			if (isNull(bounds) || bounds.isEmpty()) {
				return;
			}

			double width = getContentWidth();
			double height = getContentHeight();
			double size = getSize();

			if (Double.isNaN(width) || Double.isNaN(height) || Double.isNaN(size) ||
				Double.isInfinite(width) || Double.isInfinite(height) || Double.isInfinite(size) ||
				width <= 0 || height <= 0 || size <= 0) {
				return;
			}

			double scale = size / height;

			if (width * scale > size) {
				scale = size / width;
			}

			// Prevent invalid scale values
			if (Double.isNaN(scale) || Double.isInfinite(scale) || scale <= 0) {
				return;
			}

			if (isNull(contentWidth.get()) || isNull(contentHeight.get())) {
				double sWidth = width * scale;
				double sHeight = height * scale;

				// Initial size pivot offset.
				double tx = -bounds.getMinX() - (width / 2);
				double ty = -bounds.getMinY() - (height / 2);

				// Center scaled size.
				tx += (sWidth / 2) - (sWidth - getWidth()) / 2;
				ty += (sHeight / 2) - (sHeight - getHeight()) / 2;

				svgPath.getTransforms().clear();
				svgPath.setTranslateX(snapPositionX(tx));
				svgPath.setTranslateY(snapPositionY(ty));
				svgPath.setScaleX(scale);
				svgPath.setScaleY(scale);
			}
			else {
				Scale scaleT = new Scale();
				scaleT.setX(scale);
				scaleT.setY(scale);
				scaleT.setPivotX(0);
				scaleT.setPivotY(0);

				svgPath.getTransforms().clear();
				svgPath.getTransforms().add(scaleT);
			}
		}
	}

	private void initialize() {
		svgPath.contentProperty().bind(contentProperty());
		svgPath.fillProperty().bind(fillColorProperty());
		svgPath.strokeProperty().bind(strokeColorProperty());
		svgPath.strokeWidthProperty().bind(strokeWidthProperty());
		svgPath.smoothProperty().bind(smoothProperty());
		svgPath.contentProperty().addListener(observable -> requestLayout());

		getChildren().add(svgPath);

		sizeProperty().addListener(observable -> requestLayout());

		if (nonNull(svgPath.getContent()) && !svgPath.getContent().isEmpty()) {
			requestLayout();
		}

		parentProperty().addListener((observable, oldParent, newParent) -> {
			if (nonNull(newParent)) {
				if (Labeled.class.isAssignableFrom(newParent.getClass())) {
					Labeled labeled = (Labeled) newParent;
					labeled.fontProperty().addListener((observable1, oldFont, newFont) -> setSize(newFont.getSize()));

					setSize(labeled.getFont().getSize());
				}
			}
		});
	}
}
