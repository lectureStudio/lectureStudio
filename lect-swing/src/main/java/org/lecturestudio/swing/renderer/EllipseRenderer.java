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
import java.awt.geom.Ellipse2D;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.EllipseShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;

/**
 * Implements a renderer for drawing a {@link EllipseShape}.
 * 
 * @author Alex Andres
 */
public class EllipseRenderer extends BaseRenderer {

	@Override
	public Class<? extends Shape> forClass() {
		return EllipseShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		EllipseShape ellipseShape = (EllipseShape) shape;
		KeyEvent keyEvent = shape.getKeyEvent();
		Stroke stroke = ellipseShape.getStroke();
		float width = (float) stroke.getWidth();

		Rectangle2D rect = ellipseShape.getRect();
		Ellipse2D.Double ellipse = new Ellipse2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

		context.setColor(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setStroke(new BasicStroke(width));

		if (keyEvent != null && keyEvent.isAltDown()) {
			context.fill(ellipse);
		}
		else {
			context.draw(ellipse);
		}

		// Focus
		if (shape.isSelected()) {
			context.setColor(FOCUS_COLOR);

			if (keyEvent != null && keyEvent.isAltDown()) {
				ellipse = new Ellipse2D.Double(rect.getX() + width, rect.getY() + width, rect.getWidth() - 2 * width, rect.getHeight() - 2 * width);

				context.fill(ellipse);
			}
			else {
				float penWidth = width * 0.5f;

				context.setStroke(new BasicStroke(penWidth, CAP, JOIN));
				context.draw(ellipse);
			}
		}
	}

}
