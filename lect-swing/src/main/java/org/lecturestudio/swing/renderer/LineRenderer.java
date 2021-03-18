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
import java.awt.geom.Line2D;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.model.shape.LineShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;

/**
 * Implements a renderer for drawing a {@link LineShape}.
 * 
 * @author Alex Andres
 */
public class LineRenderer extends BaseRenderer {

	@Override
	public Class<? extends Shape> forClass() {
		return LineShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		LineShape lineShape = (LineShape) shape;
		float width = (float) lineShape.getLineWidth();
		Stroke stroke = lineShape.getStroke();
		Point2D p1 = lineShape.getStartPoint();
		Point2D p2 = lineShape.getEndPoint();

		Line2D line = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());

		context.setColor(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setStroke(new BasicStroke(width, CAP, JOIN));
		context.draw(line);

		// Focus
		if (shape.isSelected()) {
			context.setColor(FOCUS_COLOR);
			context.setStroke(new BasicStroke(width * 0.7f, CAP, JOIN));
			context.draw(line);
		}
	}

}
