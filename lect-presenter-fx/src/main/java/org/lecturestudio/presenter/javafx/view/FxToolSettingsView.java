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

package org.lecturestudio.presenter.javafx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.beans.ConvertibleNumberProperty;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.ToolSettingsView;

@FxmlView(name = "tool-settings", presenter = org.lecturestudio.presenter.api.presenter.ToolSettingsPresenter.class)
public class FxToolSettingsView extends GridPane implements ToolSettingsView {

	@FXML
	private CheckBox scaleHighlighterCheckBox;

	@FXML
	private Slider highlighterSlider;

	@FXML
	private Slider penSlider;

	@FXML
	private Slider pointerSlider;

	@FXML
	private Slider lineSlider;

	@FXML
	private Slider arrowSlider;

	@FXML
	private Slider rectangleSlider;

	@FXML
	private Slider ellipseSlider;

	@FXML
	private CubicCurve highlighterCurve;

	@FXML
	private CubicCurve penCurve;

	@FXML
	private Circle pointerCircle;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxToolSettingsView() {
		super();
	}

	@Override
	public void setScaleHighlighter(BooleanProperty scale) {
		scaleHighlighterCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(scale));
	}

	@Override
	public void setHighlighterWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		highlighterSlider.valueProperty().bindBidirectional(highlightProperty);
	}

	@Override
	public void setPenWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> penProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		penSlider.valueProperty().bindBidirectional(penProperty);
	}

	@Override
	public void setPointerWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> pointerProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		pointerSlider.valueProperty().bindBidirectional(pointerProperty);
	}

	@Override
	public void setLineWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> lineProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		lineSlider.valueProperty().bindBidirectional(lineProperty);
	}

	@Override
	public void setArrowWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> arrowProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		arrowSlider.valueProperty().bindBidirectional(arrowProperty);
	}

	@Override
	public void setRectangleWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> rectangleProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		rectangleSlider.valueProperty().bindBidirectional(rectangleProperty);
	}

	@Override
	public void setEllipseWidth(DoubleProperty width) {
		ConvertibleNumberProperty<Double, Double> ellipseProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		ellipseSlider.valueProperty().bindBidirectional(ellipseProperty);
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@FXML
	private void initialize() {
		highlighterCurve.strokeWidthProperty().bind(highlighterSlider.valueProperty());
		penCurve.strokeWidthProperty().bind(penSlider.valueProperty());
		pointerCircle.radiusProperty().bind(pointerSlider.valueProperty());
	}



	/**
	 * Tool size to slide space and vice-versa converter.
	 */
	private static class ToolSizeConverter implements Converter<Double, Double> {

		static final ToolSizeConverter INSTANCE = new ToolSizeConverter();


		@Override
		public Double to(Double value) {
			return value * 500;
		}

		@Override
		public Double from(Double value) {
			return value / 500;
		}
	}
}
