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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.view.TeXBoxView;

public class TeXBox extends PageObject<TeXShape> implements TeXBoxView {

	private static final String DEFAULT_STYLE_CLASS = "tex-box";

	private final ObjectProperty<TeXFont> font = new SimpleObjectProperty<>();


	public TeXBox() {
		initialize();
	}

	public ObjectProperty<TeXFont> fontProperty() {
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
	public TeXFont getTextFont() {
		return nonNull(getPageShape()) ? getPageShape().getFont() : null;
	}

	@Override
	public void setTextFont(TeXFont font) {
		if (nonNull(getPageShape())) {
			getPageShape().setFont(font);
		}
	}

	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/resources/css/tex-box.css").toExternalForm();
	}

	@Override
	public void dispose() {

	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TeXBoxSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}

}
