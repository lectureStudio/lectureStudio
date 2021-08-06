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

package org.lecturestudio.presenter.swing.view;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.ToolSettingsView;
import org.lecturestudio.swing.beans.ConvertibleNumberProperty;
import org.lecturestudio.swing.components.previews.ArrowToolPreview;
import org.lecturestudio.swing.components.previews.EllipseToolPreview;
import org.lecturestudio.swing.components.previews.LineToolPreview;
import org.lecturestudio.swing.components.previews.PenToolPreview;
import org.lecturestudio.swing.components.previews.PointerToolPreview;
import org.lecturestudio.swing.components.previews.RectangleToolPreview;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "tool-settings", presenter = org.lecturestudio.presenter.api.presenter.ToolSettingsPresenter.class)
public class SwingToolSettingsView extends JPanel implements ToolSettingsView {

	private JCheckBox scaleHighlighterCheckBox;

	private JSlider highlighterSlider;

	private JSlider penSlider;

	private JSlider pointerSlider;

	private JSlider lineSlider;

	private JSlider arrowSlider;

	private JSlider rectangleSlider;

	private JSlider ellipseSlider;

	private PenToolPreview highlighterPreview;

	private PenToolPreview penPreview;

	private PointerToolPreview pointerPreview;

	private LineToolPreview linePreview;

	private ArrowToolPreview arrowPreview;

	private RectangleToolPreview rectanglePreview;

	private EllipseToolPreview ellipsePreview;

	private JButton closeButton;

	private JButton resetButton;


	SwingToolSettingsView() {
		super();
	}

	@Override
	public void setScaleHighlighter(BooleanProperty scale) {
		SwingUtils.bindBidirectional(scaleHighlighterCheckBox, scale);
	}

	@Override
	public void setHighlighterWidth(DoubleProperty width) {
		var highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(highlighterSlider, highlightProperty);
		highlighterPreview.setWidth(highlighterSlider.getValue());
	}

	@Override
	public void setPenWidth(DoubleProperty width) {
		var penProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(penSlider, penProperty);
		penPreview.setWidth(penSlider.getValue());
	}

	@Override
	public void setPointerWidth(DoubleProperty width) {
		var pointerProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(pointerSlider, pointerProperty);
		pointerPreview.setWidth(pointerSlider.getValue());
	}

	@Override
	public void setLineWidth(DoubleProperty width) {
		var lineProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(lineSlider, lineProperty);
		linePreview.setWidth(lineSlider.getValue());
	}

	@Override
	public void setArrowWidth(DoubleProperty width) {
		var highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(arrowSlider, highlightProperty);
		arrowPreview.setWidth(arrowSlider.getValue());
	}

	@Override
	public void setRectangleWidth(DoubleProperty width) {
		var highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(rectangleSlider, highlightProperty);
		rectanglePreview.setWidth(rectangleSlider.getValue());
	}

	@Override
	public void setEllipseWidth(DoubleProperty width) {
		var highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(ellipseSlider, highlightProperty);
		ellipsePreview.setWidth(ellipseSlider.getValue());
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		highlighterSlider.addChangeListener(e -> highlighterPreview.setWidth(highlighterSlider.getValue()));
		penSlider.addChangeListener(e -> penPreview.setWidth(penSlider.getValue()));
		pointerSlider.addChangeListener(e -> pointerPreview.setWidth(pointerSlider.getValue()));
		lineSlider.addChangeListener(e -> linePreview.setWidth(lineSlider.getValue()));
		arrowSlider.addChangeListener(e -> arrowPreview.setWidth(arrowSlider.getValue()));
		rectangleSlider.addChangeListener(e -> rectanglePreview.setWidth(rectangleSlider.getValue()));
		ellipseSlider.addChangeListener(e -> ellipsePreview.setWidth(ellipseSlider.getValue()));
	}



	/**
	 * Tool size to slide space and vice-versa converter.
	 */
	private static class ToolSizeConverter implements Converter<Double, Integer> {

		static final ToolSizeConverter INSTANCE = new ToolSizeConverter();


		@Override
		public Integer to(Double value) {
			return (int) (value * 500);
		}

		@Override
		public Double from(Integer value) {
			return value / 500.d;
		}
	}
}
