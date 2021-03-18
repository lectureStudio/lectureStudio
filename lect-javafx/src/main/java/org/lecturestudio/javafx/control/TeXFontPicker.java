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

import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.util.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TeXFontPicker extends Control {

	private static final Logger LOG = LogManager.getLogger(TeXFontPicker.class);

	private static final String DEFAULT_STYLE_CLASS = "tex-font-picker";

	private final ObjectProperty<TeXFont> font = new SimpleObjectProperty<>(this, "font");

	private final ObjectProperty<EventHandler<ActionEvent>> okAction = new SimpleObjectProperty<>(this, "okAction");

	private final ObjectProperty<EventHandler<ActionEvent>> cancelAction = new SimpleObjectProperty<>(this, "cancelAction");

	private final ResourceBundle resourceBundle;


	public TeXFontPicker(ResourceBundle resourceBundle) {
		super();

		this.resourceBundle = resourceBundle;

		initialize();
	}
	
	/**
	 * The value of this TeXFontPicker is either the value input by the user,
	 * or the last selected font.
	 */
	public ObjectProperty<TeXFont> fontProperty() {
		return font;
	}

	public final void setFont(TeXFont value) {
		fontProperty().set(value);
	}

	public final TeXFont getFont() {
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
		return getClass().getResource("/resources/css/tex-font-picker.css").toExternalForm();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TeXFontPickerSkin(this, resourceBundle);
	}
	
	private void initialize() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);

		try {
			// Load TeX fonts.
			String[] listing = FileUtils.getResourceListing("/org/scilab/forge/jlatexmath/fonts", (name) -> name.endsWith(".ttf"));

			for (String filePath : listing) {
				Font.loadFont(getClass().getResourceAsStream(filePath), 1);
			}
		}
		catch (Exception e) {
			LOG.error("Load TeX fonts failed.", e);
		}
	}

}
