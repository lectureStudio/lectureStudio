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

import javafx.scene.control.Skin;

public class ClearableTextField extends CustomTextField {

	private static final String DEFAULT_STYLE_CLASS = "clearable-text-field";


	public ClearableTextField() {
		this("");
	}

	public ClearableTextField(String text) {
		super(text);

		initialize();
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/clearable-text-field.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new ClearableTextFieldSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
}
