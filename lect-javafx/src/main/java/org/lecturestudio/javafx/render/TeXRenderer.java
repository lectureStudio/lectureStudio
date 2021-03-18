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

package org.lecturestudio.javafx.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.core.text.TeXFont;

import org.jfree.fx.FXGraphics2D;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

/**
 * Renderer for TeX shapes.
 *
 * @author Alex Andres
 */
public class TeXRenderer implements Renderer<GraphicsContext> {

	@Override
	public Class<? extends Shape> forClass() {
		return TeXShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) throws Exception {
		TeXShape teXShape = (TeXShape) shape;
		String text = teXShape.getText();

		if (text.isEmpty()) {
			return;
		}

		TeXFormula formula;

		try {
			formula = new TeXFormula(text);
		}
		catch (Exception e) {
			// Ignore. Render only, if text could be parsed.
			return;
		}

		context.save();

		Transform t = context.getTransform();
		TeXFont font = teXShape.getFont();

		double sx = t.getMxx();
		double sy = t.getMyy();

		// Perform rendering with identity transformation.
		Rectangle2D bounds = shape.getBounds();
		double x = bounds.getX() * sx + t.getTx();
		double y = bounds.getY() * sy + t.getTy();

		context.setTransform(new Affine());

		// Scale text.
		float textSize = (float) (font.getSize() * sy);

		TeXFormula.TeXIconBuilder builder = formula.new TeXIconBuilder()
				.setStyle(TeXConstants.STYLE_DISPLAY)
				.setSize(textSize)
				.setType(font.getType().getValue())
				.setFGColor(toAwtColor(teXShape.getTextColor()));

		TeXIcon icon = builder.build();

		FXGraphics2D graphics = new FXGraphics2D(context);

		icon.paintIcon(null, graphics, (int) x, (int) y);

		context.restore();
	}

	private static java.awt.Color toAwtColor(Color c) {
		return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
	}
}
