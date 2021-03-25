/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class MediaTrackSelection extends Control {

	private final static String DEFAULT_STYLE_CLASS = "media-track-selection";

	private final DoubleProperty leftSelection = new SimpleDoubleProperty();

	private final DoubleProperty rightSelection = new SimpleDoubleProperty();


	public MediaTrackSelection() {
		initialize();
	}

	public final double getLeftSelection() {
		return leftSelection.get();
	}

	public final void setLeftSelection(double time) {
		leftSelection.set(time);
	}

	public final DoubleProperty leftSelectionProperty() {
		return leftSelection;
	}

	public final double getRightSelection() {
		return rightSelection.get();
	}

	public final void setRightSelection(double time) {
		rightSelection.set(time);
	}

	public final DoubleProperty rightSelectionProperty() {
		return rightSelection;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/media-track-selection.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new MediaTrackSelectionSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}
}
