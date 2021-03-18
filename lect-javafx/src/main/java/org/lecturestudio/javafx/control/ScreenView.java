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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import org.lecturestudio.core.view.Screen;
import org.lecturestudio.javafx.event.ScreenActionEvent;

public class ScreenView extends Control {

	private static final String DEFAULT_STYLE_CLASS = "screen-view";

	private final ObservableList<Screen> screens = FXCollections.observableArrayList();

	/** The screen rectangle's action, which is invoked whenever the button is clicked. */
	private final ObjectProperty<EventHandler<ScreenActionEvent>> onAction = new ObjectPropertyBase<>() {

		@Override
		public Object getBean() {
			return ScreenView.this;
		}

		@Override
		public String getName() {
			return "onAction";
		}
	};


	public ScreenView() {
		initialize();
	}

	/**
	 * @return the property to represent the screen rectangle's action, which is invoked
	 * whenever the rectangle is clicked.
	 */
	public final ObjectProperty<EventHandler<ScreenActionEvent>> onActionProperty() {
		return onAction;
	}

	public final void setOnAction(EventHandler<ScreenActionEvent> value) {
		onActionProperty().set(value);
	}

	public final EventHandler<ScreenActionEvent> getOnAction() {
		return onActionProperty().get();
	}

	public ObservableList<Screen> getScreens() {
		return screens;
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/screen-view.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new ScreenViewSkin(this);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
	}
}
