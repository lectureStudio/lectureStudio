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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.text.Font;

public class FontPicker extends Control {

	private static final String DEFAULT_STYLE_CLASS = "font-picker";
	
	private final ObjectProperty<Font> font = new SimpleObjectProperty<>(this, "font");
	
	private final ObjectProperty<EventHandler<ActionEvent>> okAction = new SimpleObjectProperty<>(this, "okAction");
	
	private final ObjectProperty<EventHandler<ActionEvent>> cancelAction = new SimpleObjectProperty<>(this, "cancelAction");

	private final ResourceBundle resourceBundle;


	public FontPicker(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;

		initialize();
	}
	
	/**
	 * The value of this FontPicker is either the value input by the user,
	 * or the last selected font.
	 */
	public ObjectProperty<Font> fontProperty() {
		return font;
	}

	public final void setFont(Font value) {
		fontProperty().set(value);
	}

	public final Font getFont() {
		return fontProperty().get();
	}
	
	/**
     * The 'Ok'-button's action, which is invoked whenever the button is fired.
     * This may be due to the user clicking on the button with the mouse, or by
     * a touch event, or by a key press, or if the developer programmatically
     * invokes the {@link #fire()} method.
     */
	public final ObjectProperty<EventHandler<ActionEvent>> okActionProperty() {
		return okAction;
	}

	public final EventHandler<ActionEvent> getOkAction() {
		return okActionProperty().get();
	}

	public final void setOkAction(EventHandler<ActionEvent> handler) {
		okAction.set(handler);
	}

	/**
     * The 'Cancel'-button's action, which is invoked whenever the button is fired.
     * This may be due to the user clicking on the button with the mouse, or by
     * a touch event, or by a key press, or if the developer programmatically
     * invokes the {@link #fire()} method.
     */
	public final ObjectProperty<EventHandler<ActionEvent>> cancelActionProperty() {
		return cancelAction;
	}

	public final EventHandler<ActionEvent> getCancelAction() {
		return cancelActionProperty().get();
	}

	public final void setCancelAction(EventHandler<ActionEvent> handler) {
		cancelAction.set(handler);
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/font-picker.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new FontPickerSkin(this, resourceBundle);
	}

	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);

		// Set default font.
		setFont(Font.font("Open Sans"));
	}

}
