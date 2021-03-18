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

import javafx.event.EventDispatcher;
import javafx.scene.control.MenuItem;
import javafx.scene.control.skin.SplitMenuButtonSkin;

import org.lecturestudio.core.text.TeXFont;

public class TeXFontPickerButtonSkin extends SplitMenuButtonSkin {

	/**
	 * Creates a new TeXFontPickerButtonSkin instance, installing the necessary child
	 * nodes into the Control children list, as well as the necessary input mappings
	 * for handling key, mouse, etc events.
	 *
	 * @param control The control that this skin should be installed onto.
	 */
	public TeXFontPickerButtonSkin(TeXFontPickerButton control, ResourceBundle resourceBundle) {
		super(control);

		initialize(control, resourceBundle);
	}

	private void initialize(TeXFontPickerButton control, ResourceBundle resourceBundle) {
		TeXFontPicker fontPicker = new TeXFontPicker(resourceBundle);
		fontPicker.fontProperty().bindBidirectional(control.texFontProperty());
		fontPicker.setOkAction(event -> {
			control.setTeXFont(fontPicker.getFont());
			control.hide();
		});
		fontPicker.setCancelAction(event -> control.hide());

		MenuItem item = new MenuItem();
		item.setGraphic(fontPicker);

		final EventDispatcher dispatcher = (event, eventDispatchChain) -> {
			eventDispatchChain.dispatchEvent(event);
			// No bubbling.
			return null;
		};

		control.addEventFilter(FontPickerButton.ON_SHOWN, event -> {
			item.getStyleableNode().setEventDispatcher(dispatcher);

			fontPicker.requestFocus();
		});

		control.setTeXFont(new TeXFont(TeXFont.Type.SERIF, 20));
		control.getItems().add(item);
	}
}
