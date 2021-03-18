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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.transform.Transform;

import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.text.TeXFont;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class TeXBoxSkin extends PageObjectSkin<TeXBox> {

	private ChangeListener<String> textListener;

	private Region textPane;

	private double fontSize;


	TeXBoxSkin(TeXBox control) {
		super(control);
	}

	@Override
	protected Node createContent() {
		textListener = (observable, oldValue, newValue) -> {
			updateContent();
		};

		TeXBox texBox = getSkinnable();
		texBox.fontProperty().set(texBox.getPageShape().getFont());
		texBox.fontProperty().addListener(observable -> updateContent());
		texBox.getPageShape().textProperty().addListener(textListener);

		fontSize = texBox.getPageShape().getFont().getSize();

		textPane = new Region();

		relocate();
		resize();

		return textPane;
	}

	@Override
	protected void onParentChange(Node parent) {
		// Detached / removed from the scene graph.
		if (isNull(parent)) {
			// Remove bindings.
			TeXShape shape = getSkinnable().getPageShape();

			if (nonNull(shape) && nonNull(textListener)) {
				shape.textProperty().removeListener(textListener);
			}
		}
	}

	@Override
	protected void onPostLayout() {
		updateFontSize();
	}

	@Override
	protected void onRelocateShape(Point2D location) {
		TeXShape shape = getSkinnable().getPageShape();
		shape.setLocation(location);
	}

	@Override
	protected void onTransform() {
		updateFontSize();
	}

	private void relocate() {
		TeXBox textBox = getSkinnable();
		Transform transform = textBox.getPageTransform();
		Bounds textBounds = textPane.getBoundsInParent();
		Rectangle2D shapeRect = textBox.getPageShape().getBounds();

		Insets padding = textBox.getPadding();

		double xOffset = textBounds.getMinX() + padding.getLeft() + 1;
		double yOffset = textBounds.getMinY() + padding.getTop() + 1;

		double sx = transform.getMxx();
		double sy = transform.getMyy();
		double tx = transform.getTx();
		double ty = transform.getTy();
		double x = Math.ceil((shapeRect.getX() + tx) * sx) - xOffset;
		double y = Math.ceil((shapeRect.getY() + ty) * sy) - yOffset;

		textBox.relocate(x, y);
	}

	private void updateFontSize() {
		TeXBox textBox = getSkinnable();
		Transform transform = textBox.getPageTransform();

		TeXFont font = textBox.getPageShape().getFont().clone();
		font.setSize((float) (fontSize * transform.getMyy()));

		textBox.fontProperty().set(font);

		relocate();
		resize();
	}

	private void updateContent() {
		TeXBox texBox = getSkinnable();
		TeXShape shape = texBox.getPageShape();

		float fontSize = texBox.fontProperty().get().getSize();

		TeXFont font = shape.getFont();
		TeXFormula formula = new TeXFormula(shape.getText());
		TeXFormula.TeXIconBuilder builder = formula.new TeXIconBuilder()
				.setStyle(TeXConstants.STYLE_DISPLAY)
				.setSize(fontSize)
				.setType(font.getType().getValue());

		TeXIcon icon = builder.build();

		textPane.setPrefWidth(icon.getIconWidth());
		textPane.setPrefHeight(icon.getIconHeight());

		if (texBox.getText().isEmpty()) {
			textPane.setPrefHeight(fontSize);
		}

		resize();
	}

}
