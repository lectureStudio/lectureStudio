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

import static java.util.Objects.nonNull;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.javafx.beans.converter.FontConverter;

public class TextBox extends PageObject<TextShape> implements TextBoxView {

	private static final String DEFAULT_STYLE_CLASS = "text-box";

	private final ObjectProperty<javafx.scene.text.Font> font = new SimpleObjectProperty<>();


	public TextBox() {
		initialize();
	}

	public ObjectProperty<javafx.scene.text.Font> fontProperty() {
		return font;
	}

	@Override
	public String getText() {
		return nonNull(getPageShape()) ? getPageShape().getText() : null;
	}

	@Override
	public void setText(String text) {
		if (nonNull(getPageShape())) {
			getPageShape().setText(text);
		}
	}

	@Override
	public TextAttributes getTextAttributes() {
		return nonNull(getPageShape()) ? getPageShape().getTextAttributes() : null;
	}

	@Override
	public void setTextAttributes(TextAttributes attributes) {
		if (nonNull(getPageShape())) {
			getPageShape().setTextAttributes(attributes);
		}
	}

	@Override
	public Color getTextColor() {
		return nonNull(getPageShape()) ? getPageShape().getTextColor() : null;
	}

	@Override
	public void setTextColor(Color color) {
		if (nonNull(getPageShape())) {
			getPageShape().setTextColor(color);
		}
	}

	@Override
	public Font getTextFont() {
		return nonNull(getPageShape()) ? getPageShape().getFont() : null;
	}

	@Override
	public void setTextFont(Font font) {
		if (nonNull(getPageShape())) {
			getPageShape().setFont(font);
			fontProperty().set(FontConverter.INSTANCE.to(font));
		}
	}

	@Override
	public String getUserAgentStylesheet() {
		return Objects.requireNonNull(getClass().getResource("/resources/css/text-box.css")).toExternalForm();
	}

	@Override
	public void dispose() {

	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TextBoxSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

}
