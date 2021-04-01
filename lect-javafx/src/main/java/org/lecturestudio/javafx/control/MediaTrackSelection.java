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

import org.lecturestudio.media.track.control.MediaTrackControl;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class MediaTrackSelection<T extends MediaTrackControl> extends Control {

	private final static String DEFAULT_STYLE_CLASS = "media-track-selection";

	private final ObjectProperty<T> trackControl = new SimpleObjectProperty<>();

	private final DoubleProperty leftSelection = new SimpleDoubleProperty();

	private final DoubleProperty rightSelection = new SimpleDoubleProperty();

	private final ObjectProperty<EventHandler<ActionEvent>> removeAction = new SimpleObjectProperty<>(this, "removeAction");


	public MediaTrackSelection() {
		initialize();
	}

	public final T getTrackControl() {
		return trackControl.get();
	}

	public final void setTrackControl(T control) {
		trackControl.set(control);
	}

	public final ObjectProperty<T> trackControlProperty() {
		return trackControl;
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

	/**
	 * The 'Remove'-button's action, which is invoked whenever the button is
	 * fired. This may be due to the user clicking on the button with the mouse,
	 * or by a touch event, or by a key press, or if the developer
	 * programmatically invokes the {@link #fire()} method.
	 */
	public final ObjectProperty<EventHandler<ActionEvent>> removeActionProperty() {
		return removeAction;
	}

	public final EventHandler<ActionEvent> getRemoveAction() {
		return removeActionProperty().get();
	}

	public final void setRemoveAction(EventHandler<ActionEvent> handler) {
		removeAction.set(handler);
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
