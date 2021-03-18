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

package org.lecturestudio.javafx.factory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ResourceBundle;

import javafx.scene.control.ListCell;

import org.lecturestudio.core.text.TeXFont;

public class TeXFontListCell extends ListCell<TeXFont.Type> {

	private final ResourceBundle resourceBundle;


	public TeXFontListCell(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	@Override
	public void updateItem(TeXFont.Type item, boolean empty) {
		super.updateItem(item, empty);

		if (nonNull(item)) {
			switch (item) {
				case SERIF:
					setText(resourceBundle.getString("font.serif"));
					break;
				case SANSSERIF:
					setText(resourceBundle.getString("font.sans-serif"));
					break;
				case BOLD:
					setText(resourceBundle.getString("font.bold"));
					break;
				case ITALIC:
					setText(resourceBundle.getString("font.italic"));
					break;
				case BOLD_ITALIC:
					setText(resourceBundle.getString("font.bold-italic"));
					break;
				case ROMAN:
					setText(resourceBundle.getString("font.roman"));
					break;
				case TYPEWRITER:
					setText(resourceBundle.getString("font.typewriter"));
					break;
			}
		}
		if (isNull(item) || empty) {
			setText("");
		}
	}

}
