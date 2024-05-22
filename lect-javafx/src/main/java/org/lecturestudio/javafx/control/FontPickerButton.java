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

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;

public class FontPickerButton extends ExtSplitMenuButton {

	private final ObjectProperty<Font> font = new SimpleObjectProperty<>(this, "text-font");

	private final ResourceBundle resourceBundle;


	@Inject
	public FontPickerButton(ResourceBundle resourceBundle) {
		super();

		this.resourceBundle = resourceBundle;

		getStyleClass().add("split-picker-button");

		// Set default font.
		setTextFont(Font.font("Open Sans"));
	}

	/**
	 * The value of this FontPicker is either the value input by the user,
	 * or the last selected font.
	 */
	public ObjectProperty<Font> textFontProperty() {
		return font;
	}

	public final void setTextFont(Font value) {
		textFontProperty().set(value);
	}

	public final Font getTextFont() {
		return textFontProperty().get();
	}

	/** {@inheritDoc} */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new FontPickerButtonSkin(this, resourceBundle);
	}

}
