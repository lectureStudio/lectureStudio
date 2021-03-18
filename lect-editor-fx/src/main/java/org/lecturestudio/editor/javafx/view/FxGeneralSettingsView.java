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

package org.lecturestudio.editor.javafx.view;

import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.view.GeneralSettingsView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.beans.LectObjectProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "general-settings", presenter = org.lecturestudio.editor.api.presenter.GeneralSettingsPresenter.class)
public class FxGeneralSettingsView extends GridPane implements GeneralSettingsView {

	@FXML
	private ComboBox<Locale> localeCombo;

	@FXML
	private CheckBox maximizedCheckBox;

	@FXML
	private CheckBox fullscreenModeCheckBox;

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
	public void setExtendedFullscreen(BooleanProperty extended) {
		fullscreenModeCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(extended));
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
}
