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

import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

public class ColorPalette extends Control {

	private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color");

	private final ResourceBundle resourceBundle;

	/** Custom colors added to the ColorPalette by the user. */
	private final ObservableList<Color> customColors = FXCollections.observableArrayList();


	public ColorPalette(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;

		getStyleClass().add("tool-color-palette");
	}

	/**
	 * The value of this ColorPalette is either the value input by the user,
	 * or the last selected value.
	 */
	public ObjectProperty<Color> colorProperty() {
		return color;
	}

	public final void setColor(Color value) {
		colorProperty().set(value);
	}

	public final Color getColor() {
		return colorProperty().get();
	}

	/**
	 * Gets the list of custom colors added to the ColorPalette by the user.
	 */
	public final ObservableList<Color> getCustomColors() {
		return customColors;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/color-palette.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new ColorPaletteSkin(this, resourceBundle);
	}

}
