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

package org.lecturestudio.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.lecturestudio.core.beans.ChangeListener;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.view.TeXBoxView;
import org.lecturestudio.swing.components.TextInputPageObject;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

public class SwingTeXBoxView extends TextInputPageObject<TeXShape> implements TeXBoxView {

	private static final java.awt.Color THEME_COLOR = new java.awt.Color(125, 40, 110);

	private ChangeListener<String> listener;

	private JLabel label;

	private TeXFont font;

	private double fontSize;


	public SwingTeXBoxView() {
		super();
	}

	@Override
	public void setFocus(boolean focus) {
		super.setFocus(focus);

		if (focus) {
			label.requestFocus();
		}
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
	protected void dispose() {
		if (nonNull(listener)) {
			getPageShape().textProperty().removeListener(listener);
		}

		super.dispose();
	}

	@Override
	protected void updateToShapeContent(TeXShape shape) {
		if (isNull(shape)) {
			return;
		}

		font = shape.getFont();
		fontSize = font.getSize();

		listener = (observable, oldValue, newValue) -> {
			updateContent();
			updateContentSize();
		};

		shape.textProperty().addListener(listener);

		updateContent();
	}

	@Override
	protected void updateToTransform(AffineTransform transform) {
		TeXShape shape = getPageShape();
		Insets padding = getInsets();
		Rectangle textBounds = label.getBounds();
		Rectangle2D shapeRect = shape.getBounds();

		double xOffset = textBounds.getMinX() + padding.left + 1;
		double yOffset = textBounds.getMinY() + padding.top + 1;

		if (!getFocus()) {
//			yOffset = padding.top + 1;
		}

		double s = transform.getScaleX();
		double tx = transform.getTranslateX();
		double ty = transform.getTranslateY();
		double x = Math.ceil((shapeRect.getX() + tx) * s) - xOffset;
		double y = Math.ceil((shapeRect.getY() + ty) * s) - yOffset;

		setLocation((int) x, (int) y);
		updateContentSize();
	}

	@Override
	protected java.awt.Color getThemeColor() {
		return THEME_COLOR;
	}

	@Override
	protected JComponent createContent() {
		label = new JLabel();
		label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		label.setForeground(new java.awt.Color(0, 0, 0, 0));

		return label;
	}

	@Override
	protected void onRelocateShape(Point2D location) {
		getPageShape().setLocation(location);
	}

	private void updateContent() {
		float fontSize = getTextFont().getSize();

		TeXShape shape = getPageShape();
		TeXFont font = shape.getFont();
		TeXFormula formula = new TeXFormula(shape.getText());
		TeXFormula.TeXIconBuilder builder = formula.new TeXIconBuilder()
				.setStyle(TeXConstants.STYLE_DISPLAY)
				.setSize(fontSize)
				.setType(font.getType().getValue());

		label.setIcon(builder.build());

		updateContentSize();
	}
}
