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
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.ToolSettingsView;
import org.lecturestudio.presenter.swing.converter.ToolSizeConverter;
import org.lecturestudio.swing.beans.ConvertibleNumberProperty;
import org.lecturestudio.swing.components.previews.PenToolPreview;
import org.lecturestudio.swing.components.previews.PointerToolPreview;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "tool-settings", presenter = org.lecturestudio.presenter.api.presenter.ToolSettingsPresenter.class)
public class SwingToolSettingsView extends JPanel implements ToolSettingsView {

	private JCheckBox scaleHighlighterCheckBox;

	private JSlider highlighterSlider;

	private JSlider penSlider;

	private JSlider pointerSlider;

	private PenToolPreview highlighterPreview;

	private PenToolPreview penPreview;

	private PointerToolPreview pointerPreview;

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
		var highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(penSlider, highlightProperty);
		penPreview.setWidth(penSlider.getValue());
	}

	@Override
	public void setPointerWidth(DoubleProperty width) {
		var highlightProperty = new ConvertibleNumberProperty<>(width, ToolSizeConverter.INSTANCE);
		SwingUtils.bindBidirectional(pointerSlider, highlightProperty);
		pointerPreview.setWidth(pointerSlider.getValue());
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
		highlighterSlider.setSnapToTicks(true);
		highlighterSlider.setMinimum(1);
		highlighterSlider.setMaximum(15);
		highlighterSlider.setMinorTickSpacing(1);
		highlighterSlider.setMajorTickSpacing(2);
		highlighterSlider.addChangeListener(e -> {
			highlighterPreview.setWidth(highlighterSlider.getValue());
		});
		penSlider.setSnapToTicks(true);
		penSlider.setMinimum(1);
		penSlider.setMaximum(15);
		penSlider.setMinorTickSpacing(1);
		penSlider.setMajorTickSpacing(2);
		penSlider.addChangeListener(e -> {
			penPreview.setWidth(penSlider.getValue());
		});
		pointerSlider.setSnapToTicks(true);
		pointerSlider.setMinimum(1);
		pointerSlider.setMaximum(15);
		pointerSlider.setMinorTickSpacing(1);
		pointerSlider.setMajorTickSpacing(2);
		pointerSlider.addChangeListener(e -> {
			pointerPreview.setWidth(pointerSlider.getValue());
		});
	}
}
