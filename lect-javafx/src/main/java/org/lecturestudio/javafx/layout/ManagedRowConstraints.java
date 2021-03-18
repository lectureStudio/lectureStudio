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

package org.lecturestudio.javafx.layout;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.RowConstraints;

public class ManagedRowConstraints extends RowConstraints {

	private final BooleanProperty visible = new SimpleBooleanProperty();

	private final double initialMaxHeight;
	private final double initialMinHeight;
	private final double initialPrefHeight;


	public ManagedRowConstraints() {
		super();

		initialMaxHeight = getMaxHeight();
		initialMinHeight = getMinHeight();
		initialPrefHeight = getPrefHeight();

		visibleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				show();
			}
			else {
				hide();
			}
		});
	}

	public BooleanProperty visibleProperty() {
		return visible;
	}

	public boolean isVisible() {
		return visibleProperty().get();
	}

	public void setVisible(boolean visible) {
		visibleProperty().set(visible);
	}

	private void hide() {
		setMaxHeight(0);
		setMinHeight(0);
		setPrefHeight(0);
	}

	private void show() {
		setMaxHeight(initialMaxHeight);
		setMinHeight(initialMinHeight);
		setPrefHeight(initialPrefHeight);
	}

}
