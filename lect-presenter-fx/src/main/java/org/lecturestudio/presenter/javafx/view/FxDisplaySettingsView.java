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

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import org.lecturestudio.core.app.configuration.ScreenConfiguration;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectColorProperty;
import org.lecturestudio.javafx.control.ScreenView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.DisplaySettingsView;

@FxmlView(name = "display-settings", presenter = org.lecturestudio.presenter.api.presenter.DisplaySettingsPresenter.class)
public class FxDisplaySettingsView extends GridPane implements DisplaySettingsView {

	@FXML
	private CheckBox autoPresentationCheckBox;

	@FXML
	private ColorPicker presentationBgColorPicker;

	@FXML
	private Pane displaysContainer;

	@FXML
	private ScreenView displayView;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxDisplaySettingsView() {
		super();
	}

	@Override
	public void setEnableDisplaysOnStart(BooleanProperty enable) {
		autoPresentationCheckBox.selectedProperty().bindBidirectional(new LectBooleanProperty(enable));
	}

	@Override
	public void setDisplayBackgroundColor(ObjectProperty<Color> color) {
		presentationBgColorPicker.valueProperty().bindBidirectional(new LectColorProperty(color));
	}

	@Override
	public void setScreens(List<ScreenConfiguration> screens) {
		FxUtils.invoke(() -> {
			displaysContainer.getChildren().clear();

			List<Screen> viewScreens = new ArrayList<>();

			for (ScreenConfiguration screenConfig : screens) {
				Rectangle2D bounds = screenConfig.getScreen().getBounds();

				CheckBox checkBox = new CheckBox(ScreenBoundsConverter.INSTANCE.to(bounds));
				checkBox.setSelected(screenConfig.getEnabled());
				checkBox.selectedProperty().bindBidirectional(new LectBooleanProperty(screenConfig.enabledProperty()));

				viewScreens.add(screenConfig.getScreen());

				FxUtils.invoke(() -> displaysContainer.getChildren().add(checkBox));
			}

			displayView.getScreens().setAll(viewScreens);
			displayView.setOnAction(event -> {
				for (ScreenConfiguration screenConfig : screens) {
					if (screenConfig.getScreen().equals(event.getScreen())) {
						// Toggle.
						screenConfig.setEnabled(!screenConfig.getEnabled());
						break;
					}
				}
			});
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
	 * Screen bounds of Rectangle2D to String converter.
	 */
	private static class ScreenBoundsConverter implements Converter<Rectangle2D, String> {

		static final ScreenBoundsConverter INSTANCE = new ScreenBoundsConverter();


		@Override
		public String to(Rectangle2D bounds) {
			return String.format("[%d, %d, %d, %d]", (int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
		}

		@Override
		public Rectangle2D from(String value) {
			return new Rectangle2D();
		}

	}
}
