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
import javafx.scene.paint.Color;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.model.shape.GridShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

/**
 * Renders a {@link GridShape}.
 *
 * @author Alex Andres
 */
public class GridRenderer implements Renderer<GraphicsContext> {

	@Override
	public Class<? extends Shape> forClass() {
		return GridShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		GridShape gridShape = (GridShape) shape;
		Color color = ColorConverter.INSTANCE.to(gridShape.getColor());
		Dimension2D viewRatio = gridShape.getViewRatio();

		double lineWidth = 1 / context.getTransform().getMxx();

		context.setStroke(color);
		context.setLineWidth(lineWidth);

		double width = viewRatio.getWidth();
		double height = viewRatio.getHeight();
		double offset = gridShape.getVerticalLinesInterval() / 100.0;

		double step = width / (1.0 / offset);

		// Draw vertical lines.

		if (gridShape.getVerticalLinesVisible()) {
			for (double x = 0; x <= width; x += step) {
				context.strokeLine(x, 0, x, height);
			}
		}

		// Draw horizontal lines.

		if (gridShape.getHorizontalLinesVisible()) {
			for (double y = 0; y <= height; y += step) {
				context.strokeLine(0, y, width, y);
			}
		}
	}
}
