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

import javafx.beans.property.*;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCombination;

import org.lecturestudio.javafx.util.AcceleratorHandler;
import org.lecturestudio.javafx.util.AcceleratorSupport;

public class ExtToggleButton extends ToggleButton implements AcceleratorSupport {

	/**
	 * The accelerator property enables accessing the associated action in one keystroke.
	 * It is a convenience offered to perform quickly a given action.
	 */
	private ObjectProperty<KeyCombination> accelerator;


	@Override
	public final void setAccelerator(KeyCombination value) {
		acceleratorProperty().set(value);
	}

	@Override
	public final KeyCombination getAccelerator() {
		return accelerator == null ? null : accelerator.get();
	}

	@Override
	public final ObjectProperty<KeyCombination> acceleratorProperty() {
		if (accelerator == null) {
			accelerator = new SimpleObjectProperty<>(this, "accelerator");

			AcceleratorHandler.set(this);
		}
		return accelerator;
	}
	
}
