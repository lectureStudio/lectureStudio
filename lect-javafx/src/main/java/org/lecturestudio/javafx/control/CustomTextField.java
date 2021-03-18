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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.StyleableObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

import org.lecturestudio.javafx.util.FxStyleablePropertyFactory;

public class CustomTextField extends TextField {

	private static final String DEFAULT_STYLE_CLASS = "custom-text-field";

	private static final FxStyleablePropertyFactory<CustomTextField> FACTORY =
			new FxStyleablePropertyFactory<>(TextField.getClassCssMetaData());

	private final StyleableObjectProperty<Number> spacing;

	private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

	private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");


	public CustomTextField() {
		this("");
	}

	public CustomTextField(String text) {
		super(text);

		spacing = FACTORY.createNumberProperty(this, "node-spacing", "-fx-node-spacing", s -> s.spacing, 5);

		initialize();
	}

	public final double getNodeSpacing() {
		return spacing.get().doubleValue();
	}

	public final void setNodeSpacing(double value) {
		spacing.set(value);
	}

	public final ObservableValue<Number> nodeSpacingProperty() {
		return spacing;
	}

	public final Node getLeftNode() {
		return left.get();
	}

	public final void setLeftNode(Node value) {
		left.set(value);
	}

	public final ObjectProperty<Node> leftNodeProperty() {
		return left;
	}

	public final Node getRightNode() {
		return right.get();
	}

	public final void setRightNode(Node value) {
		right.set(value);
	}

	public final ObjectProperty<Node> rightNodeProperty() {
		return right;
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new CustomTextFieldSkin(this);
	}

	private void initialize() {
		getStyleClass().add(DEFAULT_STYLE_CLASS);
	}
}
