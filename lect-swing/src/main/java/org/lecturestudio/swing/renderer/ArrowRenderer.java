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
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import org.lecturestudio.core.geometry.PathFactory;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.shape.ArrowShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;

/**
 * Implements a renderer for drawing a {@link ArrowShape}.
 * 
 * @author Alex Andres
 */
public class ArrowRenderer extends BaseRenderer {

	@Override
	public Class<? extends Shape> forClass() {
		return ArrowShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		ArrowShape arrowShape = (ArrowShape) shape;
		KeyEvent keyEvent = shape.getKeyEvent();
		Stroke stroke = arrowShape.getStroke();
		PenPoint2D p1 = arrowShape.getStartPoint().clone();
		PenPoint2D p2 = arrowShape.getEndPoint().clone();

		float width = (float) stroke.getWidth();

		AffineTransform oldTransform = context.getTransform();
		AffineTransform tx = new AffineTransform();
		Path2D path = PathFactory.createArrowPath(tx, keyEvent, p1, p2, width);

		context.setColor(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setStroke(new BasicStroke(width, CAP, JOIN));
		context.transform(tx);
		context.fill(path);

		// Focus
		if (shape.isSelected()) {
			boolean bold = arrowShape.isBold();
			boolean twoSided = arrowShape.isTwoSided();

			float penWidth = width * 0.7f;
			double scale = bold ? width * 2 : width;

			PenPoint2D v1 = p1.clone().subtract(p2).normalize().multiply(scale);
			PenPoint2D v2 = p2.clone().subtract(p1).normalize().multiply(scale);

			if (twoSided) {
				p1 = p1.subtract(v1);
			}

			p2 = p2.subtract(v2);

			tx = new AffineTransform();
			path = PathFactory.createArrowPath(tx, keyEvent, p1, p2, penWidth);

			context.setTransform(oldTransform);
			context.setColor(FOCUS_COLOR);
			context.transform(tx);
			context.fill(path);
		}
	}

}
