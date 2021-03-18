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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.shape.GridShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.swing.converter.ColorConverter;

/**
 * Renders a {@link GridShape}.
 * 
 * @author Alex Andres
 */
public class GridRenderer extends BaseRenderer {

	@Override
	public Class<? extends Shape> forClass() {
		return GridShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		GridShape gridShape = (GridShape) shape;
		Dimension2D viewRatio = gridShape.getViewRatio();
		Color color = ColorConverter.INSTANCE.to(gridShape.getColor());

		float lineWidth = (float) (1 / context.getTransform().getScaleX());

		context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		context.setStroke(new BasicStroke(lineWidth));
		context.setColor(color);

		double width = viewRatio.getWidth();
		double height = viewRatio.getHeight();
		double offset = gridShape.getVerticalLinesInterval() / 100.0;

		double step = width / (1.0 / offset);

		// Draw vertical lines.

		if (gridShape.getVerticalLinesVisible()) {
			for (double x = 0; x <= width; x += step) {
				context.draw(new Line2D.Double(x, 0, x, height));
			}
		}

		// Draw horizontal lines.

		if (gridShape.getHorizontalLinesVisible()) {
			for (double y = 0; y <= height; y += step) {
				context.draw(new Line2D.Double(0, y, width, y));
			}
		}
	}
}
