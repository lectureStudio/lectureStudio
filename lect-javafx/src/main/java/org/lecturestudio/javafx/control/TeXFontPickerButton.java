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

import org.lecturestudio.core.text.TeXFont;

public class TeXFontPickerButton extends ExtSplitMenuButton {

	private final ObjectProperty<TeXFont> font = new SimpleObjectProperty<>(this, "text-font");

	private final ResourceBundle resourceBundle;


	@Inject
	public TeXFontPickerButton(ResourceBundle resourceBundle) {
		super();

		this.resourceBundle = resourceBundle;

		getStyleClass().add("split-picker-button");
	}

	/**
	 * The value of this FontPicker is either the value input by the user,
	 * or the last selected font.
	 */
	public ObjectProperty<TeXFont> texFontProperty() {
		return font;
	}

	public final void setTeXFont(TeXFont value) {
		texFontProperty().set(value);
	}

	public final TeXFont getTeXFont() {
		return texFontProperty().get();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TeXFontPickerButtonSkin(this, resourceBundle);
	}

}
