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

package org.lecturestudio.javafx.view;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public interface FxTopView extends FxView {

	void setOnKeyPressed(EventHandler<? super KeyEvent> value);

	void setOnMouseClicked(EventHandler<? super MouseEvent> value);

	void onShortcutClose();

	@Override
	default void onSceneSet() {
		setOnKeyPressed(event -> {
			switch (event.getCode()) {
				// Exit on escape key.
				case ESCAPE:
				// Exit on enter key.
				case ENTER:
					onShortcutClose();

					event.consume();
					break;
				default:
					break;
			}
		});
		setOnMouseClicked(event -> {
			if (event.getTarget() != this) {
				return;
			}

			// Exit on mouse click.
			onShortcutClose();
		});
	}

}
