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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.lecturestudio.core.model.shape.RectangleShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.Rectangle2DConverter;

/**
 * Implements a renderer for drawing a {@link RectangleShape}.
 * 
 * @author Alex Andres
 */
public class RectangleRenderer extends BaseRenderer {

	@Override
	public Class<? extends Shape> forClass() {
		return RectangleShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		RectangleShape rectShape = (RectangleShape) shape;
		Stroke stroke = rectShape.getStroke();
		float width = (float) stroke.getWidth();

		Rectangle2D rect = Rectangle2DConverter.INSTANCE.to(rectShape.getRect());

		context.setColor(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setStroke(new BasicStroke(width));

		if (rectShape.fill()) {
			context.fill(rect);
		}
		else {
			context.draw(rect);
		}

		// Focus
		if (shape.isSelected()) {
			context.setColor(FOCUS_COLOR);

			if (rectShape.fill()) {
				rect = new Rectangle2D.Double(rect.getX() + width, rect.getY() + width, rect.getWidth() - 2 * width, rect.getHeight() - 2 * width);

				context.fill(rect);
			}
			else {
				float penWidth = width * 0.5f;

				context.setStroke(new BasicStroke(penWidth, CAP, JOIN));
				context.draw(rect);
			}
		}
	}

}
