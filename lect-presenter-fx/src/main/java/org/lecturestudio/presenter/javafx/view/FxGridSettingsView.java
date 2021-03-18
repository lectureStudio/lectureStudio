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

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectColorProperty;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.control.SlideView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.GridSettingsView;

@FxmlView(name = "grid-settings", presenter = org.lecturestudio.presenter.api.presenter.GridSettingsPresenter.class)
public class FxGridSettingsView extends GridPane implements GridSettingsView {

	@FXML
	private CheckBox showGridExternCheckBox;

	@FXML
	private ColorPicker gridColorPicker;

	@FXML
	private CheckBox gridVerticalLinesCheckBox;

	@FXML
	private Slider gridLinesSlider;

	@FXML
	private CheckBox griHorizontalLinesCheckBox;

	@FXML
	private Slider griHorizontalLinesSlider;

	@FXML
	private SlideView whiteboardSlideView;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;

	private final RenderController renderer;


	@Inject
	public FxGridSettingsView(RenderController renderer) {
		super();

		this.renderer = renderer;
	}

	@Override
	public void setGridColor(ObjectProperty<Color> color) {
		gridColorPicker.valueProperty().bindBidirectional(new LectColorProperty(color));
	}

	@Override
	public void setGridInterval(DoubleProperty interval) {
		gridLinesSlider.valueProperty().bindBidirectional(new LectDoubleProperty(interval));
	}

	@Override
	public void setShowGridOnDisplays(BooleanProperty show) {
		showGridExternCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(show));
	}

	@Override
	public void setShowVerticalGridLines(BooleanProperty show) {
		gridVerticalLinesCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(show));
	}

	@Override
	public void setShowHorizontalGridLines(BooleanProperty show) {
		griHorizontalLinesCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(show));
	}

	@Override
	public void setWhiteboardPage(Page page, PresentationParameter parameter) {
		whiteboardSlideView.parameterChanged(page, parameter);
		whiteboardSlideView.setPage(page);
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
		whiteboardSlideView.setPageRenderer(renderer);
	}

}
