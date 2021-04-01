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

import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.beans.ConvertibleNumberProperty;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.GeneralSettingsView;

@FxmlView(name = "general-settings", presenter = org.lecturestudio.presenter.api.presenter.GeneralSettingsPresenter.class)
public class FxGeneralSettingsView extends GridPane implements GeneralSettingsView {

	@FXML
	private ComboBox<Theme> themeCombo;

	@FXML
	private ComboBox<Locale> localeCombo;

	@FXML
	private CheckBox maximizedCheckBox;

	@FXML
	private CheckBox fullscreenCheckBox;

	@FXML
	private CheckBox tabletCheckBox;

	@FXML
	private CheckBox saveAnnotationsCheckBox;

	@FXML
	private CheckBox fullscreenModeCheckBox;

	@FXML
	private Slider extendViewSlider;

	@FXML
	private Slider textSizeSlider;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxGeneralSettingsView() {
		super();
	}

	@Override
	public void setTheme(ObjectProperty<Theme> theme) {
		themeCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(theme));
	}

	@Override
	public void setThemes(List<Theme> themes) {
		FxUtils.invoke(() -> themeCombo.getItems().setAll(themes));
	}

	@Override
	public void setLocale(ObjectProperty<Locale> locale) {
		localeCombo.valueProperty().bindBidirectional(new LectObjectProperty<>(locale));
	}

	@Override
	public void setLocales(List<Locale> locales) {
		FxUtils.invoke(() -> localeCombo.getItems().setAll(locales));
	}

	@Override
	public void setStartMaximized(BooleanProperty maximized) {
		maximizedCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(maximized));
	}

	@Override
	public void setStartFullscreen(BooleanProperty fullscreen) {
		fullscreenCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(fullscreen));
	}

	@Override
	public void setTabletMode(BooleanProperty tabletMode) {
		tabletCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(tabletMode));
	}

	@Override
	public void setSaveAnnotationsOnClose(BooleanProperty saveAnnotations) {
		saveAnnotationsCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(saveAnnotations));
	}

	@Override
	public void setExtendedFullscreen(BooleanProperty extended) {
		fullscreenModeCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(extended));
	}

	@Override
	public void setExtendPageDimension(ObjectProperty<Dimension2D> dimension) {
		Converter<Dimension2D, Double> slideSpaceConv = SlideSpaceConverter.INSTANCE;
		extendViewSlider.valueProperty().bindBidirectional(new ConvertibleNumberProperty<>(dimension, slideSpaceConv));
	}

	@Override
	public void setTextSize(DoubleProperty size) {
		textSizeSlider.valueProperty().bindBidirectional(new LectDoubleProperty(size));
		textSizeSlider.valueProperty().addListener(observable -> {
			textSizeSlider.getScene().getRoot().setStyle(String.format(Locale.US, "-fx-font-size: %.2fpt;", textSizeSlider.getValue()));
		});
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}



	/**
	 * Extended slide space to number and vice-versa converter.
	 */
	private static class SlideSpaceConverter implements Converter<Dimension2D, Double> {

		static final SlideSpaceConverter INSTANCE = new SlideSpaceConverter();

		private final PageMetrics metrics = new PageMetrics(4, 3);


		@Override
		public Double to(Dimension2D value) {
			return Math.abs((value.getWidth() - metrics.getWidth()) / metrics.getWidth());
		}

		@Override
		public Dimension2D from(Double value) {
			double width = metrics.getWidth() - (value * metrics.getWidth());
			double height = metrics.getHeight(width);

			return new Dimension2D(width, height);
		}
	}
}
