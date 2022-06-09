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

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.presenter.api.view.WhiteboardSettingsView;
import org.lecturestudio.swing.beans.ConvertibleNumberProperty;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.components.ColorChooserButton;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "whiteboard-settings", presenter = org.lecturestudio.presenter.api.presenter.WhiteboardSettingsPresenter.class)
public class SwingWhiteboardSettingsView extends JPanel implements WhiteboardSettingsView {

	private final RenderController renderer;

	private SlideView whiteboardSlideView;

	private ColorChooserButton colorChooserButton;

	private JCheckBox showGridAutoCheckBox;

	private JCheckBox showGridExternCheckBox;

	private ColorChooserButton gridColorPicker;

	private JCheckBox gridVerticalLinesCheckBox;

	private JSlider gridLinesSlider;

	private JCheckBox griHorizontalLinesCheckBox;

	private JSlider griHorizontalLinesSlider;

	private JButton closeButton;

	private JButton resetButton;


	@Inject
	SwingWhiteboardSettingsView(RenderController renderer) {
		super();

		this.renderer = renderer;
	}

	@Override
	public void setBackgroundColor(ObjectProperty<Color> color) {
		SwingUtils.bindBidirectional(colorChooserButton,
				new ConvertibleObjectProperty<>(color, ColorConverter.INSTANCE));
	}

	@Override
	public void setGridColor(ObjectProperty<Color> color) {
		SwingUtils.bindBidirectional(gridColorPicker,
				new ConvertibleObjectProperty<>(color, ColorConverter.INSTANCE));
	}

	@Override
	public void setGridInterval(DoubleProperty interval) {
		Converter<Double, Integer> converter = NumberConverter.INSTANCE;

		SwingUtils.bindBidirectional(gridLinesSlider,
				new ConvertibleNumberProperty<>(interval, converter));
	}

	@Override
	public void setShowGridAutomatically(BooleanProperty show) {
		SwingUtils.bindBidirectional(showGridAutoCheckBox, show);
	}

	@Override
	public void setShowGridOnDisplays(BooleanProperty show) {
		SwingUtils.bindBidirectional(showGridExternCheckBox, show);
	}

	@Override
	public void setShowVerticalGridLines(BooleanProperty show) {
		SwingUtils.bindBidirectional(gridVerticalLinesCheckBox, show);
	}

	@Override
	public void setShowHorizontalGridLines(BooleanProperty show) {
		SwingUtils.bindBidirectional(griHorizontalLinesCheckBox, show);
	}

	@Override
	public void setWhiteboardPage(Page page, PresentationParameter parameter) {
		whiteboardSlideView.parameterChanged(page, parameter);
		whiteboardSlideView.setPage(page);
		whiteboardSlideView.renderPage();
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
		whiteboardSlideView.setPageRenderer(renderer);
	}



	/**
	 * Grid space to integer and vice-versa converter.
	 */
	private static class NumberConverter implements Converter<Double, Integer> {

		static final NumberConverter INSTANCE = new NumberConverter();


		@Override
		public Integer to(Double value) {
			return (int) (value * 10);
		}

		@Override
		public Double from(Integer value) {
			return value / 10.d;
		}
	}
}
