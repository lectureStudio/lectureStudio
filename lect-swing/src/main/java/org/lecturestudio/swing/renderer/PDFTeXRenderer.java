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

package org.lecturestudio.swing.renderer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.scilab.forge.jlatexmath.TeXFormula.TeXIconBuilder;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.swing.converter.ColorConverter;

/**
 * Renderer for TeX shapes.
 * 
 * @author Alex Andres
 */
public class PDFTeXRenderer extends BaseRenderer {

	public Class<? extends Shape> forClass() {
		return TeXShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		TeXShape teXShape = (TeXShape) shape;
		String text = teXShape.getText();

		if (text.isEmpty()) {
			return;
		}

		AffineTransform t = context.getTransform();
		TeXFont font = teXShape.getFont();
		
		double sx = t.getScaleX();
		double sy = t.getScaleY();
		
		// Perform rendering with identity transformation.
		Rectangle2D bounds = shape.getBounds();
		double x = bounds.getX() * sx + t.getTranslateX();
		double y = bounds.getY() * sy + t.getTranslateY();
		
		context.setTransform(AffineTransform.getTranslateInstance(x, y));
		context.scale(-1, 1);
		
		// Scale text.
		float textSize = (float) (font.getSize() * sy);

		TeXFormula formula = new TeXFormula(text);
		TeXIconBuilder builder = formula.new TeXIconBuilder()
				.setStyle(TeXConstants.STYLE_DISPLAY)
				.setSize(textSize)
				.setType(font.getType().getValue())
				.setFGColor(ColorConverter.INSTANCE.to(teXShape.getTextColor()));
		
		TeXIcon icon = builder.build();

		icon.paintIcon(null, context, 0, 0);
	}

}
