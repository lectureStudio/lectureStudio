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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.SelectShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;

/**
 * Implements a renderer for rendering SelectShapes, e.g. it draws a
 * rectangle to indicate the selection area.
 *
 * @author Alex Andres
 */
public class SelectRenderer implements Renderer<GraphicsContext> {

	private static final Color FRAME = Color.rgb(255, 0, 100, 1.0);


	@Override
	public Class<? extends Shape> forClass() {
		return SelectShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		Rectangle2D bounds = shape.getBounds().clone();

		double width = 2 / context.getTransform().getMxx();
		double dash = 4 / context.getTransform().getMyy();

		context.setLineDashOffset(0);
		context.setLineDashes(dash);
		context.setLineCap(StrokeLineCap.BUTT);
		context.setLineJoin(StrokeLineJoin.BEVEL);
		context.setLineWidth(width);
		context.setStroke(FRAME);
		context.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}
}
