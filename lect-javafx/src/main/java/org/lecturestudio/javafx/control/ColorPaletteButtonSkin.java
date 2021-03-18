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

public class ColorPaletteButtonSkin extends SplitMenuButtonSkin {

	/**
	 * Creates a new ColorPaletteButtonSkin instance, installing the necessary child
	 * nodes into the Control children list, as well as the necessary input mappings
	 * for handling key, mouse, etc events.
	 *
	 * @param control The control that this skin should be installed onto.
	 */
	public ColorPaletteButtonSkin(ColorPaletteButton control, ResourceBundle resourceBundle) {
		super(control);

		initialize(control, resourceBundle);
	}

	private void initialize(ColorPaletteButton control, ResourceBundle resourceBundle) {
		ColorPalette colorPalette = new ColorPalette(resourceBundle);
		colorPalette.colorProperty().bindBidirectional(control.colorProperty());
		colorPalette.colorProperty().addListener(observable -> {
			control.hide();
			control.fire();
		});

		MenuItem item = new MenuItem();
		item.setGraphic(colorPalette);

		final EventDispatcher dispatcher = (event, eventDispatchChain) -> {
			eventDispatchChain.dispatchEvent(event);
			// No bubbling.
			return null;
		};

		control.addEventFilter(FontPickerButton.ON_SHOWN, event -> {
			item.getStyleableNode().setEventDispatcher(dispatcher);

			colorPalette.requestFocus();
		});

		control.getItems().add(item);
	}

}
