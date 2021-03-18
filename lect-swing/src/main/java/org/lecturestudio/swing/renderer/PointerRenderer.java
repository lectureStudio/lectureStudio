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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.PointerShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;

/**
 * Implements a renderer for temporary pointer annotations.
 * 
 * @author Alex Andres
 */
public class PointerRenderer extends BaseRenderer {

	@Override
	public Class<? extends Shape> forClass() {
		return PointerShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		PointerShape pointerShape = (PointerShape) shape;
		PenPoint2D point = pointerShape.getPoint();

		Stroke stroke = pointerShape.getStroke();
		Color color = ColorConverter.INSTANCE.to(stroke.getColor());

		double w = stroke.getWidth();
		double w2 = w / 2;
		double xl = point.getX() - w2;
		double yl = point.getY() - w2;
		double xs = point.getX() - w2 / 2;
		double ys = point.getY() - w2 / 2;

		Composite oldComposite = context.getComposite();

		context.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
		context.fill(new Ellipse2D.Double(xl, yl, w, w));

		// Draw inner ring.
		context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		context.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		context.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
		context.setStroke(new BasicStroke((float) w / 8));
		context.draw(new Ellipse2D.Double(xs, ys, w2, w2));

		context.setComposite(oldComposite);
	}

}
